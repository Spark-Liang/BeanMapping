package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.annotation.DataMapping;
import com.lzh.beanmapping.common.exception.BeanMappingException;
import com.lzh.beanmapping.common.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/**
 * This class is used to describe Mapping info of a POJO
 */
public class BeanMappingInfo {
    private static Logger logger = LoggerFactory.getLogger(BeanMappingInfo.class);

    private Map<Class<? extends PropertiesSourceObject>, Set<MappingInfoItem>> mappingInfos;
    private final Class targetClass;

    private BeanMappingInfo(Class targetClass) {
        this.targetClass = targetClass;
    }

    void parserMappingInfo() {
        if (mappingInfos == null) {
            Map<Class<? extends PropertiesSourceObject>, List<Field>> fieldMap = filterField(targetClass);
            mappingInfos
                    = fieldMap.entrySet().stream()
                    .collect(toMap(entry -> entry.getKey()
                            , entry -> parserMappingInfos(entry.getKey(), entry.getValue())));
        }
    }

    private Set<MappingInfoItem> parserMappingInfos(Class sourceClass, List<Field> fields) {
        PropertyDescriptor[] sourceDescriptors = null, targetDescriptors = null;
        try {
            sourceDescriptors = Introspector.getBeanInfo(sourceClass).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            logger.info("fail to parser beaninfo from sourceClass :{}", sourceClass);
            throw new RuntimeException(e);
        }
        try {
            targetDescriptors = Introspector.getBeanInfo(targetClass).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            logger.info("fail to parser beaninfo from targetClass :{}", targetClass);
            throw new RuntimeException(e);
        }
        Map<String, PropertyDescriptor> sourceDescriportsMap
                = Arrays.stream(sourceDescriptors)
                .collect(toMap(descriptor -> descriptor.getName(), e -> e));
        Map<String, PropertyDescriptor> targetDescriportsMap
                = Arrays.stream(targetDescriptors)
                .collect(toMap(descriptor -> descriptor.getName(), e -> e));
        return fields.stream()
                .map(field -> {
                    MappingInfoItem infoItem = new MappingInfoItem();

                    String targetPropertyName = field.getName();
                    PropertyDescriptor targetSetter = targetDescriportsMap.get(targetPropertyName);
                    if (targetSetter == null) {
                        logger.info("can not found setter of target property {} from source class {}", targetPropertyName, targetClass);
                        throw new BeanMappingException("can not found setter of source property from target class");
                    } else {
                        infoItem.setTargetSetter(targetSetter);
                    }

                    DataMapping dataMapping = field.getDeclaredAnnotation(DataMapping.class);
                    String sourcePropertyName = dataMapping.sourceProperty();
                    PropertyDescriptor sourceGetter = sourceDescriportsMap.get(sourcePropertyName);
                    if (sourceGetter == null) {
                        logger.info("can not found getter of source property {} from source class {}", sourcePropertyName, sourceClass);
                        throw new BeanMappingException("can not found source property from source class");
                    } else {
                        infoItem.setSourceGetter(sourceGetter);
                    }
                    infoItem.setNeedDeepCopy(dataMapping.needDeepCopy());

                    Class converterClass = dataMapping.converter();
                    if (!converterClass.equals(DataMapping.DEFAULT_CONVERTER_CLASS)) {
                        try {
                            Constructor<Function> constructor = converterClass.getConstructor();
                            Function converter = constructor.newInstance();
                            infoItem.setConverter(converter);
                        } catch (NoSuchMethodException e) {
                            logger.info("given converter class {} does not have default constructor", converterClass);
                            throw new BeanMappingException("given converter class does not have default constructor", e);
                        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                            logger.info("can not construct converter ,reason is {}", e);
                            throw new BeanMappingException("can not construct converter", e);
                        }
                    } else {
                        infoItem.setConverter(null);
                    }

                    infoItem.verify();

                    return infoItem;
                })
                .collect(toSet());
    }


    private Map<Class<? extends PropertiesSourceObject>, List<Field>> filterField(Class targetClass) {
        Field[] fields = ReflectUtils.getAllFields(targetClass, field -> field.getDeclaredAnnotation(DataMapping.class) != null);
        return Arrays.stream(fields)
                .collect(groupingBy(field ->
                        field.getDeclaredAnnotation(DataMapping.class).sourceClass()
                ));
    }

    public static BeanMappingInfo parser(Class clazz) {
        BeanMappingInfo beanMappingInfo = new BeanMappingInfo(clazz);
        beanMappingInfo.parserMappingInfo();
        return beanMappingInfo;
    }

    public Map<Class<? extends PropertiesSourceObject>, Set<MappingInfoItem>> getMappingInfos() {
        return new HashMap<>(mappingInfos);
    }

    public Class getTargetClass() {
        return targetClass;
    }
}
