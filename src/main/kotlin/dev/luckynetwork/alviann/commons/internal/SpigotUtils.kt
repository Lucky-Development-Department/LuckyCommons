@file:JvmName("SpigotUtils")
@file:Suppress("unused")

package dev.luckynetwork.alviann.commons.internal

import dev.luckynetwork.alviann.commons.builder.ItemBuilder
import dev.luckynetwork.alviann.commons.reflection.Reflections
import net.md_5.bungee.api.ChatColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.roundToInt

// ------------------------------------ //
//                 Player               //
// ------------------------------------ //

/** Gets the player's ping, if none is found it's gonna return `0` */
val Player.ping: Int
    get() {
        val player = this
        var ping = 0

        safeRun(false) {
            with(Reflections) {
                val handle = this.findMethod(Player::class.java, "getHandle")!!.invoke(player)!!
                ping = this.getField(handle::class.java, handle, "ping") as Int
            }
        }

        return ping
    }

/**
 * Sends a packet to player
 */
fun Player.sendPacket(packet: Any) {
    val handle = this.javaClass.getMethod("getHandle").invoke(this)
    val playerConnection = handle.javaClass.getField("playerConnection").get(handle)
    playerConnection.javaClass.getMethod(
        "sendPacket",
        Reflections.SPIGOT.getNMSClass("Packet")
    ).invoke(playerConnection, packet)
}

/**
 * Sends a title and subtitle using reflection
 *
 * @param title     the title text
 * @param subTitle  the subtitle text
 * @param fadeIn    the time the title takes to fade in
 * @param show      the time the title is shown
 * @param fadeOut   the time the title takes to fade out
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun Player.sendTitle(title: String, subTitle: String, fadeIn: Int, show: Int, fadeOut: Int) {
    this.sendTitle(title, fadeIn, show, fadeOut)
    this.sendSubTitle(subTitle, fadeIn, show, fadeOut, title)
}

/**
 * Sends a title using reflection
 *
 * @param title     the text
 * @param fadeIn    the time the title takes to fade in
 * @param show      the time the title is shown
 * @param fadeOut   the time the title takes to fade out
 */
fun Player.sendTitle(title: String, fadeIn: Int, show: Int, fadeOut: Int) {
    val titleTextComponent = Reflections.SPIGOT.getNMSClass("IChatBaseComponent").declaredClasses[0]
        .getMethod("a", String::class.java)
        .invoke(null, "{\"text\":\"${title.colorize()}\"}")

    var component = Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").declaredClasses[0]
        .getField("TIMES")
        .get(null)

    var titleConstructor = Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").getConstructor(
        Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").declaredClasses[0],
        Reflections.SPIGOT.getNMSClass("IChatBaseComponent"),
        Integer.TYPE,
        Integer.TYPE,
        Integer.TYPE
    )

    var titlePacket = titleConstructor.newInstance(component, titleTextComponent, fadeIn, show, fadeOut)
    this.sendPacket(titlePacket)

    component = Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").declaredClasses[0]
        .getField("TITLE")
        .get(null)

    titleConstructor = Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").getConstructor(
        Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").declaredClasses[0],
        Reflections.SPIGOT.getNMSClass("IChatBaseComponent")
    )

    titlePacket = titleConstructor.newInstance(component, titleTextComponent)
    this.sendPacket(titlePacket)
}

/**
 * sends a sub-title using reflection
 *
 * @param subTitle  the text
 * @param fadeIn    the time the title takes to fade in (in seconds)
 * @param show      the time the title is shown (in seconds)
 * @param fadeOut   the time the title takes to fade out (in seconds)
 * @param _title    the title text(if any)
 */
