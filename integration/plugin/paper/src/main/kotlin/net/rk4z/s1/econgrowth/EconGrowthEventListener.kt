package net.rk4z.s1.econgrowth

import net.rk4z.s1.econgrowth.utils.getPlayer
import net.rk4z.s1.econgrowth.utils.isPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EconGrowthEventListener : Listener {
    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        if (event.entity.isPlayer().not()) return

        val player = event.entity.getPlayer()!!
    }
}