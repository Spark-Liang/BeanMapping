package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.exception.BeanMappingException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

public class MappingInfoItem {
    private static final String CONVERTER_METHOD_NAME = "apply";

    private PropertyDescriptor sourceGetter;
    private PropertyDescriptor targetSetter;
    private boolean needDeepCopy;
    private Function converter;

    public MappingInfoItem() {
    }

    public void verify() {
        verifyPropertyNameIsEqual();
        verifyCoverterCanBeUsed();
    }

    private void verifyPropertyNameIsEqual() {
        if (sourceGetter == null) {
            throw new BeanMappingException("source property getter is null");
        }
        if (targetSetter == null) {
            throw new BeanMappingException("target property setter is null");
        }
    }

    private void verifyCoverterCanBeUsed() {
        if (converter != null) {
            Class sourcePropertyType = sourceGetter.getPropertyType();
            Class coverterClass = converter.getClass();
            try {
                Method convertMethod = coverterClass.getMethod(CONVERTER_METHOD_NAME, new Class[]{sourcePropertyType});
                if (convertMethod == null) {
                    throw new BeanMappingException("covert method does not match source type");
                }
                Class convertMethodReturnType = convertMethod.getReturnType(), targetPropertyType = targetSetter.getPropertyType();
                if (!targetPropertyType.equals(convertMethodReturnType)) {
                    throw new BeanMappingException("return type of covert method does not match target property type");
                }
            } catch (NoSuchMethodException e) {
                throw new BeanMappingException("covert method does not match source type", e);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MappingInfoItem that = (MappingInfoItem) o;
        return needDeepCopy == that.needDeepCopy &&
                Objects.equals(sourceGetter, that.sourceGetter) &&
                Objects.equals(targetSetter, that.targetSetter) &&
                Objects.equals(converter, that.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceGetter, targetSetter, needDeepCopy, converter);
    }

    @Override
    public String toString() {
        return "MappingInfoItem{" +
                "sourceGetter=" + sourceGetter +
                ", targetSetter=" + targetSetter +
                ", needDeepCopy=" + needDeepCopy +
                ", converter=" + converter +
                '}';
    }

    public PropertyDescriptor getSourceGetter() {
        return sourceGetter;
    }

    public void setSourceGetter(PropertyDescriptor sourceGetter) {
        this.sourceGetter = sourceGetter;
    }

    public PropertyDescriptor getTargetSetter() {
        return targetSetter;
    }

    public void setTargetSetter(PropertyDescriptor targetSetter) {
        this.targetSetter = targetSetter;
    }

    public boolean isNeedDeepCopy() {
        return needDeepCopy;
    }

    public void setNeedDeepCopy(boolean needDeepCopy) {
        this.needDeepCopy = needDeepCopy;
    }

    public Function getConverter() {
        return converter;
    }

    public void setConverter(Function converter) {
        this.converter = converter;
    }
}
