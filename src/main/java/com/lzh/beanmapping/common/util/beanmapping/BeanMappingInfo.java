package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.annotation.DataMapping;
import com.lzh.beanmapping.common.exception.BeanMappingException;
import com.lzh.beanmapping.common.util.IntrospectorUtils;
import com.lzh.beanmapping.common.util.ReflectUtils;
import com.lzh.beanmapping.common.util.Sets;
import org.springframework.util.CollectionUtils;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

import static com.lzh.beanmapping.common.exception.BeanMappingException.ConstantMessage.*;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * This class is used to describe Mapping info of a POJO
 */
@SuppressWarnings("WeakerAccess")
public class BeanMappingInfo {

    private Map<Class<? extends PropertiesSourceObject>, Set<MappingInfoItem>> mappingInfos;
    private final Class targetClass;

    private BeanMappingInfo(Class targetClass) {
        this.targetClass = targetClass;
    }

    private void parseMappingInfo() {
        Map<DataMapping, PropertyDescriptor> annotationMap = findAllAnnotationInFields(targetClass);

        Map<DataMapping, PropertyDescriptor> annotationMapInMethod = findAllAnnotationInMethods(targetClass);
        Set<PropertyDescriptor> commonDescriptor
                = Sets.intersection(Sets.toSet(annotationMap.values()),
                Sets.toSet(annotationMapInMethod.values()));
        if (!CollectionUtils.isEmpty(commonDescriptor)) {
            throw new BeanMappingException(DUPLICATE_DEFINE_ON_SAME_PROPERTY);
        }
        annotationMap.putAll(annotationMapInMethod);

        Map<Class<? extends PropertiesSourceObject>,Map<String,PropertyDescriptor>> sourcePropertyCache
                = annotationMap.entrySet().stream()
                    .map(entry -> entry.getKey().sourceClass())
                    .collect(toSet())
                    .stream()
                    .collect(toMap(e -> e,
                            clazz -> {
                                try {
                                    return Arrays.stream(IntrospectorUtils.getAllPropertyDescriptors(clazz))
                                            .collect(toMap(PropertyDescriptor::getName, e -> e));
                                } catch (IntrospectionException e) {
                                    throw new BeanMappingException(SYSTEM_EXCEPTION,e);
                                }
                            }));

        @SuppressWarnings("WeakerAccess")
        class TempEntry{
            Class<? extends PropertiesSourceObject> sourceClass;
            MappingInfoItem infoItem;
        }
        mappingInfos = annotationMap.entrySet().stream()
                .map(entry -> {
                    TempEntry tempEntry = new TempEntry();
                    DataMapping dataMapping = entry.getKey();
                    tempEntry.sourceClass = dataMapping.sourceClass();
                    tempEntry.infoItem = getMappingInfoItem(dataMapping,entry.getValue(),sourcePropertyCache);
                    return tempEntry;
                })
                .collect(HashMap::new,
                        (map,tempEntry) -> {
                            Set<MappingInfoItem> infoItems
                                    = map.computeIfAbsent(tempEntry.sourceClass, k -> new HashSet<>());
                            infoItems.add(tempEntry.infoItem);
                        },
                        HashMap::putAll);
    }

