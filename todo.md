# UI Redesign & Implementation Progress: "The Pocket Ledger"

Based on [DESIGN.md](file:///c:/Users/Sahil%20Patil/OneDrive/Documents/hf/HF/DESIGN.md) specification for the Android automated expense and subscription tracker.

---

## 1. Design System & Tokens
- [x] **Colour System** (`ui/theme/Color.kt`):
  - [x] Light theme ("Paper") `#F6F2EA` with deep green accent `#2F5D50`
  - [x] Dark theme ("Ink") `#141311` with soft accent `#7FB5A3`
  - [x] Semantic colours: credit `#2F7A4F`, warning `#B7791F`, danger `#A63D2F`
  - [x] Debit amounts displayed in neutral ink (`onBackground`) to avoid panic
  - [x] Low-chroma Category tints (Food, Shopping, Travel, Utility, Subscriptions, Other, Health, Entertainment)
  - [x] `LedgerExtras` composition local
- [x] **Typography** (`ui/theme/Type.kt`):
  - [x] Editorial serif for headline moments and totals (Newsreader / Serif)
  - [x] Clean sans-serif for UI and body text
  - [x] Tabular figures (`fontFeatureSettings = "tnum"`) on numeric and amount text
  - [x] Uppercase, tracked section labels (`+8% tracking`)
  - [x] True Unicode minus `−` (U+2212)
- [x] **Shapes & Geometry** (`ui/theme/Shape.kt`):
  - [x] 12dp rounded corners for cards, inputs, and sheets
  - [x] 999dp pill radius for chips and status badges
  - [x] 0dp for list rows (flat rows separated by 1dp hairlines)
  - [x] 0dp elevation everywhere with hairlines replacing drop shadows
- [x] **Theme Wiring** (`ui/theme/Theme.kt`):
  - [x] `LedgerTheme` Composable with automatic dark/light theme switching

---

## 2. Reusable Design System Components
- [x] **AmountText** (`ui/components/AmountText.kt`):
  - [x] Tabular figures (`tnum`) for column alignment
  - [x] Sign-aware: `+` for credit, true `−` for debit
  - [x] Color-aware: credit green, neutral ink for debit
- [x] **Monogram** (`ui/components/Monogram.kt`):
  - [x] 40dp circle with category tint (16% opacity)
  - [x] 1–2 letter clean uppercase initials
  - [x] No generic stock icons or emoji
- [x] **SectionLabel** (`ui/components/SectionLabel.kt`):
  - [x] Uppercase text with +8% letter-spacing and muted ink colour
- [x] **StatusBadge** (`ui/components/StatusBadge.kt`):
  - [x] Pill shape (999dp), text only, tinted background
  - [x] Active (accent tint), Upcoming (warning tint), Cancelled/Stale (danger tint)
- [x] **HairlineDivider** (`ui/components/HairlineDivider.kt`):
  - [x] 1dp divider using `outline` token with 0 elevation
- [x] **SummaryBlock** (`ui/components/SummaryBlock.kt`):
  - [x] Flat, hairline-bordered surface (0dp elevation)
  - [x] `MONTHLY COMMITMENT` section label + 40sp Display total
  - [x] Stats: upcoming debits with sum, active mandates count
- [x] **EmptyState** (`ui/components/EmptyState.kt`):
  - [x] Typography-led empty state, no mascots or clipart
- [x] **EntrySheet** (`ui/components/EntrySheet.kt`):
  - [x] Modal bottom sheet with numeric keypad-first layout
  - [x] Segmented debit/credit toggle
  - [x] Category chips and account source input
- [x] **TransactionDetailSheet** (`ui/components/TransactionDetailSheet.kt`):
  - [x] Large Display amount, merchant title, category, timestamp
  - [x] Collapsible "Notification source" disclosure for raw SMS/notification text
  - [x] Edit and Delete actions

---

## 3. Screens & Shell
- [x] **App Shell** (`ui/MainScreen.kt`):
  - [x] Left-aligned "Pocket Ledger" title in Newsreader Serif (no centered toolbar)
  - [x] Top bar "Add entry" action text button
  - [x] Underline segmented switch: **Transactions | Autopays** (text-only, 2dp accent underline, no pill fill)
  - [x] Quiet hairline warning banner for notification access
  - [x] Slim privacy footer: "Stored on this device only · No servers, no bank logins"
- [x] **Transactions View** (`ui/TransactionsScreen.kt`):
  - [x] Month header with month name + `Spent ₹... · Received ₹...`
  - [x] Search input + Date range chips ("This month", "Last 30 days", "All time") + Category filter chips
  - [x] Sticky day headers (`TODAY`, `YESTERDAY`, `28 OCT, TUE`) with day net totals
  - [x] 64dp flat list rows with category monogram, merchant, category & account source, autopay indicator, and tabular amount
  - [x] Tap row to open `TransactionDetailSheet`
- [x] **Autopays View** (`ui/AutopaysScreen.kt`):
  - [x] `SummaryBlock` commitment overview
  - [x] Grouped by status: `UPCOMING (next 7 days)`, `ACTIVE`, `STALE / CANCELLED`
  - [x] 55% opacity for cancelled/stale mandates
  - [x] Tap to expand debit history with "Mark cancelled" / "Reactivate" actions
