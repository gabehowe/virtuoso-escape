package org.virtuoso.escape.model

object Util {
    @Throws(Exception::class)
    fun rebuildSingleton(clazz: Class<*>) {
        val privateConstructor: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = clazz.getDeclaredConstructor()
        privateConstructor.setAccessible(true)
        val instance: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? = clazz.getDeclaredField("instance")
        instance.setAccessible(true)
        instance.set(null, privateConstructor.newInstance())
    }
}
