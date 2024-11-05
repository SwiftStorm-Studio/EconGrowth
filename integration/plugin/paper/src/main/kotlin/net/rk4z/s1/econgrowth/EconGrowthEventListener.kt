package net.rk4z.s1.econgrowth

import net.rk4z.s1.econgrowth.utils.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.time.Instant

@Suppress("DuplicatedCode")
class EconGrowthEventListener : Listener {
    val dataBase = EconGrowth.dataBase

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        if (event.entity.isPlayer().not()) return

        val player = event.entity.getPlayer()!!
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (!dataBase.playerIsRegistered(player.uniqueId.toString())) {
            val data = PlayerRegInfo(
                ShortUUID.fromUUIDString(player.uniqueId.toString()),
                0.0,
                // ISO 8601 format
                Instant.now().toString(),
                0,
                1
            )

            dataBase.registerPlayer(data)
        } else {
            dataBase.updateLastLogin(player.uniqueId.toString(), Instant.now().toString())
        }
    }

    @EventHandler
    fun onPlayerBreakBlock(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block
        val material = block.type
        val x = block.x
        val y = block.y
        val z = block.z
        val dim = block.world
        var isPlayerPlaced = false

        if (dataBase.isPlayerPlacedBlock(x, y, z, dim.name)) {
            dataBase.deletePlayerPlacedBlock(x, y, z, dim.name)
            isPlayerPlaced = true
        }

        //TODO: Calculate the amount of XP to be granted to the player from various states.
    }

    @EventHandler
    fun onPlayerPutBlock(event: BlockPlaceEvent) {
        val block = event.block
        val material = block.type
        val x = block.x
        val y = block.y
        val z = block.z
        val dim = block.world

        dataBase.insertPlayerPlacedBlock(
            x,
            y,
            z,
            dim.name,
            material.name
        )
    }
}