    private Map<DataMapping, PropertyDescriptor> findAllAnnotationInMethods(Class targetClass) {
        try {
            PropertyDescriptor[] propertyDescriptors = IntrospectorUtils.getAllPropertyDescriptors(targetClass);

            @SuppressWarnings("WeakerAccess")
            class TempEntry {
                DataMapping annotation;
                PropertyDescriptor descriptor;
            }

            return Arrays.stream(propertyDescriptors)
                    .map(propertyDescriptor -> {
                        Method readMethod = propertyDescriptor.getReadMethod(),
                                writeMethod = propertyDescriptor.getWriteMethod();
                        DataMapping annotationInReadMethod = readMethod != null ? readMethod.getDeclaredAnnotation(DataMapping.class) : null,
                                annotationInWriteMethod = writeMethod != null ? writeMethod.getDeclaredAnnotation(DataMapping.class) : null;
                        if (annotationInReadMethod != null && annotationInWriteMethod != null) {
                            throw new BeanMappingException(DUPLICATE_DEFINE_ON_SAME_PROPERTY);
                        } else if (annotationInReadMethod == null && annotationInWriteMethod == null) {
                            return null;
                        }
                        TempEntry tempEntry = new TempEntry();
                        if (annotationInReadMethod != null) {
                            tempEntry.annotation = annotationInReadMethod;
                        } else {
                            tempEntry.annotation = annotationInWriteMethod;
                        }
                        tempEntry.descriptor = propertyDescriptor;
                        return tempEntry;
                    })
                    .filter(Objects::nonNull)
                    .collect(
                            toMap(tempEntry -> tempEntry.annotation,
                                    tempEntry -> tempEntry.descriptor));
        } catch (IntrospectionException e) {
            throw new BeanMappingException(SYSTEM_EXCEPTION +
                    "; reason is " + e, e);
        }
    }

    private MappingInfoItem getMappingInfoItem(DataMapping dataMapping
            , PropertyDescriptor targetProperty
            , Map<Class<? extends PropertiesSourceObject>, Map<String, PropertyDescriptor>> sourcePropertyCache) {
        MappingInfoItem infoItem = new MappingInfoItem();

        infoItem.setTargetProperty(targetProperty);

        Class<? extends PropertiesSourceObject> sourceClass = dataMapping.sourceClass();
        Map<String,PropertyDescriptor> descriptorMap = sourcePropertyCache.get(sourceClass);
        if(descriptorMap != null){
            PropertyDescriptor sourceProperty = descriptorMap.get(dataMapping.sourceProperty());
            if(sourceProperty != null){
                infoItem.setSourceProperty(sourceProperty);
            }else{
                throw new BeanMappingException(CAN_NOT_FOUND_PROPERTY_ON_SOURCE_CLASS +
                        "; source class is " + sourceClass +
                        " source property is " + dataMapping.sourceProperty());
            }
        }else {
            throw new BeanMappingException(CAN_NOT_FOUND_PROPERTY_ON_SOURCE_CLASS +
                    "; source class is " + sourceClass);
        }

        infoItem.setToTargetConverterChain(dataMapping.toTargetConverterChain());

        infoItem.setToSourceConverterChain(dataMapping.toSourceConverterChain());

        infoItem.verify();

        return infoItem;
    }

    private Map<DataMapping,PropertyDescriptor> findAllAnnotationInFields(Class targetClass) {
        Predicate<Field> predicate =
                field -> null != field.getDeclaredAnnotation(DataMapping.class);
        Field[] fields = ReflectUtils.getAllDeclaredFields(targetClass, predicate);
        try {
            Map<String, PropertyDescriptor> descriptorMap
                    = Arrays.stream(IntrospectorUtils.getAllPropertyDescriptors(targetClass))
                    .collect(toMap(FeatureDescriptor::getName, e -> e));
            return Arrays.stream(fields)
                    .peek(field -> {
                        if(!descriptorMap.containsKey(field.getName())){
                            throw new BeanMappingException(FIELD_NOT_IS_NOT_A_PROPERTY +
                                    "; field is " + field);
                        }
                    })
                    .collect(toMap(field -> field.getDeclaredAnnotation(DataMapping.class)
                            ,field -> descriptorMap.get(field.getName())));
        } catch (IntrospectionException e) {
            throw new BeanMappingException(SYSTEM_EXCEPTION +
                    "; reason is " + e, e);
        }
    }


    public static BeanMappingInfo parse(Class clazz) {
        BeanMappingInfo beanMappingInfo = new BeanMappingInfo(clazz);
        beanMappingInfo.parseMappingInfo();
        return beanMappingInfo;
    }

    public Map<Class<? extends PropertiesSourceObject>, Set<MappingInfoItem>> getMappingInfos() {
        return new HashMap<>(mappingInfos);
    }

    public Class getTargetClass() {
        return targetClass;
    }
}
