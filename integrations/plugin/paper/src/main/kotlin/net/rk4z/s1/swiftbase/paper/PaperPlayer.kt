@file:Suppress("DEPRECATION", "unused")

package net.rk4z.s1.swiftbase.paper

import net.kyori.adventure.text.TextComponent
import net.rk4z.s1.swiftbase.core.IPlayer
import net.rk4z.s1.swiftbase.core.LanguageManager
import net.rk4z.s1.swiftbase.core.MessageKey
import org.bukkit.entity.Player
import kotlin.collections.get
import kotlin.reflect.full.isSubclassOf

@Suppress("DEPRECATION")
class PaperPlayer(internal val player: Player) : IPlayer<TextComponent> {
    private val languageManager: LanguageManager<PaperPlayer, TextComponent>
        get() {
            //この関数が呼び出される時点でLanguageManagerが初期化されていない場合はエラーを出す
            if (!LanguageManager.isInitialized()) {
                throw IllegalStateException("LanguageManager is not initialized but you are trying to use it.")
            }
            val languageManager = LanguageManager.get<PaperPlayer, TextComponent>()
            return languageManager
        }

    override fun getLanguage(): String {
        return player.locale().language ?: "en"
    }

    override fun getMessage(key: MessageKey<*, *>, vararg args: Any): TextComponent {
        val messages = languageManager.messages
        val expectedMKType = languageManager.expectedMKType
        val textComponentFactory = languageManager.textComponentFactory

        require(key::class.isSubclassOf(expectedMKType)) { "Unexpected MessageKey type: ${key::class}. Expected: $expectedMKType" }
        val lang = this.getLanguage()
        val message = messages[lang]?.get(key)
        val text = message?.let { String.format(it, *args) } ?: key.rc()
        return textComponentFactory(text)
    }

    override fun getRawMessage(key: MessageKey<*, *>): String {
        val messages = languageManager.messages
        val expectedMKType = languageManager.expectedMKType

        require(key::class.isSubclassOf(expectedMKType)) { "Unexpected MessageKey type: ${key::class}. Expected: $expectedMKType" }
        val lang = this.getLanguage()
        return messages[lang]?.get(key) ?: key.rc()
    }

    override fun hasMessage(key: MessageKey<*, *>): Boolean {
        val messages = languageManager.messages
        val expectedMKType = languageManager.expectedMKType

        require(key::class.isSubclassOf(expectedMKType)) { "Unexpected MessageKey type: ${key::class}. Expected: $expectedMKType" }
        val lang = this.getLanguage()
        return messages[lang]?.containsKey(key) ?: false
    }
}

fun Player.adapt(): PaperPlayer {
    return PaperPlayer(this)
}

fun PaperPlayer.getAPlayer(): Player {
    return this.player
}