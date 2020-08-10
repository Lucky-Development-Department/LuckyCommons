@file:JvmName("SpigotUtils")

package dev.luckynetwork.alviann.commons.internal

import dev.luckynetwork.alviann.commons.reflection.ReflectionUtils
import org.bukkit.*
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.roundToInt

// ------------------------------------ //
//                 Player               //
// ------------------------------------ //

/** gets the player's ping, if none is found it's gonna return `0` */
val Player.ping: Int
    get() {
        val player = this
        var ping = 0

        safeRun(false) {
            with(ReflectionUtils) {
                val handle = this.invokeMethod(Player::class.java, player, "getHandle")!!
                ping = this.getField(handle::class.java, handle, "ping") as Int
            }
        }

        return ping
    }

/** respawns a player safely */
fun Player.respawn(plugin: Plugin) {
    Bukkit.getScheduler().runTask(plugin) { this.spigot().respawn() }
}

/** plays a sound */
fun Player.playSound(sound: Sound, volume: Float = 1.0F, pitch: Float = 1.0F) =
    this.playSound(location, sound, volume, pitch)

/** plays a sound */
fun Player.playSound(sound: String, volume: Float = 1.0F, pitch: Float = 1.0F) =
    this.playSound(location, sound, volume, pitch)

/** plays a sound with the [SoundCategory] */
fun Player.playSound(sound: Sound, category: SoundCategory, volume: Float = 1.0F, pitch: Float = 1.0F) =
    this.playSound(location, sound, category, volume, pitch)

/** plays a sound with the [SoundCategory] */
fun Player.playSound(sound: String, category: SoundCategory, volume: Float = 1.0F, pitch: Float = 1.0F) =
    this.playSound(location, sound, category, volume, pitch)

/** sends multiple messages at once */
fun Player.sendMessage(vararg message: String) =
    this.sendMessage(message.toList().joinToString("\n"))

/** gets all nearby players */
fun Player.getNearbyPlayers(x: Double, y: Double, z: Double) =
    this.getNearbyEntities(x, y, z).filterIsInstance<Player>().toImmutable()

// ------------------------------------ //
//                Server                //
// ------------------------------------ //

/** gets the complete raw TPS */
val Server.rawTps: DoubleArray
    get() {
        var tps: DoubleArray?

        SoundCategory.PLAYERS
        with(ReflectionUtils) {
            val serverClass = ReflectionUtils.SPIGOT.getNMSClass("MinecraftServer")
            val instance = this.invokeMethod(serverClass, null, "getServer")

            tps = this.getField(serverClass.superclass, instance, "recentTps") as DoubleArray
        }

        if (tps == null)
            return doubleArrayOf(20.0, 20.0, 20.0)

        return tps!!
    }

/** gets the first index of the [Server.rawTps] and is formatted correctly */
val Server.firstTps
    // original: Math.min(Math.round(tps * 100.0D) / 100.0D, 20.0D)
    get() = ((this.rawTps[0] * 100.0).roundToInt() / 100.0).coerceAtMost(20.0)

/**
 * restarts the server
 *
 * the way it restarts the server is by using the RestartCommand class from and by spigot
 * since the class can't be fetched when using the spigot-api dependency it needs to use a reflection
 * the actual way of invoking or executing the class is by just doing `RestartCommand.restart();`
 *
 * well, since the method is public and static we don't need to set the method accessible
 * and to invoke it we also don't need the class instance, just use a 'null' as an instance will do it perfectly
 */
fun Server.restartServer() {
    val restartCmdClass = Class.forName("org.spigotmc.RestartCommand")
    ReflectionUtils.invokeMethod(restartCmdClass, null, "restart")
}

// ------------------------------------ //
//                Plugin                //
// ------------------------------------ //

/** @see [Server.rawTps] */
val Plugin.rawTps get() = this.server.rawTps

/** @see [Server.firstTps] */
val Plugin.firstTps get() = this.server.firstTps

/** @see [Server.restartServer] */
fun Plugin.restartServer() = this.server.restartServer()

/**
 * registers a command and allow to chain the usage (for kotlin only)
 */
fun JavaPlugin.registerCommand(command: String, executor: CommandExecutor): JavaPlugin {
    this.getCommand(command).executor = executor
    return this
}

/**
 * registers a listener and allow to chain the usage (for kotlin only)
 */
fun JavaPlugin.registerListeners(listener: Listener): JavaPlugin {
    this.server.pluginManager.registerEvents(listener, this)
    return this
}

// ------------------------------------ //
//          InventoryClickEvent         //
// ------------------------------------ //

/** checks if the clicked item is `null` or the clicked inventory is `null` */
val InventoryClickEvent.isItemOrInventoryNull
    get() = this.currentItem == null || this.clickedInventory == null

/** checks if the clicked item is [Material.AIR] */
val InventoryClickEvent.isItemAir
    get() = this.currentItem.type == Material.AIR

/** checks if the clicked slot is outside of the inventory */
val InventoryClickEvent.isClickedOutside
    get() = this.slotType == InventoryType.SlotType.OUTSIDE

// ------------------------------------ //
//             Configuration            //
// ------------------------------------ //

/** shortened version of [ConfigurationSection.getConfigurationSection] */
fun ConfigurationSection.getSection(path: String): ConfigurationSection? =
    this.getConfigurationSection(path)

// ------------------------------------ //
//                 World                //
// ------------------------------------ //

/**
 * deletes the world completely without leaving a single trace
 *
 * @param saveChunks do you want to save the world chunks?
 */
fun World.delete(saveChunks: Boolean = false) {
    val worldFolder = this.worldFolder
    safeRun { Bukkit.unloadWorld(this, saveChunks) }
    worldFolder.deleteRecursively()
}