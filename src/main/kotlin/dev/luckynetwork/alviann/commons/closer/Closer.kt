package dev.luckynetwork.alviann.commons.closer

import dev.luckynetwork.alviann.commons.internal.safeRun
import java.io.Flushable

class Closer : AutoCloseable {

    private val closeableList = arrayListOf<AutoCloseable>()

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
        safeRun(false) {
            val iterator = closeableList.iterator()

            while (iterator.hasNext()) {
                val item = iterator.next()

                if (item is Flushable)
                    safeRun(false) { item.flush() }

                safeRun { item.close() }
                iterator.remove()
            }
        }

        closeableList.clear()
    }

}