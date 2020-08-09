package dev.luckynetwork.alviann.commons.closer

import dev.luckynetwork.alviann.commons.internal.safeRun
import java.io.Flushable

class Closer : AutoCloseable {

    private val closeableList = arrayListOf<AutoCloseable?>()

    /**
     * adds a closable instance
     *
     * @param closeable the closeable instance
     */
    fun <T : AutoCloseable> add(closeable: T?): T? {
        if (closeable == null)
            return null

        closeableList.add(closeable)
        return closeable
    }

    /**
     * closes all closable instances
     */
    override fun close() {
        val iterator = closeableList.iterator()

        while (iterator.hasNext()) {
            val next = iterator.next()

            if (next != null) {
                if (next is Flushable)
                    safeRun(false) { (next as Flushable).flush() }

                safeRun { next.close() }
            }

            iterator.remove()
        }

        closeableList.clear()
    }

}