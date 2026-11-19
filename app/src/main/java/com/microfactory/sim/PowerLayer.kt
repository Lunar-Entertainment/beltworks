package com.microfactory.sim

/**
 * Active editing/viewing layer in the dual-layer grid architecture.
 * - [FACTORY] (Layer 0): inner `width x height` floor for belts, spawners, hatches, and machines.
 * - [POWER] (Layer 1): expanded `(width + 4) x (height + 4)` grid with a 2-tile outer perimeter ring.
 */
enum class GridLayer {
    FACTORY,
    POWER
}

/**
 * Terrain features on Layer 1 (Power Layer).
 */
enum class PowerTerrainType {
    NORMAL,
    WIND_SPOT,
    WATER_CURRENT,
    ROCK
}

/**
 * A terrain feature placed at [coord] in Power Layer coordinates (`0 until width + 4`, `0 until height + 4`).
 */
data class PowerTerrainTile(
    val coord: GridCoord,
    val type: PowerTerrainType,
    val flowDir: Direction = Direction.SOUTH
)

/**
 * Occupant entity on a single cell of Layer 1 (Power Layer).
 */
sealed interface PowerTile {
    data object Empty : PowerTile

    /**
     * Generates +3⚡ on [PowerTerrainType.NORMAL] and +6⚡ (2.0x) on [PowerTerrainType.WIND_SPOT].
     */
    data object WindTurbine : PowerTile

    /**
     * Generates +8⚡ steady output. Placeable only on [PowerTerrainType.WATER_CURRENT].
     */
    data class HydroGenerator(
        val direction: Direction = Direction.SOUTH
    ) : PowerTile

    /**
     * Conductive cable with auto-connecting 4-way neighbor mask (N=1, E=2, S=4, W=8).
     */
    data class PowerCable(
        val connectionMask: Int = 0
    ) : PowerTile {
        fun isConnected(dir: Direction): Boolean =
            (connectionMask and maskForDirection(dir)) != 0

        companion object {
            const val MASK_NORTH = 1 shl 0
            const val MASK_EAST = 1 shl 1
            const val MASK_SOUTH = 1 shl 2
            const val MASK_WEST = 1 shl 3

            fun maskForDirection(dir: Direction): Int = when (dir) {
                Direction.NORTH -> MASK_NORTH
                Direction.EAST -> MASK_EAST
                Direction.SOUTH -> MASK_SOUTH
                Direction.WEST -> MASK_WEST
            }
        }
    }

    /**
     * Stores up to 40⚡ of surplus generation, discharges automatically when demand exceeds supply,
     * and resets the cable transmission distance penalty.
     */
    data object CapacitorBank : PowerTile

    /**
     * Conductive power node that supplies Layer 0 machines directly above its footprint.
     */
    data object Substation : PowerTile

    fun isConductive(): Boolean = this != Empty

    fun canSupplyMachineAbove(): Boolean = when (this) {
        is PowerCable, CapacitorBank, Substation -> true
        Empty, WindTurbine, is HydroGenerator -> false
    }

    fun isPlayerEditable(): Boolean = this != Empty
}

/**
 * Live telemetry for a connected Power Layer sub-network.
 */
data class PowerNetworkStats(
    val networkId: Int,
    val rawGenerated: Float,
    val effectiveGenerated: Float,
    val transmissionEfficiency: Float,
    val demand: Float,
    val capacitorStored: Float,
    val capacitorCapacity: Float,
    val capacitorDischarge: Float,
    val satisfaction: Float
)

/**
 * Visual power status of a Layer 0 processing machine.
 */
enum class MachinePowerState {
    UNPOWERED,
    BROWNOUT,
    POWERED,
    OVERCLOCKED
}
