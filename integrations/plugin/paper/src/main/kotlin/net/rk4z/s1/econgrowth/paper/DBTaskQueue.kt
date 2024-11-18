package net.rk4z.s1.econgrowth.paper

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

object DBTaskQueue {
    private val executor = Executors.newSingleThreadExecutor()
    private val queue = LinkedBlockingQueue<() -> Unit>()

    init {
        executor.submit {
            while (true) {
                try {
                    val task = queue.take()
                    task.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    operator fun <T> invoke(task: () -> T): T {
        val future = executor.submit(Callable { task() })
        return try {
            future.get()
        } catch (e: Exception) {
            throw RuntimeException("Task execution failed", e)
        }
    }
}
