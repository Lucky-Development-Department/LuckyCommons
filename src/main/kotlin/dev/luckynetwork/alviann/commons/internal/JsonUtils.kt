@file:JvmName("JsonUtils")
@file:Suppress("unused")

package dev.luckynetwork.alviann.commons.internal

import com.google.gson.*
import com.google.gson.stream.JsonReader
import java.io.Reader

val defaultGson = GsonBuilder().create()!!
val prettyGson = GsonBuilder().setPrettyPrinting().create()!!

/** The default json parser (supports all versions) */
@Suppress("DEPRECATION")
val defaultParser = JsonParser()

/** Prettifies the json string */
fun JsonElement.toPrettyString() = prettyGson.toJson(this)!!

/**
 * Transforms the array to an array of string
 */
fun JsonArray.asStringArray(): Array<String> {
    val list = ArrayList<String>()

    for (element in this)
        list.add(element.asString)

    return list.toTypedArray()
}

/**
 * Transforms the array to an array of [Number]
 */
fun JsonArray.asNumberArray(): Array<Number> {
    val list = ArrayList<Number>()

    for (element in this)
        list.add(element.asNumber)

    return list.toTypedArray()
}

/**
 * Gets a [JsonElement] from nodes
 *
 * For example you have a json file like this
 * ```json
 * {
 *    "options": {
 *      "use-sql": true
 *    }
 * }
 * ```
 *
 * Instead of getting it one by one like this
 * ```kotlin
 * val json: JsonObject = ...
 * val useSql = json.get("options").asJsonObject.get("use-sql").asBoolean
 * ```
 *
 * You can just simplify it by using
 * ```kotlin
 * val json: JsonObject = ...
 * val useSql = json.fromNode("options.use-sql").asBoolean
 * ```
 */
fun JsonObject.fromNode(nodes: String): JsonElement? {
    if (!nodes.contains("."))
        return this.get(nodes)

    val members = nodes.split(".")
    var element: JsonElement? = null

    // need to know whether it's the first time the member is being read
    var firstRun = false

    for (member in members) {
        if (!firstRun) {
            element = this.get(member)
            firstRun = true
        } else {
            if (element == null)
                return null

            element = element.asJsonObject.get(member)
        }
    }

    return element
}


/**
 * Gets a string value from json
 */
fun JsonObject.getString(member: String): String? {
    val element = this.fromNode(member) ?: return null
    return element.asString
}

/** Creates adds a property to the [JsonObject] and make it chain-able */
@JvmSynthetic
fun JsonObject.chain(property: String, value: String) =
    createChain(this, property, value)

/** Creates adds a property to the [JsonObject] and make it chain-able */
@JvmSynthetic
fun JsonObject.chain(property: String, value: Number) =
    createChain(this, property, value)

/** Creates adds a property to the [JsonObject] and make it chain-able */
@JvmSynthetic
fun JsonObject.chain(property: String, value: Boolean) =
    createChain(this, property, value)

/** Creates adds a property to the [JsonObject] and make it chain-able */
@JvmSynthetic
fun JsonObject.chain(property: String, value: Char) =
    createChain(this, property, value)

/** Creates adds a property to the [JsonObject] and make it chain-able */
@JvmSynthetic
fun JsonObject.chain(property: String, value: JsonElement) =
    createChain(this, property, value)

@JvmSynthetic
private fun createChain(json: JsonObject, property: String, value: Any): JsonObject {
    when (value) {
        is String -> json.addProperty(property, value)
        is Number -> json.addProperty(property, value)
        is Char -> json.addProperty(property, value)
        is Boolean -> json.addProperty(property, value)
        is JsonElement -> json.add(property, value)
        else -> throw IllegalArgumentException("Invalid argument!")
    }
    return json
}

/** Parses a string into a [JsonElement] */
@Suppress("DEPRECATION")
fun parseToJson(string: String) = defaultParser.parse(string)!!

/** Parses a reader into a [JsonElement] */
@Suppress("DEPRECATION")
fun parseToJson(reader: Reader) = defaultParser.parse(reader)!!

/** Parses a json reader into a [JsonElement] */
@Suppress("DEPRECATION")
fun parseToJson(reader: JsonReader) = defaultParser.parse(reader)!!