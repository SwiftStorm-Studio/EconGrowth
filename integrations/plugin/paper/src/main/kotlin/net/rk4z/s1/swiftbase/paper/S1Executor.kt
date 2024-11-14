package net.rk4z.s1.swiftbase.paper

import net.rk4z.s1.swiftbase.core.S0Executor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("DuplicatedCode")
class S1Executor internal constructor(private val plugin: JavaPlugin) : S0Executor {
    private val isShutdown = AtomicBoolean(false)
    private val runningTasks = mutableListOf<Future<*>>()

    override fun <T> submit(task: Callable<T>): Future<T> {
        checkShutdown()
        val future = Bukkit.getScheduler().callSyncMethod(plugin, task)
        runningTasks.add(future)
        return future
    }

    override fun <T> submitAsync(task: Callable<T>): Future<T> {
        checkShutdown()
        val future = CompletableFuture<T>()

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                future.complete(task.call())
            } catch (e: Exception) {
                future.completeExceptionally(RuntimeException("Task execution failed", e))
            }
        })

        runningTasks.add(future)
        return future
    }

    override fun execute(task: Runnable) {
        checkShutdown()
        Bukkit.getScheduler().runTask(plugin, task)
        runningTasks.add(CompletableFuture.runAsync(task))
    }

    override fun executeAsync(task: Runnable) {
        checkShutdown()
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task)
        runningTasks.add(CompletableFuture.runAsync(task))
    }

    override fun executeTimer(task: Runnable, delay: Long, period: Long) {
        checkShutdown()
        Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period)
        runningTasks.add(CompletableFuture.runAsync(task))
    }

    override fun executeAsyncTimer(task: Runnable, delay: Long, period: Long) {
        checkShutdown()
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period)
        runningTasks.add(CompletableFuture.runAsync(task))
    }

    override fun schedule(task: Runnable, delay: Long) {
        checkShutdown()
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay)
        runningTasks.add(CompletableFuture.runAsync(task))
    }

    override fun scheduleAtFixedRate(task: Runnable, delay: Long, period: Long) {
        checkShutdown()
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period)
    }

    @Throws(TimeoutException::class, InterruptedException::class, ExecutionException::class)
    override fun <T> submitWithTimeout(task: Callable<T>, timeout: Long, timeUnit: TimeUnit): T? {
        checkShutdown()
        val future = Bukkit.getScheduler().callSyncMethod(plugin, task)
        runningTasks.add(future)
        return try {
            future.get(timeout, timeUnit)
        } catch (e: TimeoutException) {
            future.cancel(true)
            throw e
        }
    }

    override fun <T> submitWithDelay(task: Callable<T>, delay: Long): Future<T> {
        checkShutdown()
        val future = Bukkit.getScheduler().callSyncMethod(plugin, task)
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable { runningTasks.add(future) }, delay)
        return future
    }

    @Throws(InterruptedException::class)
    override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>> {
        checkShutdown()
        val futures = tasks.map { task -> submit(task) }
        return futures
    }

    @Throws(InterruptedException::class)
    override fun awaitTermination(timeout: Long, timeUnit: TimeUnit): Boolean {
        val endTime = System.nanoTime() + timeUnit.toNanos(timeout)
        while (System.nanoTime() < endTime) {
            if (runningTasks.all { it.isDone || it.isCancelled }) {
                return true
            }
            Thread.sleep(100) // Polling delay
        }
        return false
    }

    override fun cancelAll(mayInterruptIfRunning: Boolean) {
        runningTasks.forEach { it.cancel(mayInterruptIfRunning) }
    }

    override fun isShutdown(): Boolean {
        return isShutdown.get()
    }

    override fun shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            runningTasks.forEach { it.cancel(true) }
            runningTasks.clear()
        }
    }

    override fun checkShutdown() {
        if (isShutdown.get()) {
            throw RejectedExecutionException("Executor has been shut down")
        }
    }
}