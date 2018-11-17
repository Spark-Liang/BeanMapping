package com.lzh.beanmapping.core.annotation;

import com.lzh.beanmapping.common.PropertiesSourceObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * This annotation is used to define the properties mapping between POJO and PO.<br>
 * If you want to config the relationship between the property in the POJO and properties provider,
 * you need to place this annotation on the field or the set method.
 * And place on the set method is recommend.Because you need to make sure field name is
 * same as the property name.<br>
 * If this annotation place in other method it will be ignore.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface DataMapping {
    /**
     * define the property source class of the described property
     *
     * @return the class that provide the property
     */
    Class<? extends PropertiesSourceObject> sourceClass();

    /**
     * define use which property in source class to set the described property
     *
     * @return the name of the property to be provided
     */
    String sourceProperty();

    /**
     * define a convert chain that can convert the source property to target property.
     * the converter chain must meet this restriction :
     * <ul>
     *     <li>Each of the converter in the chain must have the default constructor.</li>
     *     <li>The input parameter of each of the converter can be assigned from the return type of the previous converter.</li>
     *     <li>First converter's input type can be assign from the source property</li>
     *     <li>The last converter's return type can be assign to the target property</li>
     * </ul>
     *
     *
     * @return array of the converters
     */
    Class<? extends Function>[] toTargetConverterChain() default {};

    /**
     * define a convert chain that can convert the target property to source property.
     * the converter chain must meet this restriction :
     * <ul>
     *     <li>Each of the converter in the chain must have the default constructor.</li>
     *     <li>The input parameter of each of the converter can be assigned from the return type of the previous converter.</li>
     *     <li>First converter's input type can be assign from the target property</li>
     *     <li>The last converter's return type can be assign to the source property</li>
     * </ul>
     * @return array of the converters
     */
    Class<? extends Function>[] toSourceConverterChain() default {};

}
