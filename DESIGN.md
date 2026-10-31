# DESIGN.md: Android App UI/UX Spec

**Project:** Automated Expense & Subscription Tracker (Android, Kotlin, Jetpack Compose, Material 3)
**Repo:** https://github.com/WARlord05/HF (`ExpenseTracker/`)
**Purpose of this file:** A design brief for building an aesthetic, human-crafted-looking mobile UI. Hand this to anyone (or any tool) building the screens so the result does not look like a generic template.

---

## 1. Design Direction: "The Pocket Ledger"

The app should feel like a well-kept paper ledger translated to a phone: calm, warm, precise. It is a tool people open many times a day for two seconds, so it must be quiet and fast to read.

**Keywords:** warm paper, ink, tabular numbers, hairline rules, generous whitespace, editorial.

**What it is not:** a neon fintech dashboard, a gradient-heavy "AI" product, or a stock Material 3 demo with default purple.

### 1.1 Anti-Template Rules (what makes it look non-AI, non-generic)

| Avoid | Do instead |
|---|---|
| Purple/blue-to-pink gradients | Flat, warm neutral surfaces with one accent colour |
| Glassmorphism, glows, blurred blobs | Solid surfaces separated by 1dp hairlines |
| Sparkle / magic-wand / robot icons | Plain, functional icons; text labels where clearer |
| Emoji as category icons | Custom monoline icons or simple lettered monograms |
| Default Material purple (`#6750A4`) | A custom palette (below); never ship the default seed |
| Every card with big rounded corners and drop shadow | Mostly flat rows with dividers; cards only where grouping is needed |
| Centered hero text and "Welcome back, User!" copy | Left-aligned, plain, specific copy |
| Generic stock illustrations | Typography-led empty states, no mascots |
| Pie charts with 8 rainbow slices | Single-hue bars and sparklines; colour only for meaning |
| Marketing words ("Supercharge", "Smart", "AI-powered") | Say what it does: "3 debits this week" |

---

## 2. Colour System

One accent, a warm neutral scale, and semantic colours used sparingly. Define these as a custom `ColorScheme`; do not use dynamic colour as the primary identity (offer it as an optional setting at most).

### 2.1 Light theme ("Paper")

| Token | Hex | Use |
|---|---|---|
| `background` | `#F6F2EA` | App background (warm paper) |
| `surface` | `#FBF8F2` | Cards, sheets, nav bar |
| `surfaceVariant` | `#EDE7DA` | Chips, input fields, pressed states |
| `outline` | `#D9D1C1` | Hairline dividers (1dp) |
| `onBackground` | `#1F1B16` | Primary text (ink) |
| `onSurfaceMuted` | `#6B6358` | Secondary text, captions |
| `accent` | `#2F5D50` | Primary actions, active tab, links (deep green) |
| `onAccent` | `#FBF8F2` | Text on accent |
| `debit` | `#1F1B16` | Debit amounts (neutral ink, not red, to avoid alarm) |
| `credit` | `#2F7A4F` | Credit amounts |
| `warning` | `#B7791F` | "Upcoming" badge, nearing-limit states |
| `danger` | `#A63D2F` | Stale/cancelled, destructive actions |

### 2.2 Dark theme ("Ink")

| Token | Hex |
|---|---|
| `background` | `#141311` |
| `surface` | `#1C1A17` |
| `surfaceVariant` | `#27241F` |
| `outline` | `#3A362F` |
| `onBackground` | `#EDE8DD` |
| `onSurfaceMuted` | `#9A9284` |
| `accent` | `#7FB5A3` |
| `onAccent` | `#10201B` |
| `credit` | `#6FBF8E` |
| `warning` | `#D9A441` |
| `danger` | `#D9786A` |

Use off-black and off-white only; never pure `#000` or `#FFF`.

### 2.3 Category colours

Categories get a **muted tint on a monogram circle**, not saturated icons. Keep chroma low so a long list stays calm.

| Category | Tint |
|---|---|
| Food | `#C98B5A` |
| Shopping | `#8A7BA8` |
| Travel | `#5E8FA8` |
| Utility | `#B39B4D` |
| Subscriptions | `#2F5D50` |
| Other | `#8C867B` |

---

## 3. Typography

Pair a characterful serif for a few headline moments with a clean sans for UI and a tabular face for money.

| Role | Font | Notes |
|---|---|---|
| Display / screen titles / totals | **Newsreader** (or Fraunces) | Used sparingly: screen title and big totals only |
| UI text, labels, body | **Inter** (or Instrument Sans) | 400 / 500 / 600 |
| Amounts, dates in lists | **Inter with `tnum`** or **JetBrains Mono** | Tabular figures so digits align in columns |

Load via Compose Google Fonts provider (`androidx.compose.ui:ui-text-google-fonts`) and ship a local fallback in `res/font/`.

### Type scale (sp)

