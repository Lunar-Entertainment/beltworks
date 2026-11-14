package com.microfactory.sim

data class MovingItem(
    val id: Long,
    val type: ItemType,
    var prevCoord: GridCoord,
    var currentCoord: GridCoord
)
