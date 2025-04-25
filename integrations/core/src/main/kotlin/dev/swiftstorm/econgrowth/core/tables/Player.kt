package dev.swiftstorm.econgrowth.core.tables

import dev.swiftstorm.econgrowth.core.util.DB
import dev.swiftstorm.econgrowth.core.util.ShortUUID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

data class PlayerData(
    val uuid: String,
    val totalXP: Float,
    val xp: Float,
    val level: Int,
    val balance: Double,
    val lastLogin: String
)

object Players : IdTable<String>("players") {
    override val id = varchar("uuid", 22).entityId()
    val totalXP = float("total_xp")
    val xp = float("xp")
    val level = integer("level")
    val balance = double("balance")
    val lastLogin = text("last_login")
}

class Player(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Player>(Players) {
        fun from(data: PlayerData): Player {
            return new(data.uuid) {
                totalXP = data.totalXP
                xp = data.xp
                level = data.level
                balance = data.balance
                lastLogin = data.lastLogin
            }
        }
    }

    var totalXP by Players.totalXP
    var xp by Players.xp
    var level by Players.level
    var balance by Players.balance
    var lastLogin by Players.lastLogin

    fun to(): PlayerData = PlayerData(
        uuid = id.value,
        totalXP = totalXP,
        xp = xp,
        level = level,
        balance = balance,
        lastLogin = lastLogin
    )
}

fun PlayerData.insert(): Player {
    return DB {
        Player.from(this@insert)
    }
}

fun PlayerData.update(): Boolean {
    return DB {
        val entity = Player.findById(this@update.uuid) ?: return@DB false
        entity.totalXP = totalXP
        entity.xp = xp
        entity.level = level
        entity.balance = balance
        entity.lastLogin = lastLogin
        true
    }
}

fun Player.deletePlayer(): Boolean {
    this.delete()
    return true
}

fun ShortUUID.getPlayer(): PlayerData? {
    return DB {
        Player.findById(this@getPlayer.toShortString())?.to()
    }
}
