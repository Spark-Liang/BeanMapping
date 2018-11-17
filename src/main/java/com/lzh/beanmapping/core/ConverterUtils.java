package com.lzh.beanmapping.core;

import com.lzh.beanmapping.common.exception.BeanMappingException;
import org.springframework.asm.Type;
import org.springframework.cglib.core.Signature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lzh.beanmapping.common.exception.BeanMappingException.ConstantMessage.DUPLICATED_CONVERT_METHOD;
import static com.lzh.beanmapping.common.exception.BeanMappingException.ConstantMessage.NOT_CONVERT_METHOD;

/**
 * This Class Is Use for processing the converter class
 */
@SuppressWarnings("JavaDoc")
class ConverterUtils {
    static final Signature CONVERTER_METHOD;

    private static final String CONVERTER_METHOD_NAME = "apply";
    private static final Class[] CONVERTER_METHOD_PARAMS = new Class[]{Object.class};

    static Class getConverterMethodReturnType(Class<? extends Function> currentConverterClass) {
        return getConverterMethod(currentConverterClass).getReturnType();
    }

    static Class getConverterMethodInputType(Class<? extends Function> currentConverterClass) {
        return getConverterMethod(currentConverterClass).getParameterTypes()[0];
    }

    /**
     * get convert method which override {@link Function#apply(Object)} .
     * It's means that the method name is 'apply' and only have one parameter.
     * but this method is not the same method of {@link Function#apply(Object)}
     *
     * @param converterClass the to get convert method
     * @return the convert method
     * @throws {@link BeanMappingException} for {@link BeanMappingException.ConstantMessage#DUPLICATED_CONVERT_METHOD}
     */
    static Method getConverterMethod(Class<? extends Function> converterClass) {
        List<Method> methods = Arrays.stream(converterClass.getMethods())
                .filter(method -> {
                    if (!method.getName().equals(CONVERTER_METHOD_NAME))
                        return false;
                    Class[] params = method.getParameterTypes();
                    return CONVERTER_METHOD_PARAMS.length == params.length;
                })
                .collect(Collectors.toList());
        if (methods.size() == 1) {
            return methods.get(0);
        } else if (methods.size() > 1) {
            List<Method> methodIsOverride = methods.stream()
                    .filter(method -> {
                        Class[] params = method.getParameterTypes();
                        return !Arrays.deepEquals(params, CONVERTER_METHOD_PARAMS);
                    })
                    .collect(Collectors.toList());
            if (methodIsOverride.size() == 0) {
                return methods.get(0);
            } else if (methodIsOverride.size() == 1) {
                return methodIsOverride.get(0);
            } else {
                throw new BeanMappingException(DUPLICATED_CONVERT_METHOD +
                        "; converter class is " + converterClass);
            }
        } else {
            throw new BeanMappingException(NOT_CONVERT_METHOD +
                    "; converter class is " + converterClass);
        }
    }

    static {
        Method convertMethod = null;
        try {
            convertMethod = Function.class.getDeclaredMethod("apply", new Class[]{Object.class});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        CONVERTER_METHOD = new Signature(convertMethod.getName(),
                Type.getReturnType(convertMethod),
                Type.getArgumentTypes(convertMethod));
    }
}