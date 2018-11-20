package org.nanospark.beanmapping.core;


import org.nanospark.beanmapping.common.exception.BeanMappingException;

import java.util.function.Function;

import static org.nanospark.beanmapping.common.exception.BeanMappingException.ConstantMessage.DO_CONVERT_FAILED;

class ConverterChain<T, R> {


    private final Function[] converterChain;

    ConverterChain(Function[] converterChain) {
        this.converterChain = converterChain;
    }

    @SuppressWarnings("unchecked")
    R convert(T t) {
        Object tmpValue = t;
        for (Function converter : converterChain) {
            try {
                tmpValue = converter.apply(tmpValue);
            } catch (Throwable e) {
                throw new BeanMappingException(DO_CONVERT_FAILED +
                        ";\n converter is " + converter +
                        ", parameter is " + tmpValue +
                        "\n reason is " + e, e);
            }
        }
        return (R) tmpValue;
    }

}
