package dev.luckynetwork.alviann.commons.internal

import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginManager
import net.md_5.bungee.event.EventHandler

/**
 * Get the [PluginManager] associated with loading plugins and
 * dispatching events. It is recommended that implementations use the
 * provided PluginManager class.
 */
val Plugin.pluginManager get() = this.proxy.pluginManager!!

/**
 * Register command(s) so that it may be executed.
 *
 * @param command the command to register
 */
@JvmSynthetic
fun Plugin.registerCommand(vararg command: Command) =
    command.forEach { this.pluginManager.registerCommand(this, it) }

/**
 * Register listener(s) for receiving called events. Methods in this
 * Object which wish to receive events must be annotated with the
 * [EventHandler] annotation.
 *
 * @param listener the listener to register events for
 */
@JvmSynthetic
fun Plugin.registerListeners(vararg listener: Listener) =
    listener.forEach { this.pluginManager.registerListener(this, it) }
