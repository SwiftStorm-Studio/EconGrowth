package net.rk4z.s1.econgrowth.xp

import org.bukkit.Material

object XPManager {
    private val blockXPMap = mutableMapOf<Material, IntRange>(
        // 空気
        Material.AIR to 0..0,
        // 石
        Material.STONE to 1..2,
        // 花崗岩
        Material.GRANITE to 1..2,
        // 滑らかな花崗岩
        Material.POLISHED_GRANITE to 1..2,
        // 閃緑岩
        Material.DIORITE to 1..2,
    )
}