@file:JvmName("Utils")
@file:Suppress("unused")

package dev.luckynetwork.alviann.commons.internal

import com.google.gson.JsonElement
import dev.luckynetwork.alviann.commons.closer.Closer
import dev.luckynetwork.alviann.commons.objects.JavaValue
import dev.luckynetwork.alviann.commons.objects.SafeFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.io.BufferedInputStream
import java.io.File
import java.math.RoundingMode
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

// ------------------------------------ //
//                Global                //
// ------------------------------------ //

/**
 * This works basically the same as the classical [Any.toString] method
 * but this one is generated based on the class variables and the result can be seen during runtime.
 *
 * Unlike the normal way, you would need to do everything yourself,
 * automatically generate it by using the IDE, etc.
 */
@Suppress("unused")
fun Any.stringify(): String {
    val clazz = this::class.java

    val builder = ArrayList<String>()
    for (field in clazz.declaredFields) {
        field.isAccessible = true
        builder.add("${field.name}=${field.get(this)}")
    }

    return "${clazz.simpleName}(${builder.joinToString()})"
}

/** Transforms an object into a [JsonElement] */
fun Any.toJson() = parseToJson(defaultGson.toJson(this))

// ------------------------------------ //
//                String                //
// ------------------------------------ //

/** Parses the string to UUID */
fun String.toUUID(): UUID = UUID.fromString(this)

/** Parses a string to a json */
fun String.toJson() = parseToJson(this)

/** Creates a censored string */
fun String.censor(): String = "*".repeat(this.length)

/** Determines if the string is an integer */
val String.isInt
    get() = this.toIntOrNull() != null

/** Determines if a string is a valid minecraft username */
val String.isMinecraftUsername
    get() = this.matches(Regex("^[a-zA-Z_0-9]{3,16}\$"))

// ------------------------------------ //
//                Double                //
// ------------------------------------ //

/** rounds up the decimal places in [Double] */
fun Double.round(roundingMode: RoundingMode, decimal: Int) =
    this.toBigDecimal().setScale(decimal, roundingMode).toDouble()

/** rounds up the decimal places in [Double] to 1 decimal places */
val Double.roundUp
    get() = this.round(RoundingMode.UP, 1)

/** rounds up the decimal places in [Double] to 1 decimal places */
val Double.roundDown
    get() = this.round(RoundingMode.DOWN, 1)

// ------------------------------------ //
//                 Long                 //
// ------------------------------------ //

/** formats [Long] to the days:hours:minutes:seconds format */
fun Long.formatTime(): String {
    val seconds = this % 60
    val minutes = this % 3600 / 60
    val hours = this % 86400 / 3600
    val days = this / 86400

    return when {
        days > 0 -> "$days:$hours:$minutes:$seconds"
        hours > 0 -> "$hours:$minutes:$seconds"
        seconds > 0 -> "$minutes:$seconds"
        else -> "$seconds"
    }
}

// ------------------------------------ //
//           Global Collections         //
// ------------------------------------ //

/** Gets the immutable object of the [Collection] */
fun <T> Collection<T>.toImmutable(): Collection<T> =
    Collections.unmodifiableCollection(this)

/** Gets the immutable object of the [List] */
fun <T> List<T>.toImmutable(): List<T> =
    Collections.unmodifiableList(this)

/** Gets the immutable object of the [Map] */
fun <K, V> Map<K, V>.toImmutable(): Map<K, V> =
    Collections.unmodifiableMap(this)

/** Gets the immutable object of the [Set] */
fun <T> Set<T>.toImmutable(): Set<T> =
    Collections.unmodifiableSet(this)

// ------------------------------------ //
//               Coroutines             //
// ------------------------------------ //

/**
 * Submits a [block] to run asynchronously and can get the result
 */
@Suppress("DeferredResultUnused")
fun <T> runAsync(context: CoroutineContext, block: () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()

    CoroutineScope(context).async {
        try {
            future.complete(block())
        } catch (e: Exception) {
            future.completeExceptionally(e)
            future.complete(null)
        }
    }

    return future
}

