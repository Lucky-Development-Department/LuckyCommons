@file:JvmName("Utils")

package dev.luckynetwork.alviann.commons.internal

import dev.luckynetwork.alviann.commons.closer.Closer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import net.md_5.bungee.api.ChatColor
import java.io.BufferedInputStream
import java.io.File
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

/**
 * this works basically the same as the classical [Any.toString] method
 * but this one is generated based on the class variables and the result can be seen during runtime
 *
 * unlike the normal way, you would need to do everything yourself,
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

// ------------------------------------ //
//                String                //
// ------------------------------------ //

/** parses the string to UUID */
fun String.toUUID(): UUID = UUID.fromString(this)

/** translates the string to a minecraft chat color*/
fun String.colorize(): String =
    ChatColor.translateAlternateColorCodes('&', this)

/** translates all string to minecraft chat color*/
fun List<String>.colorize() = this.map { it.colorize() }

/** parses a string to a json */
fun String.toJson() = parseToJson(this)

/** creates a censored string */
fun String.censor(): String = "*".repeat(this.length)

/** determines if the string is an integer */
val String.isInt
    get() = this.toIntOrNull() != null

/** determines if a string is a valid minecraft username */
val String.isMinecraftUsername
    get() = this.matches(Regex("^[a-zA-Z_0-9]{3,16}\$"))

// ------------------------------------ //
//           Global Collections         //
// ------------------------------------ //

/** gets the immutable object of the [Collection] */
fun <T> Collection<T>.toImmutable(): Collection<T> =
    Collections.unmodifiableCollection(this)

/** gets the immutable object of the [List] */
fun <T> List<T>.toImmutable(): List<T> =
    Collections.unmodifiableList(this)

/** gets the immutable object of the [Map] */
fun <K, V> Map<K, V>.toImmutable(): Map<K, V> =
    Collections.unmodifiableMap(this)

/** gets the immutable object of the [Set] */
fun <T> Set<T>.toImmutable(): Set<T> =
    Collections.unmodifiableSet(this)

// ------------------------------------ //
//               Coroutines             //
// ------------------------------------ //

/**
 * submits a [block] to run asynchronously and can get the result
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
 * runs a function inside the [block] safely
 * if any error is thrown, depending on the [printError] it may print it to the console
 */
fun <T> safeRun(printError: Boolean = true, block: () -> T): T? {
    try {
        return block()
    } catch (e: Throwable) {
        if (printError) e.printStackTrace()
    }
    return null
}

/** creates a [Closer] block */
fun closer(block: (Closer) -> Unit) = Closer().use(block)

// ------------------------------------ //
//                 File                 //
// ------------------------------------ //

/**
 * gets a stream of a file from the jar
 *
 * @param clazz the class which will be transformed to a [ClassLoader] to get the stream
 * @param name the file inside the .jar
 */
fun getResourceStream(clazz: Class<*>, name: String): BufferedInputStream? =
    clazz.classLoader.getResourceStream(name)?.buffered()

/**
 * gets a stream of a file from the jar
 *
 * @param name the file inside the .jar
 */
fun Any.getResourceStream(name: String) = getResourceStream(this::class.java, name)

/** gets the plugin jar file */
val Class<*>.currentJarFile
    get() = safeRun(false) { File(this.protectionDomain.codeSource.location.toURI()) }

/**
 * loads a file from the jar to a certain location
 *
 * @param clazz the class which has the correct [ClassLoader]
 * @param source the file inside the .jar
 * @param path the file destination
 * @param options the options to load the file
 *
 * @throws NullPointerException if the file inside the jar cannot be found
 * @see Files.copy
 */
fun loadFile(clazz: Class<*>, source: String, destination: Path, vararg options: CopyOption) {
    closer {
        val stream = it.add(getResourceStream(clazz, source))
            ?: throw NullPointerException("Cannot find the file from the jar!")

        Files.copy(stream, destination, *options)
    }
}

/**
 * loads a file from the jar to a certain location
 *
 * @param source the file inside the .jar
 * @param path the file destination
 * @param options the options to load the file
 *
 * @throws NullPointerException if the file inside the jar cannot be found
 * @see Files.copy
 */
fun Any.loadFile(source: String, destination: Path, vararg options: CopyOption) =
    loadFile(this::class.java, source, destination, *options)

// ------------------------------------ //
//                 Thread               //
// ------------------------------------ //

/** sleeps a thread */
fun sleep(millis: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) =
    Thread.sleep(timeUnit.toMillis(millis))

/** safely sleeps a thread */
fun safeSleep(millis: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) =
    sleep(millis, timeUnit)

// ------------------------------------ //
//               Date Format            //
// ------------------------------------ //

/**
 * builds a date from pattern format (UTC+7)
 *
 * @param millis  the millis
 * @param pattern the pattern
 *
 * @return the formatted date
 */
fun dateFormat(pattern: String, millis: Long = System.currentTimeMillis()): String {
    val dateFormat = SimpleDateFormat(pattern)
    dateFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
    return dateFormat.format(millis)
}