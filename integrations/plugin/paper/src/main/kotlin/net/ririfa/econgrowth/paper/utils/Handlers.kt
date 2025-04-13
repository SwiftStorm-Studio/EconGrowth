package net.ririfa.econgrowth.paper.utils

import net.ririfa.econgrowth.core.events.DatabaseChangeEvent
import net.ririfa.econgrowth.paper.EconGrowth
import net.rk4z.beacon.IEventHandler
import net.rk4z.beacon.handler
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class Handlers : IEventHandler {
    val onDBChange = handler<DatabaseChangeEvent> { event ->
        val changeInfo = event.changeInfo
        val table = changeInfo.table

        try {
            transaction(EconGrowth.EGDB.memoryDB) {
                val rows = table.selectAll().toList()

                transaction(EconGrowth.EGDB.fileDB) {
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

//            Logger.info("Successfully migrated data from memory DB to file DB for table ${table.tableName}.")
        } catch (e: Exception) {
//            Logger.error("Failed to migrate data for table ${table.tableName}.")
            e.printStackTrace()
        }
    }
}