// ------------------------------------ //
//             Miscellaneous            //
// ------------------------------------ //

/**
 * Runs a function inside the [block] safely
 *
 * If any error is thrown, depending on the [printError] it may print it to the console
 * just like any other thrown exceptions
 */
@JvmSynthetic
fun <T> safeRun(printError: Boolean = true, block: () -> T): T? {
    try {
        return block()
    } catch (e: Throwable) {
        if (printError) e.printStackTrace()
    }
    return null
}

/**
 * Runs an instructions under the [block] safely
 *
 * If any error is thrown, depending on the [printError] it may print it to the console
 * just like any other thrown exceptions
 */
@JvmOverloads
fun <T> safeRun(printError: Boolean = true, block: SafeFunction<T?>): T? {
    try {
        return block.invoke()
    } catch (e: Throwable) {
        if (printError) e.printStackTrace()
    }
    return null
}

/** creates a [Closer] block */
@JvmSynthetic
fun closer(block: (Closer) -> Unit) = Closer().use(block)

/** @see JavaValue */
@Suppress("HasPlatformType")
fun <T> jvmValue(value: T) = JavaValue.valueOf(value)

/** @see JavaValue */
@Suppress("HasPlatformType")
fun <T> jvmValue(block: () -> T) = JavaValue.valueFromBlock(block)

/** @see JavaValue */
@Suppress("HasPlatformType")
fun <T> javaValue(value: T) = JavaValue.valueOf(value)

/** @see JavaValue */
@Suppress("HasPlatformType")
fun <T> javaValue(block: () -> T) = JavaValue.valueFromBlock(block)

// ------------------------------------ //
//                 File                 //
// ------------------------------------ //

/**
 * Gets a stream of a file from the jar
 *
 * @param name the file inside the .jar
 */
fun Class<*>.getResourceStream(name: String): BufferedInputStream? =
    this.classLoader.getResourceAsStream(name)?.buffered()

/**
 * Gets a stream of a file from the jar
 *
 * @param name the file inside the .jar
 */
fun Any.getResourceStream(name: String): BufferedInputStream? =
    this.javaClass.getResourceStream(name)

/** gets the plugin jar file */
val Class<*>.currentJarFile
    get() = safeRun(false) { File(this.protectionDomain.codeSource.location.toURI()) }

/**
 * Loads a file from the jar to a certain location
 *
 * @param source the file inside the .jar
 * @param destination the file destination
 * @param options the options to load the file
 *
 * @throws NullPointerException if the file inside the jar cannot be found
 * @see Files.copy
 */
fun Class<*>.loadFile(source: String, destination: Path, vararg options: CopyOption) {
    closer {
        val stream = it.add(this.getResourceStream(source))
            ?: throw NullPointerException("Cannot find the file from the jar!")

        Files.copy(stream, destination, *options)
    }
}

/**
 * Loads a file from the jar to a certain location
 *
 * @param source the file inside the .jar
 * @param destination the file destination
 * @param options the options to load the file
 *
 * @throws NullPointerException if the file inside the jar cannot be found
 * @see Files.copy
 */
fun Any.loadFile(source: String, destination: Path, vararg options: CopyOption) =
    this.javaClass.loadFile(source, destination, *options)

// ------------------------------------ //
//                 Thread               //
// ------------------------------------ //

/** Sleeps a thread */
@JvmOverloads
fun sleep(millis: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) =
    Thread.sleep(timeUnit.toMillis(millis))

/** Safely sleeps a thread */
@JvmOverloads
fun safeSleep(millis: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) =
    sleep(millis, timeUnit)

// ------------------------------------ //
//               Date Format            //
// ------------------------------------ //

/**
 * Builds a date from pattern format
 *
 * @param millis  the millis
 * @param pattern the pattern
 *
 * @return the formatted date
 */
@JvmOverloads
fun dateFormat(pattern: String, timeZone: String? = "Asia/Bangkok", millis: Long = System.currentTimeMillis()): String {
    val dateFormat = SimpleDateFormat(pattern)

    if (timeZone != null)
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)

    return dateFormat.format(millis)
}