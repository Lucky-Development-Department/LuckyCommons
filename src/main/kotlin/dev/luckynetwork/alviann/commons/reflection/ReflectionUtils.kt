package dev.luckynetwork.alviann.commons.reflection

import dev.luckynetwork.alviann.commons.internal.toImmutable
import org.bukkit.Bukkit
import java.lang.reflect.Field

@Suppress("UNCHECKED_CAST")
object ReflectionUtils {

    /**
     * sets a field value for an instance
     */
    @JvmStatic
    fun setField(clazz: Class<*>, instance: Any?, fieldName: String, value: Any?) {
        val field = clazz.getField(fieldName)
        field.isAccessible = true
        field.set(instance, value)
    }

    /**
     * gets a field value from an instance
     */
    @JvmStatic
    fun getField(clazz: Class<*>, instance: Any?, fieldName: String): Any? {
        val field = clazz.getField(fieldName)
        field.isAccessible = true
        return field.get(instance)
    }

    /**
     * invokes a method from an instance
     */
    @JvmStatic
    fun invokeMethod(clazz: Class<*>, instance: Any?, methodName: String, vararg params: Any?): Any? {
        val method = clazz.getMethod(methodName)
        method.isAccessible = true
        return method.invoke(instance, params)
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
     * gets all fields from a class
     */
    @JvmStatic
    fun getFields(clazz: Class<*>, declared: Boolean = true): List<Field> {
        val fieldList = ArrayList<Field>()

        val rawFields = if (declared) clazz.declaredFields else clazz.fields
        for (rawField in rawFields) {
            rawField.isAccessible = true
            fieldList.add(rawField)
        }

        return fieldList.toImmutable()
    }

    object SPIGOT {

        /** gets the current bukkit/spigot NMS version */
        @JvmStatic
        val version by lazy {
            Bukkit.getServer().javaClass.getPackage().name.split(Regex("\\.")).toTypedArray()[3]
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