| Style | Size / line height | Weight |
|---|---|---|
| Display total | 40 / 44 | Newsreader 500 |
| Screen title | 28 / 34 | Newsreader 500 |
| Section label | 12 / 16, +8% tracking, uppercase | Inter 600, muted |
| Body | 15 / 22 | Inter 400 |
| Row title (merchant) | 16 / 22 | Inter 500 |
| Row amount | 16 / 22 | Tabular 500 |
| Caption | 12 / 16 | Inter 400, muted |

Rules:
- Always set `fontFeatureSettings = "tnum"` on any numeric text.
- Right-align amounts; keep the minus sign and currency symbol consistent (`−₹1,249.00`, using a true minus `−`, not a hyphen).
- Never use more than two font families on one screen.

---

## 4. Layout, Shape, Spacing

- **Spacing unit:** 4dp grid. Common steps: 4, 8, 12, 16, 24, 32.
- **Screen gutters:** 20dp horizontal.
- **Corner radius:** 12dp for cards and inputs, 999dp for chips and badges, 0dp for list rows. Pick these and stay consistent.
- **Elevation:** 0dp everywhere. Separate with hairlines (`outline`, 1dp) and tonal difference, not shadows. The only exception is the bottom sheet scrim.
- **Touch targets:** minimum 48dp.
- **List rows:** 64dp tall, left monogram (40dp), title and caption stacked, amount right-aligned.
- **Safe areas:** enable edge-to-edge; respect system bar insets with `WindowInsets`.

---

## 5. Screen-by-Screen Spec

### 5.1 App shell
- Top: screen title in Newsreader, left-aligned, no centred app bar. Optional small search icon and overflow on the right.
- Segmented switch under the title: **Transactions | Autopays** (text only, underline indicator in `accent`, no pill fill).
- Bottom bar (optional, 3 items max): **Home, Insights, Settings**. Text labels always visible; no filled indicator pill (use a 2dp accent line above the active item).
- Slim privacy line at the foot of Home: "Stored on this device only." Small, muted, no lock-shield illustration.

### 5.2 View A: Transactions
1. **Month header:** month name (Newsreader), then one muted line: `Spent ₹42,310 · Received ₹60,000`.
2. **Filter row:** horizontally scrolling chips (Category, Account, Date, Mode). Selected chip = accent outline plus tinted fill.
3. **Timeline:** sticky day headers in the section-label style (`TODAY`, `YESTERDAY`, `28 OCT, TUE`) with the day's net total on the right.
4. **Row:** monogram circle in category tint, merchant (title), `Category · HDFC Card ••4091` (caption), amount right-aligned. Credits in `credit` colour with a leading `+`.
5. **Autopay indicator:** a small "repeat" glyph beside the caption when `isAutopay = true`; tapping the row opens detail.
6. **Detail sheet:** large amount (Display), merchant, date/time, account, payment mode, category (editable), raw notification text in a collapsed "Source" section, **Edit** and **Delete** actions.
7. **Add cash transaction:** floating action button replaced by a text button "Add entry" in the top bar; opens a bottom sheet with a numeric keypad-first form (amount, merchant, category, date).
8. **Empty state:** one line of text ("Nothing here yet. New payments appear as they arrive.") plus a button to open notification-access settings. No illustration.

### 5.3 View B: Autopays & Subscriptions
1. **Summary block** at top (flat, hairline-bordered, not a coloured hero card):
   - `Monthly commitment` label, then total in Display type.
   - Two small stats beside it: `Upcoming 5 debits · ₹6,840` and `Active 12`.
2. **Group by status:** `UPCOMING (next 7 days)`, `ACTIVE`, `STALE / CANCELLED` as section labels; cancelled items at 55% opacity.
3. **Autopay row:** monogram, service name, `Monthly · last ₹649 on 3 Oct`, right side shows `Next 3 Nov` and a status badge.
4. **Status badges** (pill, text-only, tinted background):
   - `Active` → accent tint
   - `Upcoming` → warning tint
   - `Stale` / `Cancelled` → danger tint, muted text
5. **Detail screen:** history of debits as a simple list with a thin sparkline of amount over time; actions: **Mark cancelled**, **Set reminder**, **Edit frequency**.
6. **Detection transparency:** if an autopay was auto-flagged, show a small caption "Detected from 3 similar debits" with an "Not a subscription" undo link.

### 5.4 Insights (optional, post-MVP)
- Month-over-month bar chart in a single hue (`accent` at 3 opacities for current / previous / older).
- Top merchants as a ranked list with proportional thin bars, not a pie chart.

### 5.5 Settings
- Notification-access status with a clear on/off row and a button to system settings.
- Monitored apps list (toggle per source app).
- Theme: System / Light / Dark. Currency. Export CSV. Backup (roadmap items shown as disabled with "Coming soon").

---

## 6. Components Checklist

Build these as reusable Composables in a `designsystem` module:

