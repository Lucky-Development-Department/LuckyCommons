package dev.luckynetwork.alviann.commons.scheduler

import dev.luckynetwork.alviann.commons.internal.runAsync
import dev.luckynetwork.alviann.commons.internal.safeRun
import dev.luckynetwork.alviann.commons.internal.safeSleep
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("MemberVisibilityCanBePrivate")
class ScheduledTask<T>(
    /** the block to run the task */
    val block: () -> T,
    /** the type of the task */
    val type: Type,
    /** the scheduler parent */
    private val parent: Scheduler,
    private val delay: Long,
    private val period: Long
) {

    private var cancelled = AtomicBoolean(false)
    private var done = AtomicBoolean(false)
    private var running = AtomicBoolean(false)

    private lateinit var future: CompletableFuture<T?>

    /** determines if the task is cancelled */
    val isCancelled get() = cancelled.get()

    /** determines if the task has finished */
    val isDone get() = done.get()

    /** determines if the task is currently running */
    val isRunning get() = running.get()

    init {
        require(delay >= 0L) { "The task delay mustn't be any lower than 0!" }
        require(period >= 0L) { "The task period mustn't be any lower than 0!" }

        parent.tasks.add(this)
    }

    /**
     * waits until the task is finished to get the final result from the task
     */
    fun await(): T? {
        if (!isRunning)
            throw IllegalStateException("The task isn't currently running!")
        if (type == Type.REPEATING)
            throw IllegalStateException("Cannot wait a repeating task!")

        return future.get()
    }

    /**
     * cancels the task
     *
     * @return `true` if this task is cancelled, `false` is otherwise
     */
    fun cancel(): Boolean {
        if (isCancelled || isDone)
            return true

        running.set(false)
        cancelled.set(true)
        done.set(true)
        parent.tasks.remove(this)

        return future.cancel(true)
    }

    /**
     * runs the scheduled task based on it's type
     */
    fun run(): ScheduledTask<T> {
        if (parent.isTerminated)
            throw IllegalStateException("The parent (Scheduler class) is terminated!")

        when (type) {
            Type.DELAY -> {
                future = runAsync(parent.dispatcher) {
                    running.set(true)

                    // runs the delay
                    safeSleep(delay)
                    // runs the block
                    val result = safeRun { block() }

                    running.set(false)
                    done.set(true)
                    parent.tasks.remove(this)

                    return@runAsync result
                }
            }
            Type.REPEATING -> {
                val initialDone = AtomicBoolean(false)

                future = runAsync(parent.dispatcher) {
                    running.set(true)

                    var result: T? = null
                    while (true) {
                        if (parent.isTerminated || isCancelled)
                            break

                        // runs the initial delay
                        if (!initialDone.get())
                            safeSleep(delay)
                        // otherwise runs the period delay
                        else
                            safeSleep(period)

                        result = safeRun { block() }

                        if (parent.isTerminated || isCancelled)
                            break
                    }

                    running.set(false)
                    done.set(true)
                    parent.tasks.remove(this)

                    return@runAsync result
                }
            }
        }

        return this
    }

    enum class Type {

        /**
         * the task will be run after a certain amount of delay
         */
        DELAY,

        /**
         * the task itself is self repeating and will always keep on repeating
         * unless it's cancelled
         */
        REPEATING;

    }

}
