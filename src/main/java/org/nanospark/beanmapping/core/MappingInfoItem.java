package org.nanospark.beanmapping.core;

import org.nanospark.beanmapping.common.exception.BeanMappingException;
import org.nanospark.beanmapping.common.util.ArrayUtils;
import org.springframework.cglib.core.Constants;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import static org.nanospark.beanmapping.common.exception.BeanMappingException.ConstantMessage.*;

/**
 * To describe all the information about bean mapping for one property
 */
@SuppressWarnings({"JavaDoc", "WeakerAccess"})
public class MappingInfoItem {

    private PropertyDescriptor sourceProperty;
    private PropertyDescriptor targetProperty;
    private Class<? extends Function>[] toTargetConverterChain;
    private Class<? extends Function>[] toSourceConverterChain;


    /**
     * verify this instance of {@link MappingInfoItem} is validated
     * the main points of the validation are list as below:
     * <ul>
     * <li>validate getter and setter of the source and the target property is not null</li>
     * <li>validate source and target type when not have converter</li>
     * <li>validate all the converter in the converter chain have the default constructor</li>
     * <li>validate each convert that it's input type match the return type of the previous converter</li>
     * <li>validate converter chain match the type of source property and target property</li>
     * </ul>
     *
     * @throws BeanMappingException
     */
    void verify() {
        verifyPropertyDescriptor();
        verifyConverterChain(toSourceConverterChain, targetProperty, sourceProperty);
        verifyConverterChain(toTargetConverterChain, sourceProperty, targetProperty);
    }

    private void verifyPropertyDescriptor() {
        if (sourceProperty == null) {
            throw new BeanMappingException(SOURCE_IS_NULL);
        } else {
            doVerifyPropertyDescriptor(sourceProperty);
        }
        if (targetProperty == null) {
            throw new BeanMappingException(TARGET_IS_NULL);
        } else {
            doVerifyPropertyDescriptor(targetProperty);
        }
        if (ArrayUtils.isEmpty(toSourceConverterChain)
                || ArrayUtils.isEmpty(toTargetConverterChain)) {
            Class sourceType = sourceProperty.getPropertyType(),
                    targetType = targetProperty.getPropertyType();
            if (!Objects.equals(sourceType, targetType))
                throw new BeanMappingException(PROPERTY_TYPE_IS_DIFFERENT +
                        "; sourceProperty is " + sourceType +
                        " targetProperty is " + targetType);
        }
    }

    private void doVerifyPropertyDescriptor(PropertyDescriptor descriptor) {
        if (descriptor.getReadMethod() == null) {
            throw new BeanMappingException(GET_METHOD_IS_NULL + " ; the reference property is " + descriptor);
        }
        if (descriptor.getWriteMethod() == null) {
            throw new BeanMappingException(SET_METHOD_IS_NULL + " ; the reference property is " + descriptor);
        }
    }

    /**
     * do verify on the converter chain , and the validation include :
     * <ul>
     *     <li>validate the input type of the chain match the type of fromProperty</li>
     *     <li>the input type of each converter must match the return type of the previous converter</li>
     *     <li>validate the output type of the chain match the type of toProperty</li>
     *     <li>will ignore all the converter that have the generic type convert method
     *     , and this method have the same generic type in input type and return type</li>
     * </ul>
     * @param converterChain the convert chain to be verify
     * @param fromProperty the property that to be convert by the chain
     * @param toProperty the property that chain will return
     */
    @SuppressWarnings("unchecked")
    void verifyConverterChain(Class<? extends Function>[] converterChain,
                              PropertyDescriptor fromProperty,
                              PropertyDescriptor toProperty) {
        if (ArrayUtils.isNotEmpty(converterChain)) {
            Class<? extends Function> currentConverterClass;
            Class typeToBeProvided,
                    typeToBeAssigned;

            Iterator<Class<? extends Function>> iterator = ArrayUtils.iterator(converterChain);

            //do verify on 'fromProperty' can assign to chain's input type
            //if not have converter will change type than directly to verify the type of fromProperty and target property
            typeToBeProvided = fromProperty.getPropertyType();
            currentConverterClass = skipToNextConverterThatWillChangeType(iterator);
            if (currentConverterClass != null) {
                typeToBeAssigned = ConverterUtils.getConverterMethodInputType(currentConverterClass);
                if (!typeToBeAssigned.isAssignableFrom(typeToBeProvided)) {
                    throw new BeanMappingException(CONVERTER_CHAIN_NOT_MATCH_INPUT_TYPE +
                            ";\n type to be provided is " + typeToBeProvided +
                            ", type to be assigned is " + typeToBeAssigned);
                }
                typeToBeProvided = ConverterUtils.getConverterMethodReturnType(currentConverterClass);

                //do verify on converter chain body
                while (iterator.hasNext()) {
                    currentConverterClass = skipToNextConverterThatWillChangeType(iterator);
                    if (currentConverterClass == null) {
                        continue;
                    }
                    typeToBeAssigned = ConverterUtils.getConverterMethodInputType(currentConverterClass);

                    if (!typeToBeAssigned.isAssignableFrom(typeToBeProvided)) {
                        throw new BeanMappingException(CONVERTER_NOT_MATCH_PREVIOUS_TYPE +
                                ";\n type to be provided is " + typeToBeProvided +
                                ", type to be assigned is " + typeToBeAssigned +
                                "\n current converter is " + currentConverterClass);
                    }

                    typeToBeProvided = ConverterUtils.getConverterMethodReturnType(currentConverterClass);
                }
            }

            //do verify on 'toProperty' can be assign from chain's return type
            typeToBeAssigned = toProperty.getPropertyType();
            if (!typeToBeAssigned.isAssignableFrom(typeToBeProvided)) {
                throw new BeanMappingException(CONVERTER_CHAIN_NOT_MATCH_RETURN_TYPE +
                        ";\n type to be provided is " + typeToBeProvided +
                        ", type to be assigned is " + typeToBeAssigned);
            }
        }
    }


