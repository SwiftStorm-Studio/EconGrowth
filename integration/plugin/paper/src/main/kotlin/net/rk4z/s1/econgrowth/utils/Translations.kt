@file:Suppress("ClassName")

package net.rk4z.s1.econgrowth.utils

import net.rk4z.s1.swiftbase.MessageKey

open class Main : MessageKey {



}

open class System : MessageKey {
    open class Error : System() {
        object SERVER_SHOULD_BE_PAPER : Error()
    }

    open class Log : System() {
        object CHECKING_UPDATE : Log()
        object ALL_VERSION_COUNT : Log()
        object NEW_VERSION_COUNT : Log()
        object VIEW_LATEST_VER : Log()
        object LATEST_VERSION_FOUND : Log()
        object YOU_ARE_USING_LATEST : Log()
        object FAILED_TO_CHECK_UPDATE : Log()
        object ERROR_WHILE_CHECKING_UPDATE : Log()

        open class Other : Log() {
            object UNKNOWN : Other()
            object UNKNOWN_ERROR : Other()
            object ERROR : Other()
        }
    }
}