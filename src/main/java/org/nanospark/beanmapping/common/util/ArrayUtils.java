package org.nanospark.beanmapping.common.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


@SuppressWarnings("WeakerAccess")
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
    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[] arr1, T[] arr2, Predicate<T> predicate) {
        Class arrClass;
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

        return Stream.of(arr1, arr2)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .filter(e -> predicate == null || predicate.test(e))
                .collect(toList())
                .toArray(exampleArray);
    }

    /**
     * check the array is null or length is equals to zero
     * @param arr array to test
     * @return is empty or not
     */
    public static boolean isEmpty(Object[] arr){
        return !isNotEmpty(arr);
    }

    /**
     * check the length of array is equals not zero
     * @param arr array to test
     * @return is not empty or not
     */
    public static boolean isNotEmpty(Object[] arr){
        return arr != null && arr.length > 0;
    }

    /**
     * create an iterator from the given array
     * @param arr the array to be iterate
     * @param <T> component type of the array
     * @return {@link Iterator}
     */
    public static <T> Iterator<T> iterator(T[] arr){
        return Arrays.asList(arr).iterator();
    }
}
