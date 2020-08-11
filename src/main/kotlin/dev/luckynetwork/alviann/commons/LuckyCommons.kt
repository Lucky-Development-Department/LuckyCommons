package dev.luckynetwork.alviann.commons

import com.github.alviannn.sqlhelper.SQLBuilder
import dev.luckynetwork.alviann.luckyinjector.bungee.BungeeInjector
import dev.luckynetwork.alviann.luckyinjector.spigot.SpigotInjector

@Suppress("unused")
object LuckyCommons {

    /** loads LuckyInjector dependencies depending on it's platform */
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

    /** gets the default [SQLBuilder] object depending on it's platform */
    fun getDefaultSQLBuilder(instance: Any): SQLBuilder = when (instance::class.java.name) {
        "net.md_5.bungee.api.plugin.Plugin" -> {
            BungeeInjector.getDefaultSQLBuilder()
        }
        "org.bukkit.plugin.java.JavaPlugin" -> {
            SpigotInjector.getDefaultSQLBuilder()
        }
        else ->
            throw UnsupportedOperationException("Cannot support any other platforms!")
    }

}