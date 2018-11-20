package org.nanospark.beanmapping.common.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ArrayUtilsTest {

    @Test
    public void canConcateGivenArrays() {
        //given
        Object[] arr1 = new Object[]{new Object(), new Object()},
                arr2 = new Object[]{new Object()};

        //when
        Object[] result = ArrayUtils.concat(arr1, arr2);
        assertThat(result).hasSize(arr1.length + arr2.length);
        assertThat(result).contains(arr1);
        assertThat(result).contains(arr2);
    }

    @Test
    public void canConcateWhenFirstGivenArrayisNull() {
        //given
        Object[] arr1 = null,
                arr2 = new Object[]{new Object()};

        //when
        Object[] result = ArrayUtils.concat(arr1, arr2);
        assertThat(result).hasSize(arr2.length);
        assertThat(result).contains(arr2);
    }

    @Test
    public void canConcateWhenSecondGivenArrayisNull() {
        //given
        Object[] arr1 = new Object[]{new Object(), new Object()},
                arr2 = null;

        //when
        Object[] result = ArrayUtils.concat(arr1, arr2);
        assertThat(result).hasSize(arr1.length);
        assertThat(result).contains(arr1);
    }

    @Test
    public void returnNullWhenAllGivenArrayisNull() {
        //given
        Object[] arr1 = null,
                arr2 = null;

        //when
        Object[] result = ArrayUtils.concat(arr1, arr2);
        assertThat(result).isNull();
    }

    @Test
    public void canReturnRightTypeArray() {
        //given
        ClassForArraysUtilsTest[] arr1 = new ClassForArraysUtilsTest[]{new ClassForArraysUtilsTest("A"), new ClassForArraysUtilsTest("B")},
                arr2 = new ClassForArraysUtilsTest[]{new ClassForArraysUtilsTest("C")};

        //when
        ClassForArraysUtilsTest[] result = ArrayUtils.concat(arr1, arr2);
        assertThat(result).hasSize(arr1.length + arr2.length);
        assertThat(result).contains(arr1);
        assertThat(result).contains(arr2);
    }

    @Test
    public void canDofilterDuringConcat() {
        //given
        ClassForArraysUtilsTest[] arr1 = new ClassForArraysUtilsTest[]{new ClassForArraysUtilsTest("A"), new ClassForArraysUtilsTest("B")},
                arr2 = new ClassForArraysUtilsTest[]{new ClassForArraysUtilsTest("C")};

        //when
        ClassForArraysUtilsTest[] result = ArrayUtils.concat(arr1, arr2, e -> e.getName().equals("C"));
        assertThat(result).doesNotContain(arr1);
        assertThat(result).contains(arr2);
    }
}

@SuppressWarnings("WeakerAccess")
class ClassForArraysUtilsTest {
    private String name;

    public ClassForArraysUtilsTest() {
    }

    public ClassForArraysUtilsTest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}