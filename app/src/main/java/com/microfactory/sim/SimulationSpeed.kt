package com.microfactory.sim

enum class SimulationSpeed(val multiplier: Int, val label: String) {
    PAUSED(0, "Pause"),
    X1(1, "1x"),
    X2(2, "2x"),
    X3(3, "3x")
}
