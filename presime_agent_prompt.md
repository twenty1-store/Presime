# Presime — Antigravity Agent Build Prompt

## Project overview

Build **Presime**, a productivity timer Android app. The app helps users manage their time through a clock, stopwatch, Pomodoro timer, and an intelligent app-usage guard system. Clean, minimal, editorial aesthetic.

**Platform:** Android (minSdk 26, targetSdk 35)
**Language:** Kotlin
**UI:** Jetpack Compose (Material3 base, heavily customized)
**Architecture:** MVVM + Repository pattern
**DI:** Hilt
**Local DB:** Room
**Navigation:** Compose Navigation (single-activity)
**Build system:** Gradle (Kotlin DSL)

---

## Design system

### Visual direction
Inspired by editorial minimalism. Large monochrome numerals, generous whitespace, thin strokes, zero decorative noise. Think premium utility — not gamified, not corporate.

### Color tokens (implement as Compose theme with light + dark MaterialTheme)

**Light mode:**
```
Background:     #F0EEE9   (warm off-white)
Surface:        #E8E6E0   (slightly deeper warm gray)
Surface variant:#D8D6CF
On background:  #1A1918   (near-black, warm tint)
On surface:     #1A1918
Muted text:     #9A9890   (inactive labels, ghost numerals)
Ghost text:     #C8C6BF   (large background numerals — inactive)
Border:         #D0CEC7   (thin strokes, dividers)
Accent:         #1A1918   (same as text — monochrome accent)
Error:          #C0392B
```

**Dark mode:**
```
Background:     #141312
Surface:        #1E1D1B
Surface variant:#282724
On background:  #F0EEE9
On surface:     #F0EEE9
Muted text:     #706E68
Ghost text:     #2E2D2A   (large background numerals — inactive)
Border:         #2A2927
Accent:         #F0EEE9
Error:          #E74C3C
```

### Typography
Use **DM Sans** (Google Fonts) as the primary typeface.

```kotlin
// Type scale
Display  → DM Sans, 72sp, weight 700, letterSpacing -0.03em  // active big number
Heading1 → DM Sans, 32sp, weight 600
Heading2 → DM Sans, 20sp, weight 600
Body     → DM Sans, 15sp, weight 400
Caption  → DM Sans, 11sp, weight 500, letterSpacing 0.08em, UPPERCASE
Ghost    → DM Sans, 96sp, weight 700, alpha 0.12  // background watermark numbers
```

Add DM Sans to the project via `res/font/` or use `downloadable fonts` in XML.

### Spacing
Use 8dp base unit. All padding/margin in multiples of 4 or 8.

### Shape
- Cards: `RoundedCornerShape(16.dp)`
- Chips/pills: `RoundedCornerShape(50)`
- Bottom sheet: `RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)`

---

## App structure

### Navigation graph — 5 destinations

```
NavGraph
├── HomeScreen          (start destination)
├── StopwatchScreen
├── TimerScreen         (countdown + Pomodoro mode toggle)
├── AppGuardScreen      (app usage tracking & limits)
└── StatsScreen         (session history with labels)
```

Bottom navigation bar with 5 tabs. Icons: use Material Symbols Outlined (Rounded style).
- Home → `home`
- Stopwatch → `timer`
- Timer → `hourglass_empty`
- Guard → `shield`
- Stats → `bar_chart`

---

## Screen specifications

### 1. HomeScreen

**Layout:**
- Top: App name "Presime" in Caption style, top-left. Settings icon top-right (opens system settings shortcut for permissions).
- Center: Large analog clock drawn with Canvas API.
  - Clock face: no numbers, thin minimal tick marks only (12 major, 48 minor)
  - Hour/minute hands: 2dp stroke, `On background` color
  - Seconds hand: 1dp stroke, lighter weight
  - Center dot: 6dp filled circle
- Below clock: Digital time in Heading1 style — `HH:mm` format
- Below digital time: Date in Caption style — `EEE, dd MMM yyyy`
- Bottom section: Today's focus summary card — show `Today: X h Ym focused` if any sessions logged, else show motivational line like "Start your first session."