    private Class skipToNextConverterThatWillChangeType(Iterator<Class<? extends Function>> iterator) {
        Class<? extends Function> currentConverterClass = null;
        while (iterator.hasNext()) {
            currentConverterClass = iterator.next();
            Method convertMethod = ConverterUtils.getConverterMethod(currentConverterClass);
            Type genericInputType = convertMethod.getGenericParameterTypes()[0],
                    genericReturnType = convertMethod.getGenericReturnType();
            if (genericInputType == null) {
                throw new BeanMappingException(SYSTEM_EXCEPTION +
                        "\n generic input type of " + convertMethod + " is null");
            }
            if (genericReturnType == null) {
                throw new BeanMappingException(SYSTEM_EXCEPTION +
                        "\n generic return type of " + convertMethod + " is null");
            }
            if (genericInputType instanceof TypeVariable
                    && genericReturnType instanceof TypeVariable
                    && genericInputType.equals(genericReturnType)) {

                if (!Object.class.equals(convertMethod.getReturnType())) {
                    throw new BeanMappingException(GENERIC_TYPE_OF_CONVERTER_IS_NOT_OBJECT);
                }
                verifyConverterCanConstruct(currentConverterClass);

                //skip this converter
                currentConverterClass = null;
            } else {
                break;
            }
        }
        if (currentConverterClass != null) {
            verifyConverterCanConstruct(currentConverterClass);
        }

        return currentConverterClass;
    }

    private void verifyConverterCanConstruct(Class<? extends Function> converterClass) {
        try {
            Constructor<? extends Function> defaultConstruct = converterClass.getConstructor(Constants.EMPTY_CLASS_ARRAY);
            try {
                //noinspection JavaReflectionInvocation
                defaultConstruct.newInstance();
            } catch (Throwable e) {
                throw new BeanMappingException(CONVERTER_CAN_NOT_CONSTRUCT +
                        "; converter class is " + converterClass, e);
            }
        } catch (NoSuchMethodException e) {
            throw new BeanMappingException(CONVERTER_NOT_HAS_DEFAULT_CONSTRUCTOR +
                    "; converter class is " + converterClass, e);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MappingInfoItem infoItem = (MappingInfoItem) o;
        return Objects.equals(sourceProperty, infoItem.sourceProperty) &&
                Objects.equals(targetProperty, infoItem.targetProperty) &&
                Arrays.equals(toTargetConverterChain, infoItem.toTargetConverterChain) &&
                Arrays.equals(toSourceConverterChain, infoItem.toSourceConverterChain);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sourceProperty, targetProperty);
        result = 31 * result + Arrays.hashCode(toTargetConverterChain);
        result = 31 * result + Arrays.hashCode(toSourceConverterChain);
        return result;
    }

    @Override
    public String toString() {
        return "MappingInfoItem{" +
                "sourceProperty=" + sourceProperty +
                ", targetProperty=" + targetProperty +
                ", toTargetConverterChain=" + Arrays.toString(toTargetConverterChain) +
                ", toSourceConverterChain=" + Arrays.toString(toSourceConverterChain) +
                '}';
    }

    public PropertyDescriptor getSourceProperty() {
        return sourceProperty;
    }

    public void setSourceProperty(PropertyDescriptor sourceProperty) {
        this.sourceProperty = sourceProperty;
    }

    public PropertyDescriptor getTargetProperty() {
        return targetProperty;
    }

    public void setTargetProperty(PropertyDescriptor targetProperty) {
        this.targetProperty = targetProperty;
    }

    public Class<? extends Function>[] getToTargetConverterChain() {
        return toTargetConverterChain;
    }

    public void setToTargetConverterChain(Class<? extends Function>[] toTargetConverterChain) {
        this.toTargetConverterChain = toTargetConverterChain;
    }

    public Class<? extends Function>[] getToSourceConverterChain() {
        return toSourceConverterChain;
    }

    public void setToSourceConverterChain(Class<? extends Function>[] toSourceConverterChain) {
        this.toSourceConverterChain = toSourceConverterChain;
    }
}

