package dev.swiftstorm.econgrowth.core.logging

interface EconGrowthLogger {
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}