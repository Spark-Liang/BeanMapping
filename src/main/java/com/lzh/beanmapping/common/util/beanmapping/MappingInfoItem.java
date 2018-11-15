package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.exception.BeanMappingException;
import com.lzh.beanmapping.common.util.ArrayUtils;
import org.springframework.cglib.core.Constants;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lzh.beanmapping.common.exception.BeanMappingException.ConstantMessage.*;

/**
 * To describe all the information about bean mapping for one property
 */
public class MappingInfoItem {
    private static final String CONVERTER_METHOD_NAME = "apply";
    private static final Class[] CONVERTER_METHOD_PARAMS = new Class[]{Object.class};

    private PropertyDescriptor sourceProperty;
    private PropertyDescriptor targetProperty;
    private Class<? extends Function>[] toTargetConverterChain;
    private Class<? extends Function>[] toSourceConverterChain;

    public MappingInfoItem() {
    }

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
    public void verify() {
        verifyPropertyDescriptor();
        verifyCoverterChain(toSourceConverterChain,targetProperty,sourceProperty);
        verifyCoverterChain(toTargetConverterChain,sourceProperty,targetProperty);
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
     * </ul>
     * @param converterChain the convert chain to be verify
     * @param fromProperty the property that to be convert by the chain
     * @param toProperty the property that chain will return
     */
    void verifyCoverterChain(Class<? extends Function>[] converterChain,
                                     PropertyDescriptor fromProperty, 
                                     PropertyDescriptor toProperty) {
        if (ArrayUtils.isNotEmpty(converterChain)) {
            Class<? extends Function> previousConverterClass = null, currentConverterClass;
            Iterator<Class<? extends Function>> iterator = ArrayUtils.iterator(converterChain);

            if (iterator.hasNext()) {
                currentConverterClass = iterator.next();
                verifyConverterCanConstruct(currentConverterClass);
                Method convertMethod = getConverterMethod(currentConverterClass);
                Class typeToBeProvided = fromProperty.getPropertyType(),
                        typeToBeAssigned = convertMethod.getParameterTypes()[0];
            if(!typeToBeAssigned.isAssignableFrom(typeToBeProvided)){
                    throw new BeanMappingException(CONVERTER_CHAIN_NOT_MATCH_INPUT_TYPE +
                            "; type to be assigned is " + typeToBeAssigned +
                            " type to provide is " + typeToBeProvided);
                }

                while (iterator.hasNext()) {
                    Class nextNotGenericTypeConverterClass = getNextNotGenericTypeConverterClass(iterator);
                    if(nextNotGenericTypeConverterClass == null){
                        continue;
                    }

                    previousConverterClass = currentConverterClass;
                    currentConverterClass = nextNotGenericTypeConverterClass;
                    verifyConverterCanConstruct(currentConverterClass);

                    Method previousConvertMethod = getConverterMethod(previousConverterClass),
                            currentConvertMethod = getConverterMethod(currentConverterClass);

                    if (!canReturnTypeAssignToInputType(previousConvertMethod, currentConvertMethod)) {
                        throw new BeanMappingException(CONVERTER_NOT_MATCH_PREVIOUS_TYPE +
                                "; previous converter is " + previousConverterClass +
                                " current converter is " + currentConverterClass);
                    }
                }

                convertMethod = getConverterMethod(currentConverterClass);
                Class typeOfPropertyToAssign = toProperty.getPropertyType(),
                        providePropertyType = convertMethod.getReturnType();
                if(! typeOfPropertyToAssign.isAssignableFrom(providePropertyType)){
                    throw new BeanMappingException(CONVERTER_CHAIN_NOT_MATCH_RETURN_TYPE +
                            "; type to be assigned is " + typeOfPropertyToAssign +
                            " type to provide is " + providePropertyType);
                }
            }
        }
    }

    private Class getNextNotGenericTypeConverterClass(Iterator<Class<? extends Function>> iterator) {
        //TODD
        return iterator.next();
    }

    private boolean canReturnTypeAssignToInputType(Method method1, Method method2) {
        return method2.getParameterTypes()[0]
                .isAssignableFrom(method1.getReturnType());
    }

    /**
     *
     * get convert method which override {@link Function#apply(Object)} .
     * It's means that the method name is 'apply' and only have one parameter.
     * but this method is not the same method of {@link Function#apply(Object)}
     * @param converterClass the to get convert method
     * @return the convert method
     * @throws {@link BeanMappingException} for {@link BeanMappingException.ConstantMessage#DUPLICATED_CONVERT_METHOD}
     */
    private Method getConverterMethod(Class<? extends Function> converterClass) {
        List<Method> methods = Arrays.stream(converterClass.getMethods())
                .filter(method -> {
                    if (!method.getName().equals(CONVERTER_METHOD_NAME))
                        return false;
                    Class[] params = method.getParameterTypes();
                    return CONVERTER_METHOD_PARAMS.length == params.length;
                })
                .collect(Collectors.toList());
        if(methods.size() == 1){
            return methods.get(0);
        }else if(methods.size() > 1){
            List<Method> methodIsOverride = methods.stream()
                    .filter(method -> {
                        Class[] params = method.getParameterTypes();
                        return ! Arrays.deepEquals(params,CONVERTER_METHOD_PARAMS);
                    })
                    .collect(Collectors.toList());
            if(methodIsOverride.size() == 0){
                return methods.get(0);
            }else if(methodIsOverride.size() == 1){
                return methodIsOverride.get(0);
            }else {
                throw new BeanMappingException(DUPLICATED_CONVERT_METHOD +
                        "; converter class is " + converterClass);
            }
        }else{
            throw new BeanMappingException(NOT_CONVERT_METHOD +
                    "; converter class is " + converterClass);
        }
    }

    private void verifyConverterCanConstruct(Class<? extends Function> converterClass) {
        Constructor<? extends Function> defaultConstruct = null;
        try {
            defaultConstruct = converterClass.getConstructor(Constants.EMPTY_CLASS_ARRAY);
            Function converterInstance = null;
            try {
                converterInstance = defaultConstruct.newInstance();
            } catch (Throwable e) {
                throw new BeanMappingException(CONVERTER_CAN_NOT_CONSTRUCT +
                        "; converter class is " + converterClass, e);
            }
            if (converterInstance == null) {
                throw new BeanMappingException(CONVERTER_CAN_NOT_CONSTRUCT +
                        "; converter class is " + converterClass);
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
