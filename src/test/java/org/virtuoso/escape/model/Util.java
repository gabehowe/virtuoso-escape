package org.virtuoso.escape.model;

public class Util {
    public static void rebuildSingleton(Class<?> clazz) throws Exception {
        var privateConstructor = clazz.getDeclaredConstructor();
        privateConstructor.setAccessible(true);
        var instance = clazz.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, privateConstructor.newInstance());
    }
}