- [ ] `LedgerTheme` (colour, type, shapes, spacing tokens)
- [ ] `AmountText` (tabular, sign-aware, colour-aware)
- [ ] `Monogram` (category-tinted circle with 1–2 letters)
- [ ] `TransactionRow`, `AutopayRow`
- [ ] `SectionLabel` (uppercase, tracked)
- [ ] `StatusBadge`
- [ ] `HairlineDivider`
- [ ] `FilterChipRow`
- [ ] `SummaryBlock`
- [ ] `EntrySheet` (add/edit)
- [ ] `EmptyState` (text and one action)

---

## 7. Motion & Feedback

Keep motion functional and short.

- **Durations:** 150–250ms; easing `FastOutSlowIn`. No bouncy springs, no parallax.
- **New transaction arrives:** the row slides in at the top of the list with a 600ms background fade from `surfaceVariant` to `background`. No confetti, no toast spam. Optional single-line snackbar: "Added ₹349 · Swiggy".
- **Status change:** badge cross-fades.
- **Numbers:** count-up animation only on the Autopay monthly total at first load, under 400ms.
- Respect the system "Remove animations" setting.

---

## 8. Copy & Voice

Plain, specific, calm. Short sentences. Sentence case. No exclamation marks, no emoji.

| Instead of | Write |
|---|---|
| "Oops! Something went wrong 😢" | "Couldn't read that notification." |
| "Smart insights for you" | "This month" |
| "Subscription detected! 🎉" | "Looks like a recurring payment." |
| "Unlock premium" | (no upsell in v1) |

---

## 9. Accessibility

- Contrast: body text at least 4.5:1, large text 3:1 in both themes. Verify muted text on `surfaceVariant`.
- Never rely on colour alone: credits carry a `+`, statuses carry text.
- Support font scale up to 200% (rows grow; do not clip).
- Content descriptions on all icon-only buttons; amounts read as "debit, one thousand two hundred forty-nine rupees".
- TalkBack order follows visual order; day headers are marked as headings.

---

## 10. Implementation Notes (Compose)

```kotlin
// Theme.kt  (sketch)
private val PaperColors = lightColorScheme(
    primary = Color(0xFF2F5D50),
    onPrimary = Color(0xFFFBF8F2),
    background = Color(0xFFF6F2EA),
    surface = Color(0xFFFBF8F2),
    surfaceVariant = Color(0xFFEDE7DA),
    outline = Color(0xFFD9D1C1),
    onBackground = Color(0xFF1F1B16),
    onSurface = Color(0xFF1F1B16),
    error = Color(0xFFA63D2F),
)

@Immutable
data class LedgerExtras(
    val credit: Color,
    val warning: Color,
    val mutedText: Color,
)

val LocalLedgerExtras = staticCompositionLocalOf {
    LedgerExtras(Color(0xFF2F7A4F), Color(0xFFB7791F), Color(0xFF6B6358))
}

// AmountText.kt (sketch)
@Composable
fun AmountText(amount: Double, isCredit: Boolean, modifier: Modifier = Modifier) {
    val extras = LocalLedgerExtras.current
    val sign = if (isCredit) "+" else "−"
    Text(
        text = "$sign₹%,.2f".format(amount),
        color = if (isCredit) extras.credit else MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
        modifier = modifier,
    )
}
```

- Use `LazyColumn` with `stickyHeader` for day groups; provide stable `key = { it.id }`.
- Keep UI state in ViewModels exposed as `StateFlow<UiState>`; Composables stay stateless where possible.
- Currency and locale: format via `NumberFormat.getCurrencyInstance(locale)`; do not hardcode the symbol. The schema's `currency` default should follow the device locale (INR for India) rather than `"USD"`.

---

## 11. Suggested Folder Structure

```
app/src/main/java/.../
├── designsystem/      # LedgerTheme, tokens, shared components
├── feature/
│   ├── transactions/  # list, detail, add/edit sheet
│   ├── autopays/      # summary, list, detail
│   ├── insights/
│   └── settings/
├── domain/            # use cases, models
├── data/              # Room, repositories, parser
└── service/           # NotificationListenerService
```

---

## 12. Definition of "Done" for the UI

- [ ] Both themes implemented and checked on a small (360dp) and large (412dp+) device
- [ ] No default Material purple anywhere
- [ ] No shadows, gradients, or emoji in the UI
- [ ] All amounts use tabular figures and align on the right edge
- [ ] Empty, loading, and error states exist for both views
- [ ] TalkBack pass completed
- [ ] Screenshots taken in light and dark for the README

---

## 13. Open Questions for the Team

1. Is the primary market India (INR, UPI-first)? That affects default currency, monogram logic, and parser patterns.
2. Should dynamic (Material You) colour be offered as an optional theme?
3. Is a bottom navigation bar wanted, or just the two-tab switch on a single Home screen?
4. App name and icon: the working name is "Expense & Subscription Tracker"; a short, non-generic name would help the visual identity.

> **Note:** your message said "for anthropic". I read that as "for Android". If you meant something else, tell me and I'll adjust.
