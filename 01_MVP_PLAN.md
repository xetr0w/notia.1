# NOTIA — ANDROID RUNBOOK (Antigravity Execution Spec)
**Status:** Android-first, production-quality, zero-shortcuts  
**Goal:** “Samsung Notes hissi + profesyonel UI + offline-first + Study Timer + sonra Sync + AI”  
**Audience:** Antigravity agent(s) implementing the app step-by-step.

---

## 0) NORTH STAR: What “Perfect” Means
This project is considered successful only if:

### 0.1 Core Experience (Non-negotiable)
- Writing feels *instant* (low latency), smooth, stable.
- No random strokes from palm / accidental touches.
- No “sometimes saves sometimes not” behavior. Data integrity is sacred.
- UI is professional and pleasant: animations are smooth and subtle, not gimmicky.
- Low-end devices do not stutter during writing or navigation.

### 0.2 Performance Targets (Reality-based)
We do NOT hard-lock FPS. We target:
- **Frame pacing**: stable rendering without visible jank.
- **Input latency**: minimal perceived lag while writing.

Budgets:
- 60Hz devices: try to stay under ~16.6ms per frame during active writing.
- 120Hz devices: stay as low as possible (8.3ms budget exists but not always reachable). The requirement is: **no visible stutter**, and writing remains fluid.

### 0.3 “Nice Effects” Rule (UX polish)
Buttons and transitions must feel alive, but:
- No heavy blur / huge shadows / expensive runtime effects during drawing.
- No over-animated “toy UI”.
- Never block UI thread.
- Every animation must be cancellable and must not cause jank.

---

## 1) TECHNOLOGY DECISIONS (Final)
### 1.1 Stack
- Language: **Kotlin**
- UI: **Jetpack Compose**
- Drawing Engine: **Custom View (android.view.View + Canvas)** embedded via `AndroidView`.
  - Compose Canvas is allowed for non-critical visuals (graph, mind map view), NOT for pen engine MVP.
- Database (index only): **Room**
- File Storage (strokes + pages + attachments): `.notia` **single-file package** per note (zip container)
- Async: Coroutines + Flow
- Background tasks: WorkManager
- Logging: Timber

### 1.2 Minimum SDK
- minSdk: 26
- targetSdk: latest stable

---

## 2) PRODUCT SHAPE: Modules & Features
Notia is 2 main product pillars:
1) **Notes (drawing + typed + attachments)**
2) **Study Timer (sessions + stats + session notes)**

Other features (links, mind map, AI, sync) come after core stability.

---

## 3) ARCHITECTURE (MVVM – Practical, Not Over-Engineered)
### 3.1 High-level
Use MVVM with clean boundaries:
- UI (Compose screens)
- ViewModels (state + events)
- Domain/Repositories (business logic)
- Data sources:
  - Room for note index & timer sessions metadata
  - FileStore for `.notia` package (note content)
  - Later: SyncStore (cloud)
  - Later: AI module

### 3.2 Packages (recommended)
`com.notia.app`
- `core/` (utils, time, serialization, logging)
- `data/indexdb/` (Room: NoteEntity, SessionEntity, DAOs)
- `data/repos/` (NoteRepository, TimerRepository)
- `filestore/` (NotiaPackageStore, ZipIO, BinaryStrokeCodec)
- `drawing/` (DrawingView, InputSampler, Renderer, StrokeModel, Tools)
- `features/noteslist/`
- `features/editor/`
- `features/timer/`
- `features/settings/`
- `features/search/` (later)
- `features/links/` (later)
- `features/mindmap/` (later)
- `sync/` (later)
- `ai/` (later)

### 3.3 Principle
Drawing engine must remain independent from Compose state churn.
Compose should not recompose at high frequency from drawing events.

---

## 4) DATA MODEL: `.notia` NOTE PACKAGE (Single File)
### 4.1 Why single file?
- Offline-first
- Easy backup / share
- Easy sync
- No DB bloating
- Fewer edge cases

