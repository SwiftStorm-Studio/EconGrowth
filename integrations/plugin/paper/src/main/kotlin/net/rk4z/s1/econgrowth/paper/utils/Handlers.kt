package net.rk4z.s1.econgrowth.paper.utils

import net.rk4z.beacon.IEventHandler
import net.rk4z.beacon.handler
import net.rk4z.s1.econgrowth.core.events.DatabaseChangeEvent
import net.rk4z.s1.econgrowth.paper.EconGrowth
import net.rk4z.s1.swiftbase.core.Logger
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class Handlers : IEventHandler {
    val egdb = EconGrowth.EGDB

    val onDBChange = handler<DatabaseChangeEvent> { event ->
        val changeInfo = event.changeInfo
        val table = changeInfo.table

        try {
            transaction(egdb.memoryDB) {
                val rows = table.selectAll().toList()

                transaction(egdb.fileDB) {
                    rows.forEach { row ->
                        table.insert { insertStatement ->
                            table.columns.forEach { column ->
                                @Suppress("UNCHECKED_CAST")
                                insertStatement[column as Column<Any>] = row[column]
                            }
                        }
                    }
                }
            }

            Logger.info("Successfully migrated data from memory DB to file DB for table ${table.tableName}.")
        } catch (e: Exception) {
            Logger.error("Failed to migrate data for table ${table.tableName}.")
            e.printStackTrace()
        }
    }
}