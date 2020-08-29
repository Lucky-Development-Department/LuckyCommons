package dev.luckynetwork.alviann.commons.hooks.impl

import dev.luckynetwork.alviann.commons.hooks.Hook
import org.bukkit.Bukkit

abstract class SpigotHook : Hook {

    override val isHooked
        get() = Bukkit.getPluginManager().getPlugin(name) != null

}