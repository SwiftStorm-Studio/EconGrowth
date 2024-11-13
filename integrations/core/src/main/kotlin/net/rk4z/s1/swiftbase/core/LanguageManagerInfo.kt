package net.rk4z.s1.swiftbase.core

import kotlin.reflect.KClass

class LanguageManagerInfo<P : IPlayer, C>(
    val textComponentFactory: (String) -> C,
    val expectedType: KClass<out MessageKey<P, C>>,
)