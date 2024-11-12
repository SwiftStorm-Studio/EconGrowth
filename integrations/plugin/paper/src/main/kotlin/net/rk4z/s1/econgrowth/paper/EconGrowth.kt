package net.rk4z.s1.econgrowth.paper

import net.rk4z.s1.econgrowth.paper.utils.System
import net.rk4z.s1.econgrowth.paper.utils.System.Log.Other.UNKNOWN.t
import net.rk4z.s1.swiftbase.paper.PaperPlayerAdapter
import net.rk4z.s1.swiftbase.paper.PluginEntry
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent


class EconGrowth : PluginEntry(

) {
    init {

    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        System.Log.CHECKING_UPDATE.t(PaperPlayerAdapter(event.player))
    }
}