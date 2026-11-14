# Micro-Factory Belt

A minimalist, one-handed, portrait-mode grid automation puzzle game built in **Kotlin** with **Jetpack Compose (Material 3)** and a custom Compose `Canvas` renderer.

## Gameplay & Controls

- **Objective:** Route raw resources from fixed **Input Spawners** through conveyor belts and processing machines to satisfy all **Delivery Hatch** quotas.
- **Drag with Belt tool:** Automatically orients straight and 90° curved corner belts along your finger path.
- **Tap existing belt/machine:** Rotates orientation 90° clockwise (fixed Spawners, Hatches, and Rocks stay locked).
- **Tap or drag with Bulldoze tool:** Clears player-placed tiles back to empty.
- **Simulation Speeds:** Toggle `Pause`, `1x`, `2x`, `3x`, or `Reset` at any time.

## Architecture

- `com.microfactory.sim` — 100% pure Kotlin simulation engine (zero Android framework imports):
  - `Direction`, `GridCoord`, `ItemType`, `TileEntity`, `MovingItem`, `SimulationSpeed`
  - `FactorySimulation` with fixed 4 ticks/sec base rate, two-pass back-to-front reservation tick resolution, and rolling Items Per Minute (IPM) tracking
  - `LevelConfig` and `LevelRepository` (3 starter levels: *First Melt*, *Heavy Metal*, *Gearworks*)
- `com.microfactory.render` — Custom Compose `Canvas` renderer with 60/120 FPS `withFrameNanos` linear interpolation, animated chevron belt treads, and `pointerInput` gesture handling.
- `com.microfactory.ui` — Material 3 dark industrial Top HUD Bar, thumb-accessible Machine Palette, and Victory Modal with belt-efficiency star ratings.

## Build & Test

```bash
./gradlew testDebugUnitTest assembleDebug
```
