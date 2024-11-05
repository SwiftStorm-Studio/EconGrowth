package net.rk4z.s1.econgrowth.utils

data class PlayerRegInfo(
    val uuid: ShortUUID,
    val balance: Double,
    val lastLogin: String,
    val currentTotalXP: Int,
    val level: Int
)