package net.rk4z.s1.econgrowth.paper.listeners

import kotlinx.coroutines.Runnable
import net.rk4z.s1.econgrowth.paper.EconGrowth
import net.rk4z.s1.swiftbase.core.CB
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

object ActivePlayTimeManager : Listener {
    private val lastActiveTime = mutableMapOf<UUID, Long>() // プレイヤーID -> 最終アクション時間
    private val activePlayTime = mutableMapOf<UUID, Long>() // プレイヤーID -> アクティブ時間

    init {
        CB.executor.executeAsyncTimer(Runnable {
            val now = System.currentTimeMillis()
            for (player in Bukkit.getOnlinePlayers()) {
                val uuid = player.uniqueId
                val lastActive = lastActiveTime[uuid] ?: now

                if (now - lastActive <= 300_000) {
                    activePlayTime[uuid] = activePlayTime.getOrDefault(uuid, 0L) + 5000
                }
            }
        }, 0L, 100L)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from != event.to) updateLastActiveTime(event.player)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        updateLastActiveTime(event.player)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        updateLastActiveTime(event.player)
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.damager is Player) updateLastActiveTime(event.damager as Player)
    }

    private fun updateLastActiveTime(player: Player) {
        lastActiveTime[player.uniqueId] = System.currentTimeMillis()
    }

    fun getActivePlayTime(player: Player): Long {
        return activePlayTime[player.uniqueId] ?: 0L
    }
}