fun Player.sendSubTitle(subTitle: String, fadeIn: Int, show: Int, fadeOut: Int, _title: String? = null) {
    val title = _title ?: " "

    val titleComponent = Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").declaredClasses[0]
        .getField("TIMES")
        .get(null)

    val titleTextComponent = Reflections.SPIGOT.getNMSClass("IChatBaseComponent").declaredClasses[0]
        .getMethod("a", String::class.java)
        .invoke(null, "{\"text\":\"${title.colorize()}\"}")

    val titleConstructor = Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").getConstructor(
        Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").declaredClasses[0],
        Reflections.SPIGOT.getNMSClass("IChatBaseComponent"),
        Integer.TYPE,
        Integer.TYPE,
        Integer.TYPE
    )

    val titlePacket = titleConstructor.newInstance(titleComponent, titleTextComponent, fadeIn, show, fadeOut)
    this.sendPacket(titlePacket)

    val subTitleComponent = Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").declaredClasses[0]
        .getField("SUBTITLE")
        .get(null)

    val subTitleTextComponent = Reflections.SPIGOT.getNMSClass("IChatBaseComponent").declaredClasses[0]
        .getMethod("a", String::class.java)
        .invoke(null, "{\"text\":\"${subTitle.colorize()}\"}")

    val subTitleConstructor = Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").getConstructor(
        Reflections.SPIGOT.getNMSClass("PacketPlayOutTitle").declaredClasses[0],
        Reflections.SPIGOT.getNMSClass("IChatBaseComponent"),
        Integer.TYPE,
        Integer.TYPE,
        Integer.TYPE
    )

    val subTitlePacket = subTitleConstructor.newInstance(subTitleComponent, subTitleTextComponent, fadeIn, show, fadeOut)
    this.sendPacket(subTitlePacket)
}

/** Respawns the player safely */
fun Player.safeRespawn(plugin: Plugin) = Bukkit.getScheduler().runTask(plugin) { this.spigot().respawn() }!!

/** Respawns the player */
fun Player.respawn() = this.spigot().respawn()

/** Safely kicks players from the server */
fun Player.safeKick(plugin: Plugin, message: String) =
    Bukkit.getScheduler().runTask(plugin) { this.kickPlayer(message) }!!

/** Plays a sound */
@JvmOverloads
fun Player.playSound(sound: Sound, volume: Float = 1.0F, pitch: Float = 1.0F) =
    this.playSound(location, sound, volume, pitch)

/** Plays a sound */
@JvmOverloads
fun Player.playSound(sound: String, volume: Float = 1.0F, pitch: Float = 1.0F) =
    this.playSound(location, sound, volume, pitch)

/** Plays a sound with the [SoundCategory] */
@JvmOverloads
fun Player.playSound(sound: Sound, category: SoundCategory, volume: Float = 1.0F, pitch: Float = 1.0F) =
    this.playSound(location, sound, category, volume, pitch)

/** Plays a sound with the [SoundCategory] */
@JvmOverloads
fun Player.playSound(sound: String, category: SoundCategory, volume: Float = 1.0F, pitch: Float = 1.0F) =
    this.playSound(location, sound, category, volume, pitch)

/** Sends multiple messages at once */
fun Player.sendMessage(vararg message: String) =
    this.sendMessage(message.toList().joinToString("\n"))

/** Gets all nearby players */
fun Player.getNearbyPlayers(x: Double, y: Double, z: Double) =
    this.getNearbyEntities(x, y, z).filterIsInstance<Player>().toImmutable()

/**
 * Clears the player's inventory
 *
 * @param clearArmor do you also want to clear the player armor?
 */
@JvmOverloads
fun Player.clearInventory(clearArmor: Boolean = false) {
    this.inventory.clear()
    if (clearArmor)
        this.inventory.armorContents = null
}

/** Clears the currently active potion effects */
fun Player.clearPotionEffects() = this.activePotionEffects.forEach { this.removePotionEffect(it.type) }

/**
 * Heals the player (restores the player's health)
 *
 * @param foodLevel do you also want to restores the player's food level?
 */
@Suppress("DEPRECATION")
@JvmOverloads
fun Player.heal(foodLevel: Boolean = false) {
    this.health = try {
        this.getAttribute(Attribute.GENERIC_MAX_HEALTH).value
    } catch (_: Exception) {
        this.maxHealth
    }

    if (foodLevel)
        this.foodLevel = 20
}

// ------------------------------------ //
//                Server                //
// ------------------------------------ //

