package dev.luckynetwork.alviann.commons

import dev.luckynetwork.alviann.luckyinjector.bungee.BungeeInjector
import dev.luckynetwork.alviann.luckyinjector.spigot.SpigotInjector

@Suppress("unused")
object LuckyCommons {

    /** loads LuckyInjector */
    @JvmStatic
    fun loadInjector(instance: Any) = when (instance::class.java.name) {
        "net.md_5.bungee.api.plugin.Plugin" -> {
            BungeeInjector.loadEarly()
        }
        "org.bukkit.plugin.java.JavaPlugin" -> {
            SpigotInjector.loadEarly()
        }
        else ->
            throw UnsupportedOperationException("Cannot support any other platforms!")
    }

}