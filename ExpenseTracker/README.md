# Expense Tracker

A local-first Android expense and subscription tracker. It reads bank, UPI, wallet, and SMS
notifications, turns payment text into transactions, and keeps an autopay ledger. There is no
backend, no bank login, and no analytics. An optional on-device language model handles unusual
alerts that the deterministic parser cannot read.

- Package: `com.expensetracker.app`
- Minimum Android: 8.0 (API 26)
- Target/compile SDK: 36
- Language/UI: Kotlin + Jetpack Compose (Material 3)
- Verified debug build on a OnePlus CPH2707 (Android 16 / API 36)

---

## Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Data model](#data-model)
4. [Notification pipeline](#notification-pipeline)
5. [Parsing rules](#parsing-rules)
6. [Optional on-device LLM fallback](#optional-on-device-llm-fallback)
7. [User interface](#user-interface)
8. [Project structure](#project-structure)
9. [Build and run](#build-and-run)
10. [Tests and verification](#tests-and-verification)
11. [Troubleshooting](#troubleshooting)
12. [Privacy and security](#privacy-and-security)
13. [Known limitations](#known-limitations)
14. [Key files](#key-files)

---

## Features

- Automatic capture from bank, UPI, wallet, and messaging notifications.
- Deterministic parsing of amount, currency, debit/credit, merchant, account, and autopay signals.
- Optional on-device Qwen2.5 0.5B fallback for unusual payment wording.
- Transaction list grouped by day, with search, category filters, and date ranges.
- Manual add, edit, and delete for cash or missed transactions.
- Autopay detection, monthly-equivalent totals, next expected date, history, cancel, and reactivate.
- Daily background audit that marks overdue mandates stale.
- Notification-access onboarding card and a persistent on-device privacy banner.
- In-app AI model manager: download, progress, ready state, and remove.

---

## Architecture

```text
NotificationListenerService
        |
        v
ExpenseRepository.recordNotification()
        |
        +--> NotificationParser (regex, on-device)
        |         |
        |         +--> amount + currency found but no transaction? 
        |                    |
        |                    v
        |              LlmFallbackParser (optional, local model)
        |                    |
        |                    v
        |              LlmResponseParser (strict JSON)
        |
        +--> 60-second duplicate check
        +--> autopay upsert (if applicable)
        +--> Room insert
        +--> Toast confirmation
```

Layering:

| Layer | Responsibility |
|---|---|
| Compose UI | Screens, dialogs, filters, navigation, model status |
| ViewModels | Filtering, grouping, summaries, user actions |
| `ExpenseRepository` | Single write path, dedupe, autopay lifecycle |
| Parsers | Pure text-to-transaction logic; unit testable |
| Room | Local persistence for transactions and autopays |
| WorkManager | Daily autopay staleness audit |
| MediaPipe GenAI | Local LLM inference; never a network service |

The project uses Hilt for dependency injection. `ExpenseTrackerApp` is also the
`Configuration.Provider` for Hilt-injected WorkManager workers.

---

## Data model

Database: `expense-tracker.db` (Room, version 1, schema export disabled).

### `transactions`

| Column | Type | Notes |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `merchantName` | String | Parsed or user-edited |
| `amount` | Double | Always taken from the notification regex for notified rows |
| `currency` | String | `INR`, `USD`, `EUR`, `GBP`; defaults to `USD` |
| `timestamp` | Long | Epoch millis, from the notification post time |
| `category` | String | `Entertainment`, `Food`, `Travel`, `Bills`, `Health`, `Subscription`, `Shopping`, `Other` |
| `accountSource` | String | Bank/last four, source app label, or `Cash` |
| `transactionType` | String | `DEBIT` or `CREDIT` |
| `isAutopay` | Boolean | True for detected recurring debits |
| `autopayId` | Long? | Link to the matching autopay |
| `rawNotificationText` | String? | Original text, truncated to 500 characters |

### `autopays`

| Column | Type | Notes |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `serviceName` | String | Merchant/service name, unique case-insensitively among non-cancelled rows |
| `estimatedAmount` | Double | Most recent charge amount |
| `billingCycle` | String | `MONTHLY`, `YEARLY`, `QUARTERLY`, `WEEKLY` |
| `lastChargedTimestamp` | Long | Most recent detected charge |
| `nextExpectedTimestamp` | Long | Computed from the billing cycle |
| `status` | String | `ACTIVE`, `CANCELLED`, `PAUSED`, `STALE` |
| `category` | String | Inherited from the transaction |

Autopay status shown in the UI is derived by `displayStatus`:

- Explicit `CANCELLED`, `PAUSED`, or `STALE` wins.
- Past `nextExpectedTimestamp` becomes `STALE`.
- Due within 7 days becomes `UPCOMING`.
- Otherwise `ACTIVE`.

The daily worker marks `ACTIVE` autopays stale when their expected date is more than 7 days in
the past. The UI also recalculates status on read. It is registered as unique periodic work
named `autopay-audit` with a `KEEP` policy.

---

## Notification pipeline

`TransactionNotificationListener` is declared with
`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`. It ignores:

- Notifications from this app itself.
- Ongoing notifications.
- Group summaries.
- Packages that are not allowlisted or do not match the banking/messaging hints.

Accepted sources:

- `com.google.android.apps.nbu.paisa.user` (GPay)
- `com.phonepe.app`
- `net.one97.paytm`
- `com.google.android.apps.messaging`, `com.android.mms`, `com.samsung.android.messaging`,
  `com.microsoft.android.smsorganizer`
- `com.revolut.revolut`, `com.chase.sig.android`, `com.chase`
- `com.americanexpress.android.acctsvcs.us`
- `com.cred`, `com.dreamplug.androidapp`
- Package-name hints containing `bank`, `paytm`, `phonepe`, `paisa`, `wallet`, `revolut`,
  `chase`, `amex`, `cred`, `messaging`, `mms`, or `sms`
- `com.android.shell` in **debug builds only** for adb testing

The listener concatenates `EXTRA_TITLE`, `EXTRA_TEXT`, `EXTRA_BIG_TEXT`, and `EXTRA_SUB_TEXT`,
then calls the repository.

Processing order:

1. Regex parse with `NotificationParser`.
2. If that fails, extract an amount. If an amount exists, try the optional LLM fallback.
3. Reject the row if the same merchant and amount were recorded in the last 60 seconds.
4. Upsert an autopay when the text looks recurring.
5. Insert the transaction and show a short Toast.

The duplicate window is a simple `merchant + amount + timestamp` lookup. It prevents repeat
callbacks from the same notification but can also suppress two legitimate identical payments
within one minute.

---

## Parsing rules

`NotificationParser` is pure Kotlin with no Android dependencies and is unit tested.

### Text handling

- Newlines and repeated whitespace are collapsed.
- Text is capped at 500 characters.
- Only one amount is used per notification.

### Amount and currency

- Matches currency-first (`Rs 450`, `INR 1,299.00`, `$119.99`) or amount-first
  (`450 INR`, `119.99 USD`).
- Accepts commas and up to two decimal places.
- Rejects non-finite values, zero/negative values, and amounts of 100,000,000 or more.
- Currency is inferred from the matched token; unknown symbols default to `USD`.

### Debit / credit

- Debit words: `debited`, `debit`, `paid`, `spent`, `purchase`, `purchased`, `charged`,
  `withdrawn`, `sent`, `deducted`, `renewed`, `renewal`, `txn of`, `payment of`.
- Credit words: `credited`, `credit`, `received`, `refund`, `refunded`, `cashback`,
  `deposited`, `reversal`.
- Debit wins when both appear.

### Safety guards

- If OTP/verification wording is present without a completed-payment cue, the notification is
  ignored. This prevents "Use OTP to verify payment of Rs 500" from being recorded.
- The LLM path additionally requires a payment cue. Messages such as "out for delivery" or
  "Order confirmed" are skipped without loading the model.

### Merchant extraction

Patterns for debit: `to`, `at`, `for`, `from`. Patterns for credit: `from`, `to`, `at`, `for`.
Cleaning rules:

- Cut at `@` (UPI IDs).
- Trim punctuation and trailing stop words (`via`, `using`, `ref`, `upi`, `card`, and similar).
- Reject candidates shorter than two characters, without a letter, or in the ignored list.
- Reject names containing "account".
- Cap at 48 characters.
- Fall back to `Recurring payment` for autopay or `Unknown`.

### Account source

- Last four digits from patterns such as `ending 1234`, `XX1234`, `****1234`, or `..1234`.
- Bank name from a fixed list (HDFC, ICICI, SBI, Axis, Kotak, Yes Bank, IndusInd, IDFC First,
  Bank of Baroda, Canara, Union Bank, Federal Bank, RBL, HSBC, Citi, Chase, Revolut, Amex, DBS,
  AU Bank).
- Output is `BANK **** 1234`, `Account **** 1234`, the bank name, or the source app label.

### Autopay and billing cycle

- Autopay requires a debit and a word such as `autopay`, `auto-pay`, `e-mandate`, `mandate`,
  `recurring`, `subscription`, `renewal`, `renewed`, or `standing instruction`.
- Cycle detection: yearly words (`annual`, `per year`, `/yr`), quarterly words, weekly words;
  default monthly.

### Category

Keyword matching over the full text plus the merchant name. First match wins in this order:
Entertainment, Food, Travel, Bills, Health, Subscription, Shopping, then `Other`.

---

## Optional on-device LLM fallback

The app works without the model. The fallback is used only when:

1. The regex parser returns no transaction, and
2. The text contains a valid amount, and
3. The text contains a payment cue, and
4. The model file is present.

### Model

| Item | Value |
|---|---|
| Model | Qwen2.5-0.5B-Instruct, q8, multi-prefill, ekv1280 |
| Format | MediaPipe `.task` |
| Size | 547 MB |
| File | `filesDir/models/qwen2.5-0.5b-instruct.task` |
| Source | `https://huggingface.co/litert-community/Qwen2.5-0.5B-Instruct/resolve/main/Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task?download=true` |
| Runtime | `com.google.mediapipe:tasks-genai:0.10.35` |

### Download manager

`LlmModelManager` exposes a `StateFlow` with `Missing`, `Downloading`, `Ready`, and `Failed`.
Download behavior:

- Logged-in-free HTTPS download with redirects and a custom User-Agent.
- 30-second connect timeout, 60-second read timeout.
- 256 KB buffer, progress updates every 250 ms.
- Minimum 100 MB and maximum 700 MB size bounds.
- Free-space check before starting.
- Writes to `.part`, validates the ZIP/`PK` magic and minimum size, then renames.
- Cancellation and failure delete the partial file.
- Remove deletes both the model and any partial file.

### Inference

- Lazy engine creation; the engine is shared and guarded by a `Mutex`.
- A new `LlmInferenceSession` per request with `topK = 1`, `temperature = 0`, and
  `randomSeed = 0` for deterministic output.
- Sequence limit: 896 tokens. Message text: 500 characters. Response parsing: 2,000 characters.
- Prompt uses the Qwen chat template (`<|im_start|>system` / `user` / `assistant`) and includes
  positive and negative examples.
- The model returns one JSON object:

```json
{
  "is_transaction": true,
  "merchant": "Myntra",
  "category": "Shopping",
  "type": "DEBIT",
  "autopay": false,
  "cycle": "MONTHLY"
}
```

- `LlmResponseParser` extracts the first `{...}`, validates booleans leniently, requires
  `DEBIT` or `CREDIT`, allowlists categories and cycles, and forces autopay to debit-only.
- After a successful model result, the regex parser remains authoritative for merchant and
  category when it recognizes them. The LLM fills gaps only.
- The amount and currency always come from the regex, never from the model.
- Engine unloads after 60 seconds of disuse. A failed load is retried after 60 seconds.

### Privacy

Notification text is sent only to the local MediaPipe runtime. Network access is used only to
download the model. There is no inference server and no telemetry.

Known limits:

- The model is small; it can still mislabel merchants or categories.
- The payment-cue gate intentionally ignores alerts without a payment verb, such as a bare
  `INR 500 to John`.
- The runtime is native code. A native crash would terminate the app process; no such crash was
  observed during device verification.

---

## User interface

Two bottom-navigation tabs:

### Transactions

- Search across merchant, category, and account source.
- Category chips derived from stored transactions.
- Date-range chips: All time, This month, Last 30 days.
- Day-group headers with daily debit totals.
- Transaction cards with merchant, category, account, signed amount, date, and an `AUTOPAY` badge.
- Floating action button for manual add.
- Editor dialog for merchant, amount, debit/credit, category, and account/source. New manual
  rows default to `USD` and `Cash`.
- Delete action when editing an existing row.

### Autopays

- Monthly recurring commitment summary.
- "Due in 7 days" count and scheduled value.
- Cards with service, category, cycle, amount, next expected date, last charge, status badge.
- Expandable last-20-charge history.
- Cancel and reactivate actions.

### App chrome

- Top bar with an `AI` action that opens the model manager dialog.
- Privacy banner: parsing stays on-device; the optional model downloads once.
- Notification-access card appears when the listener permission is missing and links to system
  settings.
- Light and dark Material 3 color schemes.

---

## Project structure

```text
ExpenseTracker/
├── build.gradle.kts                 Root plugin versions
├── settings.gradle.kts              Repositories and :app include
├── gradle.properties                AndroidX, JVM args
├── gradlew / gradlew.bat            Gradle wrapper
├── gradle/wrapper/                  Gradle 8.14.3 wrapper
└── app/
    ├── build.gradle.kts             App config and dependencies
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/expensetracker/app/
        │   │   ├── ExpenseTrackerApp.kt
        │   │   ├── data/
        │   │   │   ├── AppDatabase.kt
        │   │   │   ├── TransactionEntity.kt
        │   │   │   ├── TransactionDao.kt
        │   │   │   ├── AutopayEntity.kt
        │   │   │   ├── AutopayDao.kt
        │   │   │   ├── ExpenseRepository.kt
        │   │   │   └── llm/
        │   │   │       ├── LlmModelManager.kt
        │   │   │       ├── LlmFallbackParser.kt
        │   │   │       └── LlmResponseParser.kt
        │   │   ├── di/AppModule.kt
        │   │   ├── parser/
        │   │   │   ├── NotificationParser.kt
        │   │   │   ├── NotificationSources.kt
        │   │   │   ├── ParsedNotification.kt
        │   │   │   └── BillingCycle.kt
        │   │   ├── service/TransactionNotificationListener.kt
        │   │   ├── ui/
        │   │   │   ├── MainActivity.kt
        │   │   │   ├── MainScreen.kt
        │   │   │   ├── TransactionsScreen.kt
        │   │   │   ├── TransactionsViewModel.kt
        │   │   │   ├── TransactionEditorDialog.kt
        │   │   │   ├── AutopaysScreen.kt
        │   │   │   ├── AutopaysViewModel.kt
        │   │   │   ├── LlmViewModel.kt
        │   │   │   ├── NotificationAccess.kt
        │   │   │   ├── Formatters.kt
        │   │   │   └── theme/Theme.kt
        │   │   └── worker/AutopayAuditWorker.kt
        │   └── res/                 Strings, themes, launcher drawable
        └── test/java/com/expensetracker/app/
            ├── parser/NotificationParserTest.kt
            └── data/llm/LlmResponseParserTest.kt
```

---

## Build and run

### Requirements

- JDK 17 or newer. Android Studio's bundled JBR works.
- Android SDK 36 installed.
- A device or emulator with API 26+ (the LLM requires a physical device with enough RAM).
- `adb` available for install and optional device tests.

### Commands (Windows PowerShell)

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
cd <repo>
.\gradlew.bat testDebugUnitTest assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.expensetracker.app/.ui.MainActivity
```

On macOS/Linux, replace `.\gradlew.bat` with `./gradlew`.

### First run

1. Open the app.
2. Tap **Enable** on the notification-access card.
3. Enable **Expense Tracker notifications** in system settings.
4. Return to the app; the card disappears after the permission is detected.
5. Optional: tap **AI** and download the 547 MB model.

Release builds currently do not enable minification and have no signing configuration. Add a
release keystore before publishing.

### Dependency versions

| Component | Version |
|---|---|
| AGP | 8.13.2 |
| Kotlin | 2.2.21 |
| KSP | 2.2.21-2.0.5 |
| Gradle | 8.14.3 |
| Hilt | 2.57.2 |
| Room | 2.8.3 |
| Compose BOM | 2025.10.01 |
| Navigation Compose | 2.9.6 |
| WorkManager | 2.10.5 |
| MediaPipe GenAI | 0.10.35 |
| JUnit | 4.13.2 |

---

## Tests and verification

### Unit tests

`NotificationParserTest` covers:

- Debit parsing with merchant and account.
- Credit parsing with bank and merchant.
- Card debit and category detection.
- Monthly and yearly autopay detection.
- OTP rejection, including OTP with "payment of" wording.
- Payment-cue separation between real debits and order updates.
- Long-text handling.
- Amount extraction without transaction keywords.
- Billing-cycle date math.

`LlmResponseParserTest` covers:

- Valid debit keeps the regex amount.
- Non-transaction rejection.
- Malformed JSON and invalid type rejection.
- Fenced JSON and unknown-category fallback.
- Autopay debit-only enforcement and cycle validation.
- Blank merchant becomes `Unknown`.

Run:

```powershell
.\gradlew.bat testDebugUnitTest
```

### Device verification snapshot

The debug build was exercised on a OnePlus CPH2707 (Android 16 / API 36):

- A Swiggy debit notification produced `Swiggy / INR 450 / Food / HDFC **** 1234 / DEBIT`.
- An unusual `done at Croma` alert used the LLM and produced `Croma / INR 1,499 / Shopping`.
- A Myntra alert used the LLM and produced the correct merchant and amount.
- "Out for delivery" and "Order confirmed" messages were skipped by the payment-cue gate.
- The AI dialog reported the model as **Ready** after a manual model push.
- Test rows were removed after verification.

The in-app download flow was not exercised end-to-end in that environment; the model was pushed
manually with adb. The normal user path is the **AI > Download** dialog.

### adb notification testing

Debug builds accept shell notifications:

```powershell
adb shell 'cmd notification post -t HDFC test1 \"Rs. 450 debited from A/c ****1234 to Swiggy via UPI\"'
```

Notification IDs are derived from the tag. Space posts at least 15 seconds apart; Android can
throttle or suppress rapid consecutive notifications from the same package.

---

## Troubleshooting

### No transactions appear

- Confirm notification access is enabled for **Expense Tracker notifications**.
- Confirm the source package is allowlisted or matches a package-name hint.
- Some OEMs kill listener services aggressively. Disable battery optimization for the app.
- Notifications marked ongoing or group summaries are intentionally ignored.
- OTPs may be redacted from untrusted listeners by Android/OEM software.
- Check `adb logcat -s LlmFallback:D` for model fallback and log filtering.

### Model stays missing or fails

- Confirm the model file is at `filesDir/models/qwen2.5-0.5b-instruct.task` and is at least
  100 MB.
- Ensure at least 547 MB of free storage.
- A failed model load is retried after 60 seconds.
- For a failed download, tap **Download** again; the partial file is removed automatically.
- The model is a `.task` ZIP; a truncated file fails the `PK` magic check.

### Duplicate payment was ignored

The repository suppresses the same merchant and amount within 60 seconds. Add the second payment
manually if it is genuinely separate.

### Wrong merchant or category

The deterministic regex remains authoritative when it finds a merchant or category. Edit the
transaction, or extend `CATEGORY_KEYWORDS` / merchant stop words in `NotificationParser`.
The LLM is intentionally not trusted for amounts.

---

## Privacy and security

- Transaction data stays in the app's private Room database.
- The only network operation is the optional model download. `INTERNET` is declared for that.
- No bank credentials, account logins, analytics, ads, or remote parsing.
- Notification text is stored raw, truncated to 500 characters, in `rawNotificationText`.
- The database is not encrypted at rest; it relies on the Android app sandbox.
- `android:allowBackup="true"` is currently enabled. Android may back the database up to the
  user's cloud backup. Set it to `false` for a strict local-only guarantee.
- The notification listener is not exported and is protected by
  `BIND_NOTIFICATION_LISTENER_SERVICE`.
- `com.android.shell` is accepted as a source only in debug builds.
- Do not commit `local.properties`, keystores, or signing secrets. They are ignored.

---

## Known limitations

- Single-currency display: the UI uses the first stored transaction's currency; amounts are not
  converted.
- The parser is English- and India-centric. New banks or unusual phrasing may need allowlist or
  keyword updates.
- No CSV/PDF export, import, budgets, or charts.
- No cloud sync and no multi-device support.
- The LLM is opt-in, large (547 MB), and not perfectly accurate; a payment cue is required.
- The 60-second dedupe window is simple and can drop legitimate repeated payments.
- Autopay staleness is approximate: a daily worker applies a 7-day grace period.
- Release signing, shrinking, and production hardening are not configured.
- Only JVM unit tests exist; there are no instrumentation or Compose UI tests.
- Notification-access permission must be granted manually; there is no in-app permission
  rationale flow beyond the banner.

---

## Key files

| File | Purpose |
|---|---|
| `app/src/main/AndroidManifest.xml` | Permissions, listener service, WorkManager init removal |
| `service/TransactionNotificationListener.kt` | Notification capture and Toast feedback |
| `data/ExpenseRepository.kt` | Parse/fallback, dedupe, autopay, persistence |
| `parser/NotificationParser.kt` | Deterministic text parser and guard rails |
| `parser/NotificationSources.kt` | Allowlist, hints, debug shell source |
| `data/llm/LlmModelManager.kt` | Model download, validation, state, delete |
| `data/llm/LlmFallbackParser.kt` | Local inference, prompt, deterministic session |
| `data/llm/LlmResponseParser.kt` | JSON extraction and validation |
| `data/AppDatabase.kt` | Room database definition |
| `data/TransactionEntity.kt` / `AutopayEntity.kt` | Persistence schema |
| `ui/MainScreen.kt` | App shell, tabs, privacy banner, model dialog |
| `ui/TransactionsScreen.kt` | List, search, filters, grouping, editor entry |
| `ui/AutopaysScreen.kt` | Autopay summary, status, history, actions |
| `worker/AutopayAuditWorker.kt` | Daily stale-mandate audit |
| `app/build.gradle.kts` | SDK levels, dependencies, Compose/BuildConfig |
| `README.md` | This document |
