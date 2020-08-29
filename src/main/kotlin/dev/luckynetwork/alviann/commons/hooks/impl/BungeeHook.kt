package dev.luckynetwork.alviann.commons.hooks.impl

import dev.luckynetwork.alviann.commons.hooks.Hook
import net.md_5.bungee.api.ProxyServer

abstract class BungeeHook : Hook {

    override val isHooked
        get() = ProxyServer.getInstance().pluginManager.getPlugin(name) != null

}