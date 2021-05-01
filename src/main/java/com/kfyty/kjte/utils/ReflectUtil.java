package com.kfyty.kjte.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 描述: ReflectUtil
 *
 * @author kfyty
 * @date 2021/1/23 14:51
 * @email kfyty725@hotmail.com
 */
public abstract class ReflectUtil {

    public static Field findField(Class<?> clazz, String filedName) {
        try {
            return clazz.getDeclaredField(filedName);
        } catch (Exception e) {
            if(!clazz.equals(Object.class)) {
                return findField(clazz.getSuperclass(), filedName);
            }
            throw new RuntimeException(e);
        }
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?> ... argsTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, argsTypes);
        } catch (Exception e) {
            if(!clazz.equals(Object.class)) {
                return findMethod(clazz.getSuperclass(), methodName, argsTypes);
            }
            throw new RuntimeException(e);
        }
    }

    public static void setField(Object instance, Field field, Object value) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(accessible);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getFieldValue(Object instance, String fieldName) {
        try {
            Field field = findField(instance.getClass(), fieldName);
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object result = field.get(instance);
            field.setAccessible(accessible);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(Object instance, Method method, Object ... args) {
        try {
            boolean accessible = method.isAccessible();
            method.setAccessible(true);
            Object result = method.invoke(instance, args);
            method.setAccessible(accessible);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