**Implementation:**
```kotlin
// Use Canvas in a Composable, update every second via LaunchedEffect + snapshotFlow
// Get current time with Calendar.getInstance()
// Draw arc, ticks, hands using drawLine / drawArc / drawCircle
```

---

### 2. StopwatchScreen

**Layout:**
- Top: Screen title "Stopwatch" in Caption
- Center: Large elapsed time display — `HH:MM:SS.ss` in Display typography
  - Show ghost/watermark number behind the time (purely decorative, low-alpha)
- Lap split list below (lazy column, max visible 5, scrollable)
  - Each lap row: `Lap 01   +00:12.34   00:45.67` — left number, center delta, right total
  - Most recent lap highlighted slightly
- Bottom controls:
  - When stopped: Single large circular "Start" button (48dp radius)
  - When running: "Lap" (left, secondary) + "Pause" (right, primary)
  - When paused: "Reset" (left, destructive) + "Resume" (right, primary)
- "Export laps" text button appears when ≥1 lap exists → exports CSV to Downloads

**State management:**
```kotlin
// StopwatchViewModel holds StateFlow<StopwatchState>
data class StopwatchState(
    val elapsedMs: Long = 0,
    val isRunning: Boolean = false,
    val laps: List<LapEntry> = emptyList()
)
```

**Background persistence:**
Run a `ForegroundService` (StopwatchService) that posts a persistent notification showing elapsed time. ViewModel binds to service via ServiceConnection. Service survives app backgrounding and screen lock.

**Notification:** "Presime • Stopwatch running — 00:12:34" with a Pause action button.

---

### 3. TimerScreen

**Layout:**
- Top: Two segmented tabs — `Countdown` | `Pomodoro`

**Countdown mode:**
- Large circular progress ring drawn with Canvas
  - Ring: thin (6dp stroke), fills clockwise as time runs down
  - Center: remaining time in Display typography — `MM:SS`
- Preset quick-select chips below ring: `5m  10m  15m  25m  30m  45m  60m` (horizontal scroll)
- Custom input: tapping the center time opens a number picker dialog/bottom sheet
- Bottom controls: same Start/Pause/Reset pattern as Stopwatch

**Pomodoro mode:**
- Shows current phase label: `Focus` / `Short Break` / `Long Break` in Heading2
- Shows session count: e.g. `Session 2 of 4` in Caption
- Same circular ring, but segments/phases visualized
- Default durations: Focus 25m, Short break 5m, Long break 15m (after 4 sessions)
- Settings icon (top right) to configure durations
- On phase complete: play a subtle bell sound via `SoundPool`, show a notification

**Session labeling (for stats):**
- When a Pomodoro or countdown session completes, show a bottom sheet:
  - "Label this session (optional)" — text field with suggestions: Deep Work, Reading, Coding, Study, Writing, Exercise
  - "Save" and "Skip" buttons
  - Saved to Room DB as a `FocusSession` entity

**Background persistence:**
Same ForegroundService pattern. Notification shows remaining time + phase. Pause/Skip actions in notification.

---

### 4. AppGuardScreen

**Requires special permissions — handle carefully.**

**Permission onboarding flow (shown first time or if permissions missing):**
Show a clean explanation card for each missing permission:
1. Usage Access → "Lets Presime see which apps you open." → Button: "Open Settings" → `Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)`
2. Display over other apps → "Lets Presime show you a nudge overlay." → Button: "Open Settings" → `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`

Only show the guard features after both are granted. Check permissions in `onResume`.

**Main guard UI:**
- Title "App Guard" Caption
- List of watched apps (user-added), each showing:
  - App icon + App name
  - Daily limit (e.g. "30 min / day")
  - Today's usage so far (e.g. "Used: 18 min")
  - A thin horizontal progress bar
  - Edit + Delete actions (swipe to reveal, or long press menu)
- FAB (bottom right): "+" → opens app picker bottom sheet
- App picker: Shows all installed apps (filter by user-installed only), searchable, sorted alphabetically
  - After selecting app: show limit-setting dialog — time picker (5 min increments, 5 min to 4 hours)

