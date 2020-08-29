package dev.luckynetwork.alviann.commons.config.impl

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import dev.luckynetwork.alviann.commons.config.Config
import dev.luckynetwork.alviann.commons.internal.*
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.StandardCopyOption

class JsonConfig(
    override val file: File,
    /** pair of a [Class] where the project has the specified file name
     * and a [String] that is the path and/or file name */
    private val pair: Pair<Class<*>, String>? = null
) : Config {

    private lateinit var json: JsonObject

    override fun reloadConfig() {
        if (!file.exists()) {
            if (pair != null) {
                val clazz = pair.first
                val source = pair.second

                clazz.loadFile(source, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } else {
                throw FileNotFoundException("Cannot find file named ${file.name}!")
            }
        }

        closer {
            val reader = it.add(file.reader().buffered())!!
            val content = reader.readLines().joinToString("\n")
            json = parseToJson(content).asJsonObject
        }
    }

    override fun saveConfig() {
        safeRun {
            closer {
                val writer = it.add(file.printWriter())!!
                writer.println(json.toPrettyString())
            }
        }
    }

    override fun get(path: String): Any? {
        val node = json.fromNode(path) ?: return null

        return when (node) {
            is JsonArray -> this.getList(path)
            is JsonNull -> null
            is JsonObject -> node.asJsonObject
            else -> node.asJsonPrimitive.objectValue
        }
    }

    /**
     * gets the `value` field from [JsonPrimitive]
     */
    private val JsonPrimitive.objectValue
        get() = safeRun {
            val field = this::class.java.getDeclaredField("value")
            field.isAccessible = true
            return@safeRun field.get(this)
        }

    override fun getList(path: String): List<Any?>? {
        val element = json.fromNode(path) ?: return null
        val array = element.asJsonArray

        return array.map { it.asJsonPrimitive.objectValue }
    }

}