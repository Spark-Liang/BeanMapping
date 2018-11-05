package com.lzh.beanmapping.common.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * do nothing filtering by set predicate as null
     *
     * @see ArrayUtils#concat(Object[], Object[], Predicate)
     */
    public static <T> T[] concat(T[] arr1, T[] arr2) {
        return concat(arr1, arr2, null);
    }

    /**
     * do concat on given arrays,and do filter to all element to the element based on given predicate
     *
     * @param arr1      array to do concat
     * @param arr2      array to do concat
     * @param predicate predicate to do filter
     * @param <T>       specialize the type of array component
     * @return array after concat and filtering
     */
    public static <T> T[] concat(T[] arr1, T[] arr2, Predicate<T> predicate) {
        Class arrClass = null;
        if (arr1 == null) {
            if (arr2 == null) {
                return null;
            } else {
                arrClass = arr2.getClass();
            }
        } else {
            arrClass = arr1.getClass();
        }
        T[] exampleArray = (T[]) Array.newInstance(arrClass.getComponentType(), 0);

        return Arrays.asList(arr1, arr2).stream()
                .filter(e -> e != null)
                .flatMap(arr -> Arrays.stream(arr))
                .filter(e -> predicate == null || predicate.test(e))
                .collect(toList())
                .toArray(exampleArray);
    }
}
