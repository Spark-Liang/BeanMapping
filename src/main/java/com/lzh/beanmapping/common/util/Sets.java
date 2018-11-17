package com.lzh.beanmapping.common.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class Sets {

    /**
     * convert the given {@link Collection} to {@link Set}
     *
     * @param collection the collection to be converted
     * @param <E>        Type of the components in the collection
     * @return instance of {@link Set}
     */
    public static <E> Set<E> toSet(Collection<E> collection) {
        return collection != null ? new HashSet<>(collection) : null;
    }

    /**
     * do intersect on the given {@link Set} a and {@link Set} b
     *
     * @param a   {@link Set} to be intersected
     * @param b   {@link Set} to be intersected
     * @param <E> Type of the components in the collection
     * @return return null if one of the given {@link Set} is null
     * ,        otherwise return the {@link Set} contain the common components between a and b
     */
    public static <E> Set<E> intersection(Set<E> a, Set<E> b) {
        if (a == null || b == null)
            return null;
        Set<E> largerSet,
                smallerSet;
        if (a.size() > b.size()) {
            largerSet = a;
            smallerSet = b;
        } else {
            largerSet = b;
            smallerSet = a;
        }
        return smallerSet.stream()
                .filter(largerSet::contains)
                .collect(Collectors.toSet());
    }

    /**
     * do union operation on the given {@link Set} a and {@link Set} b
     *
     * @param a   {@link Set} to do union operation
     * @param b   {@link Set} to do union operation
     * @param <E> Type of the components in the collection
     * @return return null if all of the given {@link Set} is null
     * ,        otherwise return the {@link Set} contain all the components in a or b
     */
    public static <E> Set<E> union(Set<E> a, Set<E> b) {
        if (a == null && b == null)
            return null;
        if (a == null) {
            return toSet(b);
        } else if (b == null) {
            return toSet(a);
        } else {
            Set<E> result = new HashSet<>(a);
            result.addAll(b);
            return result;
        }
    }

    /**
     * do complement on the {@link Set} a and {@link Set} b
     *
     * @param a   the base {@link Set}
     * @param b   all the component in {@link Set} to have to be remove from {@link Set} a
     * @param <E> Type of the components in the collection
     * @return return null if a is null, otherwise return the {@link Set} contain all the components in a but not in b
     */
    public static <E> Set<E> complement(Set<E> a, Set<E> b) {
        if (a == null)
            return null;
        if (b == null)
            return toSet(a);
        return a.stream()
                .filter(e -> !b.contains(e))
                .collect(Collectors.toSet());
    }
}


