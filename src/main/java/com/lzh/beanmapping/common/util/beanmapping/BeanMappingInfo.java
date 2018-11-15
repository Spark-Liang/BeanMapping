package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.annotation.DataMapping;
import com.lzh.beanmapping.common.exception.BeanMappingException;
import com.lzh.beanmapping.common.util.IntrospectorUtils;
import com.lzh.beanmapping.common.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static com.lzh.beanmapping.common.exception.BeanMappingException.ConstantMessage.CAN_NOT_FOUND_PROPERTY_ON_SOURCE_CLASS;
import static com.lzh.beanmapping.common.exception.BeanMappingException.ConstantMessage.FIELD_NOT_IS_NOT_A_PROPERTY;
import static com.lzh.beanmapping.common.exception.BeanMappingException.ConstantMessage.SYSTEM_EXCEPTION;
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

    private void parseMappingInfo() {
//        Map<Class<? extends PropertiesSourceObject>, List<Field>> fieldMap = filterField(targetClass);
//        mappingInfos
//                = fieldMap.entrySet().stream()
//                .collect(toMap(entry -> entry.getKey()
//                        , entry -> parserMappingInfos(entry.getKey(), entry.getValue())));
        Map<DataMapping,PropertyDescriptor> annotationMap = new HashMap<>();
        annotationMap.putAll(findAllAnnotationInFields(targetClass));

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

        class TempEntry{
            Class<? extends PropertiesSourceObject> sourceClass;
            MappingInfoItem infoItem;

            public Class<? extends PropertiesSourceObject> getSourceClass() {
                return sourceClass;
            }

            public MappingInfoItem getInfoItem() {
                return infoItem;
            }
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
                            Set<MappingInfoItem> infoItems = map.get(tempEntry.sourceClass);
                            if(infoItems == null){
                                infoItems = new HashSet<>();
                                map.put(tempEntry.sourceClass,infoItems);
                            }
                            infoItems.add(tempEntry.infoItem);
                        },
                        (map1,map2) ->{
                            map1.putAll(map2);
                        });
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
                        .collect(toMap(descriptor -> descriptor.getName(), e -> e));
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

    private Set<MappingInfoItem> parserMappingInfos(Class sourceClass, List<Field> fields) {
//        PropertyDescriptor[] sourceDescriptors = null, targetDescriptors = null;
//        try {
//            sourceDescriptors = Introspector.getBeanInfo(sourceClass).getPropertyDescriptors();
//        } catch (IntrospectionException e) {
//            logger.info("fail to parse beaninfo from sourceClass :{}", sourceClass);
//            throw new RuntimeException(e);
//        }
//        try {
//            targetDescriptors = Introspector.getBeanInfo(targetClass).getPropertyDescriptors();
//        } catch (IntrospectionException e) {
//            logger.info("fail to parse beaninfo from targetClass :{}", targetClass);
//            throw new RuntimeException(e);
//        }
//        Map<String, PropertyDescriptor> sourceDescriportsMap
//                = Arrays.stream(sourceDescriptors)
//                .collect(toMap(descriptor -> descriptor.getName(), e -> e));
//        Map<String, PropertyDescriptor> targetDescriportsMap
//                = Arrays.stream(targetDescriptors)
//                .collect(toMap(descriptor -> descriptor.getName(), e -> e));
//        return fields.stream()
//                .map(field -> {
//                    MappingInfoItem infoItem = new MappingInfoItem();
//
//                    String targetPropertyName = field.getName();
//                    PropertyDescriptor targetSetter = targetDescriportsMap.get(targetPropertyName);
//                    if (targetSetter == null) {
//                        logger.info("can not found setter of target property {} from source class {}", targetPropertyName, targetClass);
//                        throw new BeanMappingException("can not found setter of source property from target class");
//                    } else {
//                        infoItem.setTargetProperty(targetSetter);
//                    }
//
//                    DataMapping dataMapping = field.getDeclaredAnnotation(DataMapping.class);
//                    String sourcePropertyName = dataMapping.sourceProperty();
//                    PropertyDescriptor sourceGetter = sourceDescriportsMap.get(sourcePropertyName);
//                    if (sourceGetter == null) {
//                        logger.info("can not found getter of source property {} from source class {}", sourcePropertyName, sourceClass);
//                        throw new BeanMappingException("can not found source property from source class");
//                    } else {
//                        infoItem.setSourceProperty(sourceGetter);
//                    }
////                    infoItem.setNeedDeepCopy(dataMapping.needDeepCopy());
//
//                    Class converterClass = dataMapping.converter();
//                    if (!converterClass.equals(DataMapping.DEFAULT_CONVERTER_CLASS)) {
//                        try {
//                            Constructor<Function> constructor = converterClass.getConstructor();
//                            Function converter = constructor.newInstance();
////                            infoItem.setConverter(converter);
//                        } catch (NoSuchMethodException e) {
//                            logger.info("given converter class {} does not have default constructor", converterClass);
//                            throw new BeanMappingException("given converter class does not have default constructor", e);
//                        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
//                            logger.info("can not construct converter ,reason is {}", e);
//                            throw new BeanMappingException("can not construct converter", e);
//                        }
//                    } else {
////                        infoItem.setConverter(null);
//                    }
//
//                    infoItem.verify();
//
//                    return infoItem;
//                })
//                .collect(toSet());
        return null;
    }


    private Map<Class<? extends PropertiesSourceObject>, List<Field>> filterField(Class targetClass) {
        Field[] fields = ReflectUtils.getAllDeclaredFields(targetClass, field -> field.getDeclaredAnnotation(DataMapping.class) != null);
        return Arrays.stream(fields)
                .collect(groupingBy(field ->
                        field.getDeclaredAnnotation(DataMapping.class).sourceClass()
                ));
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
