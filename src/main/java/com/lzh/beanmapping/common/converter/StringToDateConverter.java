package com.lzh.beanmapping.common.converter;

import java.util.Date;
import java.util.function.Function;

public class StringToDateConverter implements Function<String, Date> {

    public StringToDateConverter(){}

    @Override
    public Date apply(String string) {
        return new Date(string);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }
}