**Background service (AppGuardService):**
```kotlin
// Foreground service, runs always when guard has at least one watched app
// Polls UsageStatsManager every 1000ms using Handler + postDelayed
// Uses UsageStatsManager.queryEvents() to detect foreground app changes
// When a watched app comes to foreground: start its session timer
// When session timer exceeds limit for the day:
//   → show SYSTEM_ALERT_WINDOW overlay
//   → post a notification
//   → use performGlobalAction(GLOBAL_ACTION_HOME) via AccessibilityService to go home
```

**Overlay design (drawn via WindowManager + ComposeView):**
- Full-screen dim layer
- Centered card: App icon (large), App name, "Daily limit reached (30 min)"
- Two buttons: "5 more minutes" (extends limit once) + "Go back"
- Card style matches app theme

**AccessibilityService:**
Create `PresimeAccessibilityService` extending `AccessibilityService`. In `onAccessibilityEvent`, detect `TYPE_WINDOW_STATE_CHANGED` to get foreground package name (more reliable than polling). Use `performGlobalAction(GLOBAL_ACTION_HOME)` when limit is reached.

**Room entities:**
```kotlin
@Entity data class WatchedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMs: Long
)

@Entity data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val date: String, // yyyy-MM-dd
    val usedMs: Long
)
```

---

### 5. StatsScreen

**Layout:**
- Top: Time range selector — `Today  |  This Week  |  This Month` (segmented control)
- Summary cards row:
  - "Total focus time" — e.g. `4h 23m`
  - "Sessions" — count
  - "Most focused" — top label or top blocked app
- Bar chart (use `Vico` library for Compose): daily focus minutes, colored bars
- Session history list below chart:
  - Each row: Label chip (e.g. "Deep Work"), date-time, duration
  - Sorted by most recent
  - If no label: show "Unlabeled"
- App usage section (if guard is active): shows top 3 most-used watched apps today with usage bar

**Room queries:**
```kotlin
@Query("SELECT * FROM FocusSession WHERE date >= :from ORDER BY startTime DESC")
fun getSessionsFrom(from: Long): Flow<List<FocusSession>>

@Query("SELECT SUM(durationMs) FROM FocusSession WHERE date >= :from")
fun getTotalFocusTime(from: Long): Flow<Long?>
```

---

## Room database schema

```kotlin
@Entity
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String?,           // nullable — user-assigned label
    val type: String,             // "POMODORO" | "COUNTDOWN" | "STOPWATCH"
    val startTime: Long,          // epoch ms
    val durationMs: Long,
    val date: String              // "yyyy-MM-dd" for easy day grouping
)

@Entity
data class WatchedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMs: Long
)

@Entity
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val date: String,
    val usedMs: Long
)
```

---

## AndroidManifest permissions

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

Declare services:
```xml
<service android:name=".service.StopwatchService"
    android:foregroundServiceType="specialUse" />
<service android:name=".service.TimerService"
    android:foregroundServiceType="specialUse" />
<service android:name=".service.AppGuardService"
    android:foregroundServiceType="specialUse" />
<service android:name=".service.PresimeAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

`res/xml/accessibility_service_config.xml`:
```xml
<accessibility-service
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canPerformGestures="true"
    android:notificationTimeout="100"
    android:description="@string/accessibility_service_description" />
