package dev.luckynetwork.alviann.commons.config

import java.io.File

interface Config {

    /** the config file */
    val file: File

    /** reloads the config, will also load it if it wasn't loaded */
    fun reloadConfig()

    /** saves the config */
    fun saveConfig()

    /** gets a value from the config */
    fun get(path: String): Any?

    /** gets a string value from the config */
    fun getString(path: String): String? {
        val value = get(path) ?: return null
        return value as String
    }

    /** gets a boolean value from the config */
    fun getBoolean(path: String): Boolean? {
        val value = get(path) ?: return null
        return value as Boolean
    }

    /** gets a number value from the config */
    fun getNumber(path: String): Number? {
        val value = get(path) ?: return null
        return value as Number
    }

    /** gets a list value from the config */
    @Suppress("UNCHECKED_CAST")
    fun getList(path: String): List<Any?>? {
        val value = get(path) ?: return null
        return value as List<Any>
    }

    /** gets a string list value from the config */
    fun getStringList(path: String): List<String>? {
        val list = this.getList(path) ?: return null
        return list.map { it as String }
    }

    /** gets a string list value from the config */
    fun getNumberList(path: String): List<Number>? {
        val list = this.getList(path) ?: return null
        return list.map { it as Number }
    }

}