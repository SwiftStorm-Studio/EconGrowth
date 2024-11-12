package net.rk4z.s1.swiftbase.core

import kotlin.reflect.KClass

object SBHelper {
    fun <P : IPlayer, C> crateLanguageManager(
        textComponentFactory: (String) -> C,
        expectedType: KClass<out MessageKey<P, C>>
    ): LanguageManager<P, C> {
        val languageManager = LanguageManager(textComponentFactory, expectedType)

        LanguageManager.instance = languageManager

        return languageManager
    }
}
