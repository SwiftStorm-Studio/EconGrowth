@file:Suppress("FunctionName")

package dev.swiftstorm.econgrowth.core.util

import dev.swiftstorm.econgrowth.core.DataBase.Companion.activeInstance
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

fun DBAll(block: Transaction.() -> Unit) {
    val list = listOf(
        activeInstance.db,
        activeInstance.memDb,
    )

    list.forEach {
        transaction(it) { block() }
    }
}

fun <T> DB(block: Transaction.() -> T): T {
    return transaction(activeInstance.memDb, block)
}
