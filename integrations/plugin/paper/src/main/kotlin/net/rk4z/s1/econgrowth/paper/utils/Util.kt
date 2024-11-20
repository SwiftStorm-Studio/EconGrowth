@file:Suppress("unused")

package net.rk4z.s1.econgrowth.paper.utils

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ClipContext.Block
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class Country(val timeZone: String) {
    UTC("UTC"), // 世界標準時
    US("America/New_York"), // アメリカ
    GB("Europe/London"), // イギリス
    JP("Asia/Tokyo"), // 日本
    AU("Australia/Sydney"), // オーストラリア
    DE("Europe/Berlin"), // ドイツ
    FR("Europe/Paris"), // フランス
    CA("America/Toronto"), // カナダ
    CN("Asia/Shanghai"), // 中国
    IN("Asia/Kolkata"), // インド
    BR("America/Sao_Paulo"), // ブラジル
    ZA("Africa/Johannesburg"), // 南アフリカ
    NZ("Pacific/Auckland"); // ニュージーランド

    fun getZoneId(): ZoneId = ZoneId.of(timeZone)
}

fun getTimeByCountry(country: Country = Country.UTC): String {
    return try {
        // 現在の日時を指定されたタイムゾーンに変換
        val now = LocalDateTime.now()
        val zonedDateTime = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(country.getZoneId())

        // タイムゾーンの日時をそのまま表示
        val localTime = zonedDateTime.toLocalDateTime()

        // フォーマット
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        localTime.format(formatter)
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

// UTCの日時を指定された国の時間に変換
fun getTimeFromUTC(utcTime: String, country: Country): String {
    return try {
        // UTC形式の日時を解析
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val utcDateTime = LocalDateTime.parse(utcTime, formatter)

        // UTC -> ZonedDateTimeに変換
        val utcZoned = utcDateTime.atZone(ZoneId.of("UTC"))

        // 指定された国のタイムゾーンに変換
        val targetZoned = utcZoned.withZoneSameInstant(country.getZoneId())

        // タイムゾーンの日時をフォーマット
        targetZoned.format(formatter)
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

fun Entity.isPlayer(): Boolean {
    return this is Player
}

fun Entity.getPlayer(): Player? {
    return this as? Player
}

fun Player.toNMSPlayer(): ServerPlayer? {
    return (this as? CraftPlayer)?.handle
}

fun ServerPlayer.toPaperPlayer(): CraftPlayer {
    return this.bukkitEntity
}

fun Material.isDeepSlate(): Boolean {
    // あんまりよくないけど現状は全深層系ブロックが名前にDEEPSLATEを含むのでこれで判定
    return this.name.contains("DEEPSLATE")
}