```

---

## Dependencies (libs.versions.toml)

```toml
[versions]
compose-bom = "2024.12.01"
hilt = "2.51"
room = "2.6.1"
lifecycle = "2.8.7"
vico = "2.0.0"
dm-sans = ""  # use downloadable fonts

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-navigation = { group = "androidx.navigation", name = "navigation-compose", version = "2.8.5" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
datastore = { group = "androidx.datastore", name = "datastore-preferences", version = "1.1.1" }
```

---

## Project file structure

```
app/src/main/
├── java/com/presime/
│   ├── MainActivity.kt
│   ├── PresimeApp.kt             (Hilt Application)
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   └── Theme.kt
│   │   ├── navigation/
│   │   │   └── PresimeNavGraph.kt
│   │   ├── components/
│   │   │   ├── BottomNavBar.kt
│   │   │   ├── ClockCanvas.kt    (analog clock)
│   │   │   ├── RingProgress.kt   (countdown ring)
│   │   │   └── LabelChip.kt
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── stopwatch/
│   │   │   ├── StopwatchScreen.kt
│   │   │   └── StopwatchViewModel.kt
│   │   ├── timer/
│   │   │   ├── TimerScreen.kt
│   │   │   ├── TimerViewModel.kt
│   │   │   └── SessionLabelSheet.kt
│   │   ├── guard/
│   │   │   ├── AppGuardScreen.kt
│   │   │   ├── AppGuardViewModel.kt
│   │   │   ├── AppPickerSheet.kt
│   │   │   └── OverlayView.kt
│   │   └── stats/
│   │       ├── StatsScreen.kt
│   │       └── StatsViewModel.kt
│   ├── service/
│   │   ├── StopwatchService.kt
│   │   ├── TimerService.kt
│   │   ├── AppGuardService.kt
│   │   └── PresimeAccessibilityService.kt
│   ├── data/
│   │   ├── db/
│   │   │   ├── PresimeDatabase.kt
│   │   │   ├── FocusSession.kt
│   │   │   ├── WatchedApp.kt
│   │   │   ├── AppUsageRecord.kt
│   │   │   └── dao/
│   │   │       ├── FocusSessionDao.kt
│   │   │       ├── WatchedAppDao.kt
│   │   │       └── AppUsageDao.kt
│   │   └── repository/
│   │       ├── SessionRepository.kt
│   │       └── GuardRepository.kt
│   └── util/
│       ├── TimeFormatter.kt
│       ├── PermissionHelper.kt
│       └── AppInfoHelper.kt      (gets installed app list + icons)
└── res/
    ├── font/           (DM Sans variants)
    ├── xml/
    │   └── accessibility_service_config.xml
    └── values/
        └── strings.xml
```

---

## Key implementation notes

1. **Analog clock** — implement entirely in Compose Canvas. Do NOT use any view-based clock library. Update via `LaunchedEffect` with `delay(1000)`.

2. **Arc/circular navigation** — NOT needed for Phase 1-2. The uploaded image is a style reference for typography weight and spacing, not the navigation model. Use standard bottom nav.

3. **Ghost numerals** — on HomeScreen, render the current hour as a large ghost text (alpha 0.08) behind the clock as a decorative layer only.

4. **Service ↔ ViewModel binding** — use `ServiceConnection` + a `MutableStateFlow` exposed from the service. ViewModel observes via `collectAsStateWithLifecycle`.

5. **Pomodoro completion sound** — use `SoundPool` with a short bell WAV bundled in `res/raw/`. Do NOT use MediaPlayer for short sounds.

6. **App icons in guard list** — use `packageManager.getApplicationIcon(packageName)` converted to `BitmapPainter` in Compose.

7. **Daily usage reset** — use a `BroadcastReceiver` listening to `ACTION_DATE_CHANGED` + `ACTION_BOOT_COMPLETED` to reset today's usage counters at midnight.

8. **Theming** — implement full `dynamicColorScheme` with a manual fallback using the color tokens above. Add a DataStore preference for `ThemeMode` (SYSTEM / LIGHT / DARK).

9. **Low-spec optimization (i3, 8GB RAM)** — avoid animated gradients, particle effects, and heavy blur. All animations should use `spring()` or simple `tween(200ms)`. Build with `--parallel` and enable R8 in debug builds off.

---

## What to build first (Phase 1 order)

1. Project setup — Hilt, Room, Navigation, theme tokens
2. Theme.kt + Color.kt + Type.kt with full light/dark palette
3. Bottom navigation + NavGraph skeleton (all 5 empty screens)
4. HomeScreen — analog clock Canvas component
5. StopwatchScreen + StopwatchService
6. TimerScreen — countdown mode only first, then Pomodoro
7. Session label bottom sheet + Room DB write
8. StatsScreen — basic session list, no chart yet
9. Add Vico chart to StatsScreen

**Phase 2 (after Phase 1 is stable):**
10. Permission onboarding flow (AppGuardScreen)
11. WatchedApp Room entities + AppPickerSheet
12. AppGuardService with UsageStatsManager polling
13. AccessibilityService setup
14. WindowManager overlay on limit hit
15. Stats integration — show app usage data alongside focus sessions
```
