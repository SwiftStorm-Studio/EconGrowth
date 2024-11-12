package net.rk4z.s1.swiftbase.core

import java.util.concurrent.*

/**
 * Represents an executor interface for handling synchronous and asynchronous task execution,
 * including scheduling tasks with delays and periodic execution. This interface provides
 * methods for submitting tasks with or without timeouts, as well as managing task termination
 * and shutdown states.
 */
interface S0Executor {
    /**
     * Submits a Callable task for synchronous execution on the main thread.
     *
     * @param task The Callable task to be executed.
     * @return A Future representing the result of the task.
     */
    fun <T> submit(task: Callable<T>): Future<T>

    /**
     * Submits a Callable task for asynchronous execution on a separate thread.
     *
     * @param task The Callable task to be executed.
     * @return A Future representing the pending result of the task.
     */
    fun <T> submitAsync(task: Callable<T>): Future<T>

    /**
     * Executes a Runnable task synchronously on the main thread.
     *
     * @param task The Runnable task to be executed.
     */
    fun execute(task: Runnable)

    /**
     * Executes a Runnable task asynchronously on a separate thread.
     *
     * @param task The Runnable task to be executed asynchronously.
     */
    fun executeAsync(task: Runnable)

    /**
     * Schedules a Runnable task for repeated synchronous execution on the main thread.
     *
     * @param task The Runnable task to be executed.
     * @param delay The initial delay (in milliseconds) before the task is first executed.
     * @param period The interval (in milliseconds) between consecutive executions of the task.
     */
    fun executeTimer(task: Runnable, delay: Long, period: Long)

    /**
     * Schedules a Runnable task for repeated asynchronous execution on a separate thread.
     *
     * @param task The Runnable task to be executed asynchronously.
     * @param delay The initial delay (in milliseconds) before the task is first executed.
     * @param period The interval (in milliseconds) between consecutive executions of the task.
     */
    fun executeAsyncTimer(task: Runnable, delay: Long, period: Long)

    /**
     * Schedules a Runnable task for asynchronous execution after a specified delay.
     *
     * @param task The Runnable task to be executed.
     * @param delay The delay (in milliseconds) before the task is executed.
     */
    fun schedule(task: Runnable, delay: Long)

    /**
     * Schedules a Runnable task for repeated asynchronous execution at a fixed rate.
     *
     * @param task The Runnable task to be executed.
     * @param delay The initial delay (in milliseconds) before the task is first executed.
     * @param period The interval (in milliseconds) between consecutive executions of the task.
     */
    fun scheduleAtFixedRate(task: Runnable, delay: Long, period: Long)

    /**
     * Submits a Callable task for asynchronous execution and waits for its result with a specified timeout.
     * If the task does not complete within the timeout, a TimeoutException is thrown.
     *
     * @param task The Callable task to be executed.
     * @param timeout The maximum time to wait for the task to complete.
     * @param timeUnit The time unit of the timeout argument.
     * @return The result of the task, or null if the timeout is reached.
     * @throws TimeoutException If the task does not complete within the specified timeout.
     */
    fun <T> submitWithTimeout(task: Callable<T>, timeout: Long, timeUnit: TimeUnit): T?

    /**
     * Submits a Callable task for asynchronous execution after a specified delay.
     *
     * @param task The Callable task to be executed.
     * @param delay The delay (in milliseconds) before the task is executed.
     * @return A Future representing the pending result of the task.
     */
    fun <T> submitWithDelay(task: Callable<T>, delay: Long): Future<T>

    /**
     * Submits a collection of Callable tasks for concurrent asynchronous execution.
     * This method blocks until all tasks have completed.
     *
     * @param tasks The collection of Callable tasks to execute.
     * @return A list of Future objects representing the results of the tasks.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>

    /**
     * Waits for all running tasks to complete or until the specified timeout occurs.
     *
     * @param timeout The maximum time to wait for all tasks to complete.
     * @param timeUnit The time unit of the timeout argument.
     * @return `true` if all tasks completed before the timeout, otherwise `false`.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    fun awaitTermination(timeout: Long, timeUnit: TimeUnit): Boolean

    /**
     * Cancels all currently running or scheduled tasks.
     *
     * @param mayInterruptIfRunning If `true`, tasks that are currently running are interrupted.
     */
    fun cancelAll(mayInterruptIfRunning: Boolean)

    /**
     * Checks if the Executor has been shut down.
     *
     * @return `true` if the Executor has been shut down, otherwise `false`.
     */
    fun isShutdown(): Boolean

    /**
     * Initiates an orderly shutdown of the Executor. This cancels all pending tasks
     * and rejects any new tasks submitted to the Executor.
     */
    fun shutdown()

    /**
     * Checks if the Executor has been shut down and throws a RejectedExecutionException
     * if it has been shut down, preventing further task execution.
     *
     * @throws RejectedExecutionException If the Executor has been shut down.
     */
    fun checkShutdown()
}