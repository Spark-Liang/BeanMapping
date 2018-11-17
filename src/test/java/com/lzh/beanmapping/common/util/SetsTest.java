package com.lzh.beanmapping.common.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings({"unchecked", "ConstantConditions", "ArraysAsListWithZeroOrOneArgument"})
public class SetsTest {

    /**
     * Test {@link Sets#toSet(Collection)}
     */
    @Test
    public void returnNullWhenProvideNullCollection() {
        //when
        Set result = Sets.toSet(null);

        //then
        assertThat(result).isNull();
    }

    /**
     * Test {@link Sets#toSet(Collection)}
     */
    @Test
    public void returnSetWhenProvideACollection() {
        //given
        Collection collection = new LinkedList<>();
        collection.add(new Object());

        //when
        Set result = Sets.toSet(collection);

        //then
        assertThat(result).isNotNull();
    }

    /**
     * Test {@link Sets#intersection(Set, Set)}
     */
    @Test
    public void returnCommonComponentsOfGivenSet() {
        //given
        Set<String> a = Sets.toSet(Arrays.asList("A", "B")),
                b = Sets.toSet(Arrays.asList("A"));

        //when
        Set<String> result = Sets.intersection(a, b);

        //then
        assertThat(result).containsOnly("A");

    }

    /**
     * Test {@link Sets#intersection(Set, Set)}
     */
    @Test
    public void returnNullWhenFirstSetIsNull() {
        //given
        Set<String> a = Sets.toSet(Arrays.asList("A", "B")),
                b = null;

        //when
        Set<String> result = Sets.intersection(a, b);

        //then
        assertThat(result).isNull();

    }

    /**
     * Test {@link Sets#intersection(Set, Set)}
     */
    @Test
    public void returnNullWhenSecondSetIsNull() {
        //given
        Set<String> a = null,
                b = Sets.toSet(Arrays.asList("A"));

        //when
        Set<String> result = Sets.intersection(a, b);

        //then
        assertThat(result).isNull();

    }

    /**
     * Test {@link Sets#union(Set, Set)}
     */
    @Test
    public void returnAllComponentInAllSet() {
        //given
        Set<String> a = Sets.toSet(Arrays.asList("A", "B")),
                b = Sets.toSet(Arrays.asList("C"));

        //when
        Set<String> result = Sets.union(a, b);

        //then
        assertThat(result).containsOnly("A", "B", "C");
    }

    /**
     * Test {@link Sets#union(Set, Set)}
     */
    @Test
    public void returnAllComponentInFirstSetWhenSecondIsNull() {
        //given
        Set<String> a = Sets.toSet(Arrays.asList("A", "B")),
                b = null;

        //when
        Set<String> result = Sets.union(a, b);

        //then
        assertThat(result).containsOnly(a.toArray(new String[0]));
    }

    /**
     * Test {@link Sets#union(Set, Set)}
     */
    @Test
    public void returnAllComponentInSecondSetWhenFirstIsNull() {
        //given
        Set<String> a = null,
                b = Sets.toSet(Arrays.asList("C"));

        //when
        Set<String> result = Sets.union(a, b);

        //then
        assertThat(result).containsOnly(b.toArray(new String[0]));
    }

    /**
     * Test {@link Sets#union(Set, Set)}
     */
    @Test
    public void returnNullWhenAllSetIsNull() {
        //given
        Set<String> a = null,
                b = null;

        //when
        Set<String> result = Sets.union(a, b);

        //then
        assertThat(result).isNull();
    }

    /**
     * Test {@link Sets#complement(Set, Set)}
     */
    @Test
    public void returnAllComponentInFirstButNotInSecond() {
        //given
        Set<String> a = Sets.toSet(Arrays.asList("A", "B")),
                b = Sets.toSet(Arrays.asList("B", "D"));

        //when
        Set<String> result = Sets.complement(a, b);

        //then
        assertThat(result).containsOnly("A");
    }

    /**
     * Test {@link Sets#complement(Set, Set)}
     */
    @Test
    public void returnNullWhenFirstSetIsNull_complement() {
        //given
        Set<String> a = null,
                b = Sets.toSet(Arrays.asList("B", "D"));

        //when
        Set<String> result = Sets.complement(a, b);

        //then
        assertThat(result).isNull();
    }

    /**
     * Test {@link Sets#complement(Set, Set)}
     */
    @Test
    public void returnAllComponentInFirstWhenSecondIsNull() {
        //given
        Set<String> a = Sets.toSet(Arrays.asList("A", "B")),
                b = null;

        //when
        Set<String> result = Sets.complement(a, b);

        //then
        assertThat(result).containsOnly(a.toArray(new String[0]));
    }


}