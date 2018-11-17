package com.lzh.beanmapping.core;

import java.util.function.Function;

@SuppressWarnings("unused")
public class ConvertersForTest {


}

@SuppressWarnings({"UnusedAssignment", "ParameterCanBeLocal"})
class ConverterWithoutConstructer implements Function<String, String> {

    public ConverterWithoutConstructer(String value) {
        value = null;
    }

    @Override
    public String apply(String o) {
        return o;
    }

}

class ConverterCanNotConstruct implements Function<String, String> {

    public ConverterCanNotConstruct() {
        throw new RuntimeException();
    }

    @Override
    public String apply(String o) {
        return o;
    }
}

@SuppressWarnings("unused")
interface FakeFunction {
    String apply(Integer o);
}

class ConverterWithDuplicatedImplement implements Function<String, Integer>, FakeFunction {

    public ConverterWithDuplicatedImplement() {
    }

    @Override
    public Integer apply(String o) {
        return 1;
    }

    @Override
    public String apply(Integer o) {
        return null;
    }
}

class ConverterFromStringToIntegerForTest implements Function<String, Integer> {

    public ConverterFromStringToIntegerForTest() {
    }

    @Override
    public Integer apply(String o) {
        return 1;
    }
}

class ConverterFromIntegerToStringForTest implements Function<Integer, String> {

    public ConverterFromIntegerToStringForTest() {
    }

    @Override
    public String apply(Integer o) {
        return "";
    }
}

class ConverterUsingGenericType<T> implements Function<T, T> {

    public ConverterUsingGenericType() {
    }

    @Override
    public T apply(T o) {
        return o;
    }
}

class ConverterUsingGenericTypeButNotBoundByObject<T extends String> implements Function<T, T> {

    public ConverterUsingGenericTypeButNotBoundByObject() {
    }

    @Override
    public T apply(T o) {
        return o;
    }
}

