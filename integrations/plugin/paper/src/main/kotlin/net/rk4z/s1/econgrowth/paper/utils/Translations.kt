@file:Suppress("ClassName", "unused")

package net.rk4z.s1.econgrowth.paper.utils

import net.rk4z.s1.swiftbase.paper.PaperMessageKey

open class Main : PaperMessageKey {
    open class Specializations : Main() {
        open class Miner : Specializations() {
            object NAME : Miner()
            open class Description : Miner() {
                object ITEM_0 : Description()
                object ITEM_1 : Description()
                object ITEM_2 : Description()
            }
        }
    }
}

open class System : PaperMessageKey {
    open class Log : System() {
        object LOADING : Log()
        object ENABLING : Log()
        object DISABLING : Log()

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