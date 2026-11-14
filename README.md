# Micro-Factory Belt

A minimalist, one-handed, portrait-mode grid automation puzzle game built in **Kotlin** with **Jetpack Compose (Material 3)**, a custom Compose `Canvas` renderer, and a procedural ambient flow soundtrack.

## Gameplay & Features

- **Main Menu & Contract Select:** Animated conveyor showcase, quick-continue button, star ratings & best belt records saved per level, and soundtrack toggle.
- **8 Progressive Levels:** Ranges from 8×10 starter smelters to 9×12 dual-line gear factories (*First Melt*, *Heavy Metal*, *Gearworks*, *Copper Vein*, *Twin Press*, *Full Foundry*, *Dual Alloy*, *Clockwork Core*).
- **Smooth Belt Routing & Rotation:**
  - **Drag with Belt tool:** Automatically orients straight and 90° curved corner belts along your finger path.
  - **Tap existing belt/machine:** Smoothly connects and sweeps belt outputs around neighboring feeders/targets (creating curved corners automatically), and rotates machines 90° clockwise.
  - **Tap or drag with Bulldoze tool:** Clears player-placed tiles back to empty.
- **Flow Soundtrack:** Zero-asset procedural `AudioTrack` synthesizer playing a D Dorian ambient pad, marimba-style arpeggio, soft pulse, and tactile placement/victory chimes.

## Architecture

- `com.microfactory.sim` — 100% pure Kotlin simulation engine (zero Android framework imports):
  - `Direction`, `GridCoord`, `ItemType`, `MovingItem`, `SimulationSpeed`, `TileEntity`
  - `FactorySimulation` with fixed 4 ticks/sec base rate, two-pass back-to-front reservation tick resolution, neighbor-aware smooth belt rotation, and rolling IPM tracking
  - `LevelConfig` and `LevelRepository` (8 solvable levels across 8×10, 9×11, and 9×12 grids)
- `com.microfactory.render` — Custom Compose `Canvas` renderer with 60/120 FPS `withFrameNanos` linear interpolation, seamless straight/curved belts with animated chevrons, and `pointerInput` gesture handling.
- `com.microfactory.audio` — `FlowMusicEngine` procedural `AudioTrack` synthesizer.
- `com.microfactory.data` — `ProgressStore` (`SharedPreferences`) for stars, best belt counts, and audio preference.
- `com.microfactory.ui` — `MainMenuScreen`, `FactoryGameScreen`, `TopHudBar`, `MachinePalette`, and `VictoryDialog`.

## Build & Test

```bash
./gradlew testDebugUnitTest assembleDebug
```
