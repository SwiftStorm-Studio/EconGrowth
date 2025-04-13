package net.ririfa.econgrowth.core.utils

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ChangeInfo(
    val table: Table,
    val affectedColumns: List<Column<*>>
)

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
    RU("Europe/Moscow"), // ロシア
    NZ("Pacific/Auckland"); // ニュージーランド

    fun getZoneId(): ZoneId = ZoneId.of(timeZone)
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