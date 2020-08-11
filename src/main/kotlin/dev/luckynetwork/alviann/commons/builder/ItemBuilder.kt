package dev.luckynetwork.alviann.commons.builder

import dev.luckynetwork.alviann.commons.internal.colorize
import dev.luckynetwork.alviann.commons.internal.safeRun
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta

/**
 * Easily create item stacks, without messing your hands.
 *
 * _Note that if you do use this in one of your projects, leave this notice._
 * _Please do credit me if you do use this in one of your projects._
 *
 * @author NonameSL (original)
 * @author Alviann
 */
@Suppress("unused")
class ItemBuilder : Cloneable {

    constructor(material: Material, amount: Int = 1, durability: Short = 0) {
        item = ItemStack(material, amount, durability)
    }

    constructor(rawItem: ItemStack) {
        item = ItemStack(rawItem.clone())
    }

    constructor(builder: ItemBuilder) {
        item = builder.build()
    }

    companion object {

        /** create an itemstack the shortest way */
        @JvmStatic
        fun toItem(material: Material, amount: Int = 1, durability: Short = 0) =
            ItemBuilder(material, amount, durability)

    }

    private val item: ItemStack

    /**
     * clones the item builder
     */
    override fun clone(): ItemBuilder {
        return ItemBuilder(item)
    }

    /**
     * sets the item display name
     */
    fun name(name: String): ItemBuilder {
        val meta = item.itemMeta

        meta.displayName = name.colorize()
        item.itemMeta = meta

        return this
    }

    /**
     * sets the item amount
     */
    fun amount(amount: Int): ItemBuilder {
        item.amount = amount
        return this
    }

    /**
     * sets the item lores
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun lore(vararg lores: String): ItemBuilder {
        val meta = item.itemMeta

        val newLores = lores.map { it.colorize() }
        meta.lore = newLores
        item.itemMeta = meta

        return this
    }

    /** sets the item lores */
    fun lore(loreList: Collection<String>) = this.lore(*loreList.toTypedArray())

    /**
     * removes a lore line based on the index
     */
    fun removeLoreAt(index: Int): ItemBuilder {
        val meta = item.itemMeta

        val loreList = ArrayList(meta.lore)
        loreList.removeAt(index)

        meta.lore = loreList
        item.itemMeta = meta

        return this
    }

    /**
     * adds a lore line to a specific index if specified
     */
    fun addLore(line: String, index: Int): ItemBuilder {
        val meta = item.itemMeta

        val loreList = ArrayList(meta.lore)
        loreList.add(index, line.colorize())

        meta.lore = loreList
        item.itemMeta = meta

        return this
    }

    /**
     * adds an unbreakable state to the item
     */
    @Suppress("DEPRECATION")
    fun unbreakable(unbreakable: Boolean): ItemBuilder {
        try {
            item.itemMeta.isUnbreakable = unbreakable
        } catch (e: Exception) {
            item.itemMeta.spigot().isUnbreakable = unbreakable
        }
        return this
    }

    /**
     * adds item flags to the item
     */
    fun addItemFlags(vararg flags: ItemFlag): ItemBuilder {
        item.itemMeta.addItemFlags(*flags)
        return this
    }

    /**
     * removes item flags from the item
     */
    fun removeItemFlags(vararg flags: ItemFlag): ItemBuilder {
        item.itemMeta.removeItemFlags(*flags)
        return this
    }

    /**
     * sets the skull owner of the item (only works for skull item)
     */
    @Suppress("DEPRECATION")
    fun skullOwner(owner: String): ItemBuilder {
        val offline = Bukkit.getOfflinePlayer(owner) ?: return this

        safeRun(false) {
            val meta = item.itemMeta as SkullMeta

            try {
                meta.owningPlayer = offline
            } catch (e: Exception) {
                meta.owner = offline.name
            }

            item.itemMeta = meta
        }

        return this
    }

    /**
     * adds an enchantment to the item (unsafe enchantment is allowed)
     */
    fun addEnchantment(enchantment: Enchantment, level: Int = 1): ItemBuilder {
        item.addUnsafeEnchantment(enchantment, level)
        return this
    }

    /**
     * removes an enchantment from the item
     */
    fun removeEnchantment(enchantment: Enchantment): ItemBuilder {
        item.removeEnchantment(enchantment)
        return this
    }

    /**
     * sets the leather armor color
     */
    fun leatherColor(color: Color): ItemBuilder {
        safeRun(false) {
            val meta = item.itemMeta as LeatherArmorMeta
            meta.color = color
            item.itemMeta = meta
        }

        return this
    }

    /** creates the itemstack */
    fun toItemStack() = item.clone()

    /** creates the itemstack */
    fun build() = item.clone()

}

/** builds an item
 *
 * this is only available for kotlin */
@JvmSynthetic
fun buildItem(
    item: ItemStack,
    transformer: (ItemBuilder) -> Unit
): ItemStack {
    val builder = ItemBuilder(item).apply(transformer)
    return builder.build()
}

/** builds an item
 *
 * this is only available for kotlin */
@JvmSynthetic
fun buildItem(
    builder: ItemBuilder,
    transformer: (ItemBuilder) -> Unit
): ItemStack {
    val newBuilder = ItemBuilder(builder).apply(transformer)
    return newBuilder.build()
}

/** builds an item
 *
 * this is only available for kotlin */
@JvmSynthetic
fun buildItem(
    material: Material,
    amount: Int = 1,
    durability: Short = 0,
    transformer: (ItemBuilder) -> Unit
): ItemStack {
    val builder = ItemBuilder(material, amount, durability).apply(transformer)
    return builder.build()
}