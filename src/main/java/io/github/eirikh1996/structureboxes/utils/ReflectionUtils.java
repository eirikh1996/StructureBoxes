package io.github.eirikh1996.structureboxes.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {
    public static Method getMethod(@NotNull Class clazz, @NotNull String methodName, Class... params){
        try {
            return clazz.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
