package string

import dev.luckynetwork.alviann.commons.internal.censor

fun main() {
    val name = "Alviann"

    println("Original: $name, Length: ${name.length}")
    println("Censored: ${name.censor()}, Length: ${name.censor().length}")
}