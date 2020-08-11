package json

import dev.luckynetwork.alviann.commons.config.impl.JsonConfig
import java.io.File

fun main() {
    val config = JsonConfig(File("src/test/resources/config.json"))
    println("Object: $config")

    config.reloadConfig()
    val value = config.getNumberList("numbers")!!

    println("Value: $value")
    println("Class: ${value::class.java}")

    if (value::class.java.isAssignableFrom(Collection::class.java))
        println(value[0]::class.java)
}