package net.rk4z.s1.swiftbase.core

import kotlin.reflect.KClass

object SBHelper {
    /**
     * Create a new LanguageManager instance.
     * If you're using [Core], the language manager will automatically be created.
     *
     * @param textComponentFactory The factory to create a new text component.
     * @param expectedType The expected type of the message key.
     * @return The created language manager.
     */
    fun <P : IPlayer, C> crateLanguageManager(
        textComponentFactory: (String) -> C,
        expectedType: KClass<out MessageKey<P, C>>
    ): LanguageManager<P, C> {
        val languageManager = LanguageManager(textComponentFactory, expectedType)

        LanguageManager.instance = languageManager

        return languageManager
    }
}
