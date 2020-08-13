package dev.luckynetwork.alviann.commons.reflection

import dev.luckynetwork.alviann.commons.internal.safeRun
import org.bukkit.Bukkit
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
object Reflections {

    /**
     * sets a field value for an instance
     */
    @JvmStatic
    fun setField(clazz: Class<*>, instance: Any?, fieldName: String, value: Any?) {
        val field = try {
            clazz.getDeclaredField(fieldName)
        } catch (_: Exception) {
            clazz.getField(fieldName)
        }

        field.isAccessible = true
        field.set(instance, value)
    }

    /**
     * gets a field value from an instance
     */
    @JvmStatic
    fun getField(clazz: Class<*>, instance: Any?, fieldName: String): Any? {
        val field = try {
            clazz.getDeclaredField(fieldName)
        } catch (_: Exception) {
            clazz.getField(fieldName)
        }

        field.isAccessible = true
        return field.get(instance)
    }

    /** gets all fields from a class */
    @JvmStatic
    fun getFields(clazz: Class<*>, declared: Boolean = true) =
        (if (declared) clazz.declaredFields else clazz.fields)
            .toMutableList()
            .onEach { it.isAccessible = true }

    /**
     * finds a field from a class
     */
    @JvmStatic
    fun findField(clazz: Class<*>, fieldName: String): Field? {
        val field = safeRun(false) {
            try {
                clazz.getDeclaredField(fieldName)
            } catch (_: Exception) {
                clazz.getField(fieldName)
            }
        } ?: return null

        field.isAccessible = true
        return field
    }

    /** gets all methods from a class */
    @JvmStatic
    fun getMethods(clazz: Class<*>, declared: Boolean = true) =
        (if (declared) clazz.declaredMethods else clazz.methods)
            .toMutableList()
            .onEach { it.isAccessible = true }

    /**
     * finds a method from a class
     */
    @JvmStatic
    fun findMethod(clazz: Class<*>, methodName: String, vararg paramsClasses: Class<*>): Method? {
        val method = safeRun(false) {
            try {
                clazz.getDeclaredMethod(methodName, *paramsClasses)
            } catch (_: Exception) {
                clazz.getMethod(methodName, *paramsClasses)
            }
        } ?: return null

        method.isAccessible = true
        return method
    }

    /**
     * creates a new instance based on the first constructor inside the class
     */
    @JvmStatic
    fun newFirstInstance(clazz: Class<*>, vararg params: Any?): Any? {
        val constructorList = with(clazz) {
            if (declaredConstructors.isNotEmpty())
                declaredConstructors
            else
                constructors
        }

        val constructor = constructorList.firstOrNull() ?: return null
        constructor.isAccessible = true

        return constructor.newInstance(params)
    }

    /**
     * finds a constructor from a class
     */
    @JvmStatic
    fun findConstructor(clazz: Class<*>, vararg paramsClasses: Class<*>): Constructor<*>? {
        val constructor = try {
            clazz.getDeclaredConstructor(*paramsClasses)
        } catch (_: Exception) {
            clazz.getConstructor(*paramsClasses)
        } ?: return null

        constructor.isAccessible = true
        return constructor
    }

    /** gets all constructors from a class */
    @JvmStatic
    fun getConstructors(clazz: Class<*>, declared: Boolean = true) =
        (if (declared) clazz.declaredConstructors else clazz.constructors)
            .toMutableList()
            .onEach { it.isAccessible = true }

    object SPIGOT {

        /** gets the current bukkit/spigot NMS version */
        @JvmStatic
        val version by lazy {
            Bukkit.getServer().javaClass.`package`.name.split("\\.".toRegex())[3]
        }

        /** gets a class from NMS (net.minecraft.server) */
        @JvmStatic
        fun getNMSClass(classPath: String): Class<*> =
            Class.forName("net.minecraft.server.$version.$classPath")

        /** gets a class from OBC (org.bukkit.craftbukkit) */
        @JvmStatic
        fun getOBCClass(classPath: String): Class<*> =
            Class.forName("org.bukkit.craftbukkit.$version.$classPath")

    }

}