### 4.2 Package structure
`note_<uuid>.notia` (zip)
- `meta.json`
- `pages/001.bin`, `pages/002.bin`, ...
- `thumb.webp` (or png)
- `attachments/` (images, pdf snapshots)
- `links.json` (later)
- `mindmap.json` (later)

### 4.3 meta.json (MVP schema)
```json
{
  "schemaVersion": 1,
  "noteId": "uuid",
  "title": "Untitled",
  "createdAt": 1730000000,
  "updatedAt": 1730001200,
  "canvasMode": "PAGE",
  "page": { "width": 2100, "height": 2970, "dpi": 300, "bg": "plain" },
  "fingerDrawingEnabled": true,
  "encryption": { "enabled": false, "algo": "AES_GCM" }
}
4.4 Stroke Binary Format (MVP: stable + upgradeable)
Define a binary codec with a schemaVersion inside page bin.

pages/001.bin:

header:

magic NTST (4 bytes)

version u16

strokeCount u32

each stroke:

toolType u8 (0 pen, 1 marker, 2 eraser)

colorARGB u32

baseWidth f32

flags u16 (reserved)

pointCount u32

points: repeated pointCount:

x f32

y f32

pressure f32 (0..1)

t u32 (ms from stroke start)

Performance rule: no PointF object spam. Use primitive arrays or ByteBuffer.

Upgrade path: later quantize to shorts (fixed-point) and use varint to compress.

5) NOTE INDEX DB (Room)
5.1 NoteEntity (index only)
Fields:

id (uuid)

title

createdAt

updatedAt

filePath (local)

thumbPath

pinned (bool)

tags (string or normalized table later)

reminderAt (nullable)

encrypted (bool)

lastOpenedAt

deleted (soft delete optional)

Queries needed:

list recent (updatedAt desc)

list today edited

list recently added

search title/tags (later full text)

filter reminders

5.2 Timer entities (see Timer section)
6) INPUT MODEL (Finger + Stylus, Final Rules)
We keep your simple mental model BUT implement it correctly and robustly.

6.1 Final Behavior Table
Mode	1 Finger	2 Fingers	Stylus
FingerDrawing ON	Draw	Pan/Zoom	Draw
FingerDrawing OFF	Pan/Scroll	Pan/Zoom	Draw

6.2 Stylus-first priority
Any TOOL_TYPE_STYLUS input draws regardless of fingerDrawing setting.

When stylus is active, single-finger drawing is ignored (palm rejection by policy).

Two-finger gestures always operate (pan/zoom).

6.3 Implementation approach
Use pointer tracking:

Determine tool type per pointer.

If pointer count >= 2 and both are fingers -> pan/zoom.

Stylus down triggers drawing mode immediately.

If stylus is down, ignore single-finger drawing events.

6.4 Why not “too complicated”?
It’s not complicated if you implement it as a clear ruleset. The “hard part” only appears if you mix drawing & gestures without strict gating.

7) DRAWING ENGINE (The Heart)
7.1 Goals
Smooth lines, stable, low-latency

Efficient memory

Efficient render

Undo/redo reliable

Autosave safe (debounced)

7.2 Pipeline (must follow)
Input -> Sampling -> Smoothing -> Build Stroke -> Render -> Commit -> Persist (debounced)

7.2.1 Input Sampling (critical)
Rules:

Only record point if distance from last point > minDistPx

AND/OR time since last sample > minTimeMs
Defaults (tune on device):

minDistPx ~ 1.5–2.5

minTimeMs ~ 8–12

7.2.2 Smoothing
MVP smoothing: quadratic bezier interpolation.

Build path by midpoints and quadTo.

Keep it fast.

Phase 2 smoothing upgrade:

Catmull-Rom -> cubic Bezier

Optional stabilization

7.2.3 Pressure handling (MVP)
MVP: do NOT attempt true variable-width brush inside one path (expensive).
Instead:

For stylus, store pressure per point for future use.

Render:

For pen tool: width = baseWidth (optionally mild smoothing based on average pressure)

For marker: allow mild pressure scaling per stroke (not per segment)

Phase 3+:

Implement real variable-width stroke (mesh/segments)

Only after core is stable.

7.2.4 Rendering Strategy (must)
Maintain cachedBitmap of committed strokes.

During active stroke:

draw cachedBitmap

draw active stroke path on top

Use dirty rect invalidation:

compute bounds of new segment

invalidate only that region (with padding)

Undo/redo:

Keep stroke list stacks.

After undo:

rebuild cachedBitmap from strokes.

Use checkpoints every N strokes to speed rebuild (Phase 2).

7.2.5 Eraser
Two eraser types:

MVP: “stroke eraser” (remove entire stroke if hit-test)

Phase 2: “pixel eraser” (requires separate layer or clearing path)
Start with stroke eraser (fast + stable), then evolve.

7.2.6 Zoom/Pan Coordinates
Maintain transform matrix:

world coords = note/page coords

view coords = screen
All stroke points saved in world coords.

8) UI/UX DESIGN SYSTEM (Professional + Smooth)
8.1 UI principles
Minimal, modern, premium

Soft shadows & subtle gradients allowed in lists, not on live drawing surface.

No heavy blur on editor during writing.

Avoid recomposition storms.

8.2 Interaction animations (required, tasteful)
Implement:

Press ripple + slight scale down (0.98–0.99) with spring

Icon button: quick haptic (if available) + highlight

Toolbar open/close: slide + fade

Bottom sheets: standard Material motion

Page switch: subtle crossfade/slide

List item open: shared-element-like feel (optional) but never janky

Strict rule: animations must not stutter. If a fancy effect causes jank -> remove it.

8.3 Editor layout
Top AppBar: title + back + menu

Floating/anchored toolbar:

pen/marker/eraser

width slider (popover)

color picker (popover)

undo/redo

Page indicator

Quick toggle for finger drawing

Quick access to timer (study mode)

8.4 Notes list layout
Tabs: Recent / Today edited / Reminders / Shared (later)

Each note card:

thumbnail

title

last edited

Sorting and search bar

8.5 Performance guardrails for Compose
Don’t push per-point drawing events into Compose state.

Compose only reacts to coarse events:

note loaded

tool changed

undo/redo pressed

save completed

DrawingView handles continuous updates internally.

9) STUDY TIMER (Integrated, not bolted-on)
9.1 Requirements
Pomodoro and custom sessions

Background running reliably

Notifications at session end

Sessions are saved

“Session notes” integration:

each session can attach a quick note or link to a Notia note

Stats:

daily total

weekly total

streaks/badges (later)

9.2 Timer model
We create:

TimerMode: Pomodoro (25/5), Custom (X/Y), Stopwatch

SessionState: Idle, Running, Paused, BreakRunning, Completed

9.3 Persistence (Room)
Entities:

StudySessionEntity:

id

startedAt

endedAt

durationMs

mode (pomodoro/custom)

noteId? (optional link to note)

sessionNoteText? (optional)

DailyAggregateEntity (optional caching):

date

totalMs

9.4 Background strategy
Use ForegroundService for reliable long timer? (Android restrictions vary)

Or WorkManager + exact alarms? (varies)
Recommendation:

For precise countdown + user-facing: Foreground service while running

Notifications:

ongoing notification while timer active

final “timer done” notification with action: open session note

9.5 UX integration
Small timer pill available in editor and note list

“Start session” from anywhere

“Attach session to current note” option

Session completion screen:

quick summary

add quick sticky note

optionally generate flashcards later (AI)

10) FEATURE BACKLOG (Your list, placed correctly)
10.1 Core (must before cloud/AI)
Recent added

Today edited

Reminders + recurring

Theme (dark/light)

Scanner (CameraX)

Encrypted notes

Export PDF

10.2 Linking & Knowledge Graph (Phase 4)
Note-to-note links

Link preview mini panel

Anchors on highlight or “note button”

Graph view (note map)

“idea came from note X” relations

10.3 Mind Map Mode (Phase 4)
Dedicated mind map view

Stylus shortcut interactions:

stylus button + tap: create node

stylus button + tap: create child

stylus button + double tap: delete

drag nodes

Undo/redo

10.4 Study Tools (Phase 6)
Flashcards

Sticky notes (also Phase 3 as simple per-note overlay)

Slide summary (AI)

Keyword extraction (AI)

Similar notes (AI)

Voice dictation

Streaks/badges

Not DNA (style/title suggestion)

11) SECURITY & RELIABILITY
11.1 Encryption (Phase 3)
Android Keystore generates master key

Encrypt .notia file contents (AES-GCM)

UI:

lock icon

biometric unlock optional

11.2 Data integrity
Never partially write .notia without atomicity:

write temp -> fsync -> rename

Autosave must be resilient:

if crash occurs, last committed strokes still present.

12) PHASE PLAN + GATES (Do not skip)
Phase 1: Drawing MVP + Save/Load + Notes list minimal
Gate 1 tests
✅ draw 20 minutes, no visible stutter
✅ app kill -> reopen -> same note restored
✅ undo/redo does not corrupt file
✅ memory stable (no runaway growth)

Phase 2: Multi-page + Zoom/Pan + Better stability + Palm logic polish
Gate 2
✅ 30 pages, fast open
✅ zoom/pan reliable
✅ finger toggle behaves exactly as table

Phase 3: Productization (scanner/export/reminders/theme/encryption)
Gate 3
✅ export PDF works
✅ reminders reliable
✅ encryption safe

Phase 4: Linking + Graph + Mind Map
Gate 4
✅ link preview & navigation
✅ graph view stable

Phase 5: Cloud sync + auto backup + share
Gate 5
✅ two devices sync simple conflict handling

Phase 6: AI & study enhancements
Gate 6
✅ AI never impacts editor performance

13) ANTIGRAVITY EXECUTION INSTRUCTIONS
13.1 Workflow rules
Work strictly phase by phase.

Each step ends with:

code changes

how to run

test checklist

expected result

No “big bang” changes. Small PRs.

13.2 Step-by-step tasks (Phase 1)
Step 1 — Bootstrap

create project, dependencies, base theme

implement navigation skeleton

Step 2 — Room index

NoteEntity, DAO, database, repository

NotesList shows fake data from Room

Step 3 — NotiaPackageStore

create/open note package

read/write meta.json

write/read page bin (can start with in-memory -> file once stable)

thumb write placeholder

Step 4 — Drawing engine core
Files:

DrawingView.kt

InputSampler.kt

Stroke.kt

ToolConfig.kt

Renderer.kt (cachedBitmap approach)

UndoRedoManager.kt
DoD:
✅ draws smooth line with no allocations per move

Step 5 — Editor screen integration

AndroidView host DrawingView

toolbar changes tool + width + color

autosave debounce to NotiaPackageStore

update Room.updatedAt + thumb

Step 6 — Study Timer MVP

Timer screen + pill

Foreground service running timer

store sessions in Room

Stop Phase 1 only when Gate 1 passes.

14) TEST PLAN (Practical)
14.1 Manual test scenarios
Write fast, scribble, long lines, short taps

20 minutes continuous writing

Undo/redo many times

Switch tools repeatedly

Toggle finger drawing while writing (should not break)

Background/foreground app

Low battery mode (if available)

Rotate screen (if supported) or lock to portrait (decide)

14.2 Profiling (required)
Use Android Studio Profiler:

CPU spikes during drawing?

memory allocations per move?

GC frequency?

Identify jank sources early.

15) QUALITY BAR FOR UI EFFECTS (Non-janky polish)
Required micro-interactions
Buttons: ripple + subtle scale spring

Icon toggles: animated color/weight change

Sheets: smooth slide/fade

Cards: elevation on press

Forbidden during drawing
Heavy blur

complex shadows recalculated per frame

expensive animated gradients

recomposing editor every pointer move

If effect causes lag -> remove it immediately.

16) OPEN QUESTIONS (Resolve before Phase 2)
Page size options (A4 default? custom?)

Orientation lock? (portrait default recommended)

Marker blending style?

END OF RUNBOOK