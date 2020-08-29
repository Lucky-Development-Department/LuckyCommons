package dev.luckynetwork.alviann.commons.hooks

interface Hook : AutoCloseable {

    /** the plugin name */
    val name: String

    /**
     * checks if the dependency is hooked
     *
     * @return `true` if it's hooked, `false` is otherwise
     */
    val isHooked: Boolean

    /** initializes the hook */
    fun initialize()

    /** shuts down the hook */
    fun shutdown()

    override fun close() = this.shutdown()

}