/** Gets the complete raw TPS */
val Server.rawTps: DoubleArray
    get() {
        var tps: DoubleArray?

        SoundCategory.PLAYERS
        with(Reflections) {
            val serverClass = Reflections.SPIGOT.getNMSClass("MinecraftServer")
            val instance = this.findMethod(serverClass, "getServer")!!.invoke(null)

            tps = this.getField(serverClass.superclass, instance, "recentTps") as DoubleArray
        }

        if (tps == null)
            return doubleArrayOf(20.0, 20.0, 20.0)

        return tps!!
    }

/** Gets the server TPS with correct format or at least more readable */
val Server.tps: DoubleArray
    get() {
        // original: Math.min(Math.round(tps * 100.0D) / 100.0D, 20.0D)
        fun round(double: Double) = ((double * 100.0).roundToInt() / 100.0).coerceAtMost(20.0)

        return this.rawTps.map { round(it) }.toDoubleArray()
    }

/** Gets the first index of the [Server.rawTps] and is formatted correctly */
val Server.firstTps get() = this.tps[0]

/**
 * Restarts the server
 *
 * The way it restarts the server is by using the RestartCommand class from and by spigot
 * since the class can't be fetched when using the spigot-api dependency it needs to use a reflection
 * the actual way of invoking or executing the class is by just doing `RestartCommand.restart();`
 *
 * Well, since the method is public and static we don't need to set the method accessible
 * and to invoke it we also don't need the class instance, just use a 'null' as an instance will do it perfectly
 */
fun Server.restartServer() {
    val restartCmdClass = Class.forName("org.spigotmc.RestartCommand")
    Reflections.findMethod(restartCmdClass, "restart")!!.invoke(null)
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
 * Registers a command and allow to chain the usage (for kotlin only)
 */
@JvmSynthetic
fun JavaPlugin.registerCommand(command: String, executor: CommandExecutor): JavaPlugin {
    this.getCommand(command).executor = executor
    return this
}

/**
 * Registers multiple commands (pairs of main command name and command object)
 */
@JvmSynthetic
fun JavaPlugin.registerCommand(vararg commandPair: Pair<String, CommandExecutor>): JavaPlugin {
    commandPair.forEach { this.registerCommand(it.first, it.second) }
    return this
}

/**
 * Registers listener(s) and allow to chain the usage (for kotlin only)
 */
@JvmSynthetic
fun JavaPlugin.registerListeners(vararg listener: Listener): JavaPlugin {
    listener.forEach { this.server.pluginManager.registerEvents(it, this) }
    return this
}

// ------------------------------------ //
//          InventoryClickEvent         //
// ------------------------------------ //

/** Determines if the item clicked or clicked inventory is `null` */
val InventoryClickEvent.isItemOrInventoryNull
    get() = this.currentItem == null || this.clickedInventory == null

/** Determines if the clicked item is [Material.AIR] */
val InventoryClickEvent.isItemAir
    get() = this.currentItem.type == Material.AIR

/** Determines if the player clicks is outsite of the supposed inventory event */
val InventoryClickEvent.isClickedOutside
    get() = this.slotType == InventoryType.SlotType.OUTSIDE

// ------------------------------------ //
//             Configuration            //
// ------------------------------------ //

/** Shortened version of [ConfigurationSection.getConfigurationSection] */
fun ConfigurationSection.getSection(path: String): ConfigurationSection? =
    this.getConfigurationSection(path)

// ------------------------------------ //
//                 World                //
// ------------------------------------ //

/**
 * Deletes the world completely without leaving a single trace
 *
 * @param saveChunks do you want to save the world chunks?
 */
@JvmOverloads
fun World.delete(saveChunks: Boolean = false) {
    val worldFolder = this.worldFolder
    safeRun { Bukkit.unloadWorld(this, saveChunks) }
    worldFolder.deleteRecursively()
}

// ------------------------------------ //
//               ItemStack              //
// ------------------------------------ //

/** Transforms the [ItemStack] into an [ItemBuilder] */
fun ItemStack.builder() = ItemBuilder(this)

// ------------------------------------ //
//                String                //
// ------------------------------------ //

/** Translates the string to a minecraft chat color*/
fun String.colorize(): String =
    ChatColor.translateAlternateColorCodes('&', this)

/** Translates all string to minecraft chat color*/
fun List<String>.colorize() = this.map { it.colorize() }