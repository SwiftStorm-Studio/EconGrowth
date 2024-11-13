package net.rk4z.s1.swiftbase.core

interface IPlayer<C> {
    fun getLanguage(): String

    fun getMessage(key: MessageKey<*, *>, vararg args: Any): C
}