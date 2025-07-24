package io.github.eirikh1996.structureboxes.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
@Deprecated(
        forRemoval = true
)
public class ReflectionUtils {
    public static Class getClass(String classPath) {
        Class clazz;
        try {
            clazz = Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            clazz = null;
        }
        return clazz;
    }

    public static Method getMethod(@NotNull Class clazz, @NotNull String methodName, Class... params){
        try {
            return clazz.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
            return null;
        }
    }

    public static Field getField(@NotNull Class clazz, @NotNull String fieldName){
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
