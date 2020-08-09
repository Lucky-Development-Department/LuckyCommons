@file:JvmName("JsonUtils")

package dev.luckynetwork.alviann.commons.internal

import com.google.gson.*

val defaultGson: Gson = GsonBuilder().create()
val prettyGson: Gson = GsonBuilder().setPrettyPrinting().create()

/** @return a prettified JSON string */
fun JsonElement.toPrettyString(): String = prettyGson.toJson(this)

/**
 * transforms the array to an array of string
 */
fun JsonArray.asStringArray(): Array<String> {
    val list = ArrayList<String>()

    for (element in this)
        list.add(element.asString)

    return list.toTypedArray()
}

/**
 * gets a [JsonElement] from nodes
 *
 * for example you have a json file like this
 * ```json
 *  {
 *    "options": {
 *      "use-sql": true
 *    }
 * }
 * ```
 *
 * instead of getting it one by one like this
 * ```kotlin
 * val json: JsonObject = ...
 * val useSql = json.get("options").asJsonObject.get("use-sql").asBoolean
 * ```
 *
 * you can just simplify it by using
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
 * gets a string value from json
 */
fun JsonObject.getString(member: String): String? {
    val element = this.fromNode(member) ?: return null
    return element.asString
}

/** creates adds a property to the [JsonObject] and make it chain-able */
fun JsonObject.addChain(property: String, value: String) =
    chainJson(this, property, value)

/** creates adds a property to the [JsonObject] and make it chain-able */
fun JsonObject.addChain(property: String, value: Number) =
    chainJson(this, property, value)

/** creates adds a property to the [JsonObject] and make it chain-able */
fun JsonObject.addChain(property: String, value: Boolean) =
    chainJson(this, property, value)

/** creates adds a property to the [JsonObject] and make it chain-able */
fun JsonObject.addChain(property: String, value: Char) =
    chainJson(this, property, value)

/** creates adds a property to the [JsonObject] and make it chain-able */
fun JsonObject.addChain(property: String, value: JsonElement) =
    chainJson(this, property, value)

private fun chainJson(json: JsonObject, property: String, value: Any): JsonObject {
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