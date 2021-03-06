package com.lzh.beanmapping.common.util;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class ReflectUtils {

    /**
     * get all declare field from given class,and do some filtering based on given predicate
     *
     * @param clazz     the class to analyzed
     * @param stopClass The base class at which to stop the analysis.
     *                  if it was null value will set Object.class as default value
     * @param predicate do filter base on this parameter
     * @return array of Fields
     * @throws RuntimeException when StopClass can not be the subclass of given class will throw this exception
     */
    public static Field[] getAllFields(Class clazz, Class stopClass, Predicate<Field> predicate) {
        if (stopClass == null) {
            stopClass = Object.class;
        }
        if (!checkStopClassIsNotSubClassOfClazz(clazz, stopClass)) {
            throw new RuntimeException("StopClass can not be the subclass of given class");
        }
        Field[] fields = null;
        fields = clazz.getDeclaredFields();
        Class superClass = clazz.getSuperclass();
        while (superClass != null && stopClass.isAssignableFrom(superClass)) {
            fields = ArrayUtils.concat(fields, superClass.getDeclaredFields(), predicate);
            superClass = superClass.getSuperclass();
        }
        return fields;
    }

    private static boolean checkStopClassIsNotSubClassOfClazz(Class clazz, Class stopClass) {
        if (stopClass.equals(Object.class))
            return true;

        Class currentClass = clazz;
        while (!currentClass.equals(Object.class)) {
            if (currentClass.equals(stopClass)) {
                return true;
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

    /**
     * this method will get all its fields includes all its superclass except Object.class
     *
     * @see ReflectUtils#getAllFields(Class, Class)
     */
    public static Field[] getAllFields(Class clazz) {
        return getAllFields(clazz, Object.class);
    }

    /**
     * this method will get all its fields util stopClass and do nothing on filtering
     *
     * @see ReflectUtils#getAllFields(Class, Class, Predicate)
     */
    public static Field[] getAllFields(Class clazz, Class stopClass) {
        return getAllFields(clazz, stopClass, null);
    }

    /**
     * this method will get all its fields and do filtering based on given predicate
     *
     * @see ReflectUtils#getAllFields(Class, Class, Predicate)
     */
    public static Field[] getAllFields(Class clazz, Predicate<Field> predicate) {
        return getAllFields(clazz, Object.class, predicate);
    }
}
