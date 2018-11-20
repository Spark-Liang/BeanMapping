package org.nanospark.beanmapping.core;

import java.util.function.Function;

public class ConverterWithDefaultConstructor implements Function<String, String> {

    @Override
    public String apply(String o) {
        return o;
    }
}
