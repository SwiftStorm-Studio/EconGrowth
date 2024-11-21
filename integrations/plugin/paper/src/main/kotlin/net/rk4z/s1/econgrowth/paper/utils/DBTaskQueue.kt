package net.rk4z.s1.econgrowth.paper.utils

import net.rk4z.beacon.EventBus
import net.rk4z.s1.econgrowth.paper.events.DatabaseChangeEvent
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object DBTaskQueue {
    private val executor = Executors.newSingleThreadExecutor()
    private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private val eventQueue = mutableListOf<ChangeInfo>()

    init {
        scheduledExecutor.scheduleAtFixedRate({
            synchronized(eventQueue) {
                if (eventQueue.isNotEmpty()) {
                    val eventsToSend = eventQueue.toList()
                    eventQueue.clear()
                    eventsToSend.forEach { EventBus.postAsync(DatabaseChangeEvent(it)) }
                }
            }
        }, 0, 1, TimeUnit.SECONDS)
    }

    operator fun <T> invoke(changeInfo: ChangeInfo? = null, task: () -> T): T {
        val future = executor.submit(Callable { task() })
        val result = future.get()

        if (changeInfo != null) {
            synchronized(eventQueue) {
                if (!eventQueue.contains(changeInfo)) {
                    eventQueue.add(changeInfo)
                }
            }
        }

        return result
    }
}

