package net.rk4z.s1.econgrowth.core.utils

import net.rk4z.beacon.EventBus
import net.rk4z.s1.econgrowth.core.events.DatabaseChangeEvent
import net.rk4z.s1.swiftbase.core.Logger
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

object DBTaskQueue {
    private val executor = Executors.newSingleThreadExecutor()
    private val eventQueue = CopyOnWriteArrayList<ChangeInfo>()
    private var running = true

    init {
        Thread({
            while (running) {
                try {
                    if (eventQueue.isNotEmpty()) {
                        val eventsToSend = eventQueue.toList()
                        eventQueue.clear()
                        eventsToSend.forEach { EventBus.postAsync(DatabaseChangeEvent(it)) }
                    }
                    Thread.sleep(calculateInterval())
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                } catch (e: Exception) {
                    Logger.error("Error in DBTaskQueue event processing: ${e.message}")
                    e.printStackTrace()
                }
            }
        }, "DBTaskQueue-EventProcessor").start()
    }

    operator fun <T> invoke(changeInfo: ChangeInfo? = null, task: () -> T): T {
        val future = executor.submit(Callable { task() })
        val result = future.get()

        if (changeInfo != null) {
            if (!eventQueue.contains(changeInfo)) {
                eventQueue.add(changeInfo)
            }
        }

        return result
    }

    fun shutdown() {
        running = false
        executor.shutdown()
    }

    private fun calculateInterval(): Long {
        return when {
            eventQueue.isEmpty() -> 30000L // 30 seconds
            eventQueue.size in 1..10 -> 7000L // 7 seconds
            eventQueue.size in 11..50 -> 5000L // 5 seconds
            eventQueue.size in 51..100 -> 2000L // 2 seconds
            else -> 1000L // 1 second
        }
    }
}