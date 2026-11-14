# Beltworks

A minimalist, one-handed, portrait-mode grid automation puzzle game built in **Kotlin** with **Jetpack Compose (Material 3)**, a custom Compose `Canvas` renderer, and a procedural ambient flow soundtrack.

## Gameplay & Features

- **Main Menu & Contract Select:** Animated conveyor showcase, quick-continue button, tier jump filters (`All (80)`, `1–8 Core`, `9–18 Bridge`, `19–30 Circuit`, `31–44 Filter`, `45–62 Motor`, `63–80 Apex`), `NEW` mechanic badges, star ratings & best belt records saved per level, and soundtrack toggle.
- **80 Progressive Levels:** Ranges from 8×10 starter smelters (Levels 1–8) to 10×13 dual-line megafactories (up to Level 80), introducing new logistics and crafting mechanics after Level 8:
  - **Level 9 — Copper Wire:** `Press` stamps `COPPER_INGOT` into `COPPER_WIRE`.
  - **Level 10 — Bridge:** Dual-lane 1-tile overpass crossing horizontal and vertical streams without mixing.
  - **Level 19 — Circuit:** `Assembler` combines `IRON_PLATE` + `COPPER_WIRE` into `CIRCUIT`.
  - **Level 31 — Mixed Spawners & Filter:** Alternating two-item spawners paired with directional `Filter` tiles (tap or use the inline filter bar to pick the target item).
  - **Level 35 — Trash (Incinerator):** Multi-sided sink that destroys unwanted or overflow items.
  - **Level 45 — Overflow:** Priority splitter that sends items straight until backed up, then spills excess laterally (`Hatch` stops accepting once its quota is met).
  - **Level 48 — Motor:** `Assembler` combines `GEAR` + `CIRCUIT` into `MOTOR`.
- **Smooth Belt Routing & Rotation:**
  - **Drag with Belt tool:** Automatically orients straight and 90° curved corner belts along your finger path.
  - **Tap existing belt/machine:** Smoothly connects and sweeps belt outputs around neighboring feeders/targets (creating curved corners automatically), and rotates machines 90° clockwise.
  - **Tap or drag with Bulldoze tool:** Clears player-placed tiles back to empty.
- **Flow Soundtrack:** Zero-asset multi-track procedural `AudioTrack` synthesizer featuring 5 cohesive ambient-flow songs (*Conveyor Drift*, *Copper Rain*, *Midnight Smelter*, *Clockwork Tide*, *Solar Circuit*) with detuned warm pads, polyphonic kalimba/bell plucks, sub-bass, damped echo delay, track skip controls, and tactile placement/victory chimes.

## Architecture

- `com.microfactory.sim` — 100% pure Kotlin simulation engine (zero Android framework imports):
  - `Direction`, `GridCoord`, `ItemType`, `MovingItem`, `SimulationSpeed`, `TileEntity`
  - `FactorySimulation` with fixed 4 ticks/sec base rate, two-pass back-to-front reservation tick resolution, dual-lane `Bridge` slots, neighbor-aware smooth belt rotation, and rolling IPM tracking
  - `LevelConfig`, `LevelRepository`, `LevelPack2`, `LevelPack3`, `LevelPack4` (80 levels across 8×10 through 10×13 grids)
- `com.microfactory.render` — Custom Compose `Canvas` renderer with 60/120 FPS `withFrameNanos` linear interpolation, seamless straight/curved belts with animated chevrons, and `pointerInput` gesture handling.
- `com.microfactory.audio` — `FlowMusicEngine` procedural `AudioTrack` synthesizer.
- `com.microfactory.data` — `ProgressStore` (`SharedPreferences`) for stars, best belt counts, and audio preference.
- `com.microfactory.ui` — `MainMenuScreen`, `FactoryGameScreen`, `TopHudBar`, `MachinePalette`, and `VictoryDialog`.

## Build & Test

```bash
./gradlew testDebugUnitTest assembleDebug
```
