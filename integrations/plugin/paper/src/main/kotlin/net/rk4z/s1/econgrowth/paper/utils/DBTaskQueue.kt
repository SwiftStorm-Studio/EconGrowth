package net.rk4z.s1.econgrowth.paper.utils

import net.rk4z.beacon.EventBus
import net.rk4z.s1.econgrowth.paper.EconGrowth
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
        EconGrowth.get()?.let {
            scheduledExecutor.scheduleAtFixedRate({
                synchronized(eventQueue) {
                    if (eventQueue.isNotEmpty()) {
                        val eventsToSend = eventQueue.toList()
                        eventQueue.clear()
                        eventsToSend.forEach { EventBus.postAsync(DatabaseChangeEvent(it)) }
                    }
                }
                /**
                 * Perhaps this could be improved a bit more.
                 * If anyone has a better idea regarding weight reduction, please let me know in PR.The goal of this mechanism is
                 * to reduce the I/O load as much as possible and efficiently synchronize changes in the Queue to the file.
                 * Currently, we use a fixed interval, but I'm thinking of dynamically changing as well.
                 */
            }, 0, it.syncToFileBatchInterval, TimeUnit.SECONDS)
        }
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

