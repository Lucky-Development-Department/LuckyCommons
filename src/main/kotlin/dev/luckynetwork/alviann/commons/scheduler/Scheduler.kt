package dev.luckynetwork.alviann.commons.scheduler

import dev.luckynetwork.alviann.commons.internal.safeRun
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class Scheduler : AutoCloseable {

    val executor: ExecutorService = Executors.newCachedThreadPool()
    val dispatcher = executor.asCoroutineDispatcher()

    /** determines if the scheduler is terminated */
    val isTerminated
        get() = executor.isTerminated || executor.isTerminated

    /** the existing task list */
    val tasks: MutableSet<ScheduledTask<*>> = ConcurrentHashMap.newKeySet<ScheduledTask<*>>()

    /**
     * terminates the scheduler
     */
    fun terminate() {
        tasks.forEach { safeRun { it.cancel() } }
        tasks.clear()
        safeRun { executor.shutdownNow() }
        safeRun { dispatcher.close() }
    }

    override fun close() = this.terminate()

    /**
     * starts a repeating task that
     *
     * @throws IllegalArgumentException if the [initialDelay] or [period] is anywhere below 0
     */
    @JvmOverloads
    fun scheduleRepeating(
        block: () -> Unit,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit = TimeUnit.MILLISECONDS
    ) = ScheduledTask(
        block, ScheduledTask.Type.REPEATING,
        this,
        unit.toMillis(initialDelay), unit.toMillis(period)
    ).run()

    /**
     * starts a delayed task that
     *
     * @throws IllegalArgumentException if the [delay] is anywhere below 0
     */
    @JvmOverloads
    fun <T> scheduleDelay(
        block: () -> T,
        delay: Long,
        unit: TimeUnit = TimeUnit.MILLISECONDS
    ) = ScheduledTask(
        block, ScheduledTask.Type.DELAY,
        this,
        unit.toMillis(delay), 0L
    ).run()

}