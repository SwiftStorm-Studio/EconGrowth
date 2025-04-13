package net.ririfa.econgrowth.paper

import net.ririfa.econgrowth.core.EGDB
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class Fox : JavaPlugin() {
    companion object {
        lateinit var EGDB: EGDB
            private set


    }

    override fun onEnable() {
        logger.info("")
        server.pluginManager.registerEvents(
            foxFox,
            this
        )
    }

    private val foxFox = object : Listener {
        @EventHandler
        fun onPlayerJoin(event: PlayerJoinEvent) {
            val player = event.player

            player.performCommand("")

//            player.sendMessage(
//                Component.text("Foxxxx")
//                    .style {
//                        it.color(TextColor.color())
//                    }
//            )
        }
    }
}