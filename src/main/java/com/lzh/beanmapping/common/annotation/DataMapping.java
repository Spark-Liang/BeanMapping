package com.lzh.beanmapping.common.annotation;

import com.lzh.beanmapping.common.PropertiesSourceObject;

import java.lang.annotation.*;
import java.util.function.Function;

/**
 * This annotation is used to define the properties mapping between POJO and PO
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DataMapping {
    Class DEFAULT_CONVERTER_CLASS = DefaultConverter.class;

    /**
     * define the property source class of the described property
     *
     * @return
     */
    Class<? extends PropertiesSourceObject> sourceClass();

    /**
     * define use which property in source class to set the described property
     *
     * @return
     */
    String sourceProperty();

    /**
     * when this annotation is describe a {@link java.util.Map}, {@link java.util.Collection} or a POJO
     * this setting is to control the copier whether to make a deepCopy on this property or not.
     *
     * @return
     */
    boolean needDeepCopy() default false;

    /**
     * when this annotation is describe a property which parameter type is different from source property
     * use this setting to provide converter
     *
     * @return
     */
    Class<? extends Function> converter() default DefaultConverter.class;

}

class DefaultConverter implements Function {
    @Override
    public Object apply(Object o) {
        return o;
    }

}
