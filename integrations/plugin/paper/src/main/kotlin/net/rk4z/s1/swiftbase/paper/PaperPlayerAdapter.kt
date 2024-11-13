package net.rk4z.s1.swiftbase.paper

import net.kyori.adventure.text.TextComponent
import net.rk4z.s1.swiftbase.core.Core
import net.rk4z.s1.swiftbase.core.IPlayer
import net.rk4z.s1.swiftbase.core.LanguageManager
import net.rk4z.s1.swiftbase.core.MessageKey
import org.bukkit.entity.Player
import kotlin.collections.get
import kotlin.reflect.full.isSubclassOf

class PaperPlayer(internal val player: Player) : IPlayer<TextComponent> {
    override fun getLanguage(): String {
        return player.locale().language ?: "en"
    }

    override fun getMessage(key: MessageKey<*, *>, vararg args: Any): TextComponent {
        //この関数が呼び出される時点でLanguageManagerが初期化されていない場合はエラーを出す
        val languageManager = LanguageManager.get<PaperPlayer, TextComponent>()
        if (!LanguageManager.isInitialized()) {
            throw IllegalStateException("LanguageManager is not initialized but you are trying to use it.")
        }

        val messages = languageManager.messages
        val expectedMKType = languageManager.expectedMKType
        val textComponentFactory = languageManager.textComponentFactory

        require(key::class.isSubclassOf(expectedMKType)) { "Unexpected MessageKey type: ${key::class}. Expected: $expectedMKType" }
        val lang = this.getLanguage()
        val message = messages[lang]?.get(key)
        val text = message?.let { String.format(it, *args) } ?: key.rc()
        return textComponentFactory(text)
    }
}

fun Player.adapt(): PaperPlayer {
    return PaperPlayer(this)
}

fun PaperPlayer.getAPlayer(): Player {
    return this.player
}