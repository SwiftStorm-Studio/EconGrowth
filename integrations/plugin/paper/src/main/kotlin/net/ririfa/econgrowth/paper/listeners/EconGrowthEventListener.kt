package net.ririfa.econgrowth.paper.listeners

import net.rk4z.s1.econgrowth.core.utils.Country
import net.rk4z.s1.econgrowth.core.utils.getTimeByCountry
import net.rk4z.s1.econgrowth.core.utils.toShortUUID
import net.rk4z.s1.econgrowth.paper.EconGrowth
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Suppress("unused")
class EconGrowthEventListener : Listener {
    val dataBase = EconGrowth.EGDB

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId.toShortUUID()

        if (!dataBase.isPlayerRegistered(uuid)) {
            dataBase.insertNewPlayer(uuid)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId.toShortUUID()

        if (dataBase.isPlayerRegistered(uuid)) {
            dataBase.updateLastLogin(uuid, getTimeByCountry(Country.UTC))
        }
    }

    @EventHandler
    fun onPlayerPlaceBlock(event: BlockPlaceEvent) {
        val player = event.player
        val block = event.block
        val material = block.type.toString().lowercase()
        val x = block.x
        val y = block.y
        val z = block.z
        val dim = player.world.environment.toString().lowercase()

        dataBase.insertNewBlock(x, y, z, material, dim)
    }

    @EventHandler
    fun onPlayerBreakBlock(event: BlockBreakEvent) {
        val block = event.block
        val x = block.x
        val y = block.y
        val z = block.z
        var isPlacedBlock = false

        if (dataBase.isPlayerPlacedBlock(x, y, z, block.world.environment.toString().lowercase())) {
            dataBase.deleteBlockFromPlacedBlock(x, y, z, block.world.environment.toString().lowercase())
            isPlacedBlock = true
        }

        event.player.sendMessage("BlockHardness: ${block.type.hardness}")

        //val xp = XPManager.calculateXP(event.player, isPlacedBlock, event)
    }
}