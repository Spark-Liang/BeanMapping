package com.lzh.beanmapping.core;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.util.ArrayUtils;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Type;
import org.springframework.cglib.core.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * This class define the signature about the bean transform method, and also define the class generator the generate the implement class automatically.<br>
 * For the record, we declare a direction of the BeanTransforming action.
 * We define the direction from the class which implements {@link PropertiesSourceObject} to other class is the positive direction, and reverse is the negative direction
 *
 * @param <T> define the target type of the positive transform direction
 * @param <S> define the source type of the positive transform direction
 */
public abstract class BeanTransformer<T, S extends PropertiesSourceObject> {
    private static final BeanTransformer.BeanTransformerKey KEY_FACTORY
            = (BeanTransformer.BeanTransformerKey) KeyFactory.create(BeanTransformer.BeanTransformerKey.class);
    private static final Type BEAN_TRANSFORMER = Type.getType(BeanTransformer.class);

    private static final Signature GET_TARGET_INSTANCE_FROM
            = new Signature("getTargetInstanceFrom", Constants.TYPE_OBJECT
            , new Type[]{Type.getType(PropertiesSourceObject.class)});
    private static final Signature MERGE_PROPERTIES
            = new Signature("mergeProperties", Constants.TYPE_OBJECT
            , new Type[]{Constants.TYPE_OBJECT, Type.getType(PropertiesSourceObject.class)});
    private static final Signature GET_SOURCE_INSTANCE_FROM
            = new Signature("getSourceInstanceFrom", Type.getType(PropertiesSourceObject.class),
            new Type[]{Constants.TYPE_OBJECT});
    private static final Signature MERGE_PROPERTIES_TO_SOURCE
            = new Signature("mergePropertiesToSource", Type.getType(PropertiesSourceObject.class),
            new Type[]{Type.getType(PropertiesSourceObject.class), Constants.TYPE_OBJECT});


    /**
     * new an target instance by the default constructor of the target class ,and copy property from the source object
     *
     * @param source instance of source class
     * @return target object
     */
    @SuppressWarnings("WeakerAccess")
    public abstract T getTargetInstanceFrom(S source);

    /**
     * copy corresponding property from source object to target object
     *
     * @param target instance of target class
     * @param source instance of source class
     * @return target object
     */
    @SuppressWarnings("WeakerAccess")
    public abstract T mergeProperties(T target, S source);

    /**
     * new an source instance by the default constructor of the target class ,and copy property from the target object
     *
     * @param target instance of target class
     * @return source object
     */
    @SuppressWarnings("WeakerAccess")
    public abstract S getSourceInstanceFrom(T target);

    /**
     * copy corresponding property from target object to source object
     *
     * @param source instance of source class
     * @param target instance of target class
     * @return source object
     */
    @SuppressWarnings("WeakerAccess")
    public abstract S mergePropertiesToSource(S source, T target);

    /**
     * use this method provide the access permission to invoke {@link ConverterChain#convert(Object)}
     *
     * @param converterChain the converterChain to do convert
     * @param source         the value to be convert
     * @param <T>            type of the input value
     * @param <R>            type of the return value
     * @return value after convert
     */
    @SuppressWarnings({"unused", "TypeParameterHidesVisibleType", "WeakerAccess"})
    protected <T, R> R convertByChain(ConverterChain<T, R> converterChain, T source) {
        return converterChain.convert(source);
    }

    /**
     * use this method provide the access permission to new the instance of {@link ConverterChain}
     *
     * @param converterChain all the component instance of the converterChain
     * @return instance of converterChain
     */
    @SuppressWarnings("unused")
    protected ConverterChain getConverterChainInstance(Function[] converterChain) {
        return new ConverterChain(converterChain);
    }

    /**
     * get the implement instance of the {@link BeanTransformer}
     * @param targetClass the target type of the positive transform direction
     * @param sourceClass the source type of the positive transform direction
     * @param <T> target class type
     * @param <S> source class type
     * @return instance of {@link BeanTransformer}
     */
    @SuppressWarnings("WeakerAccess")
    public static <T, S extends PropertiesSourceObject> BeanTransformer newInstance(Class<T> targetClass, Class<S> sourceClass) {
        BeanMappingInfo mappingInfo = BeanMappingInfo.parse(targetClass);
        Generator<T, S> generater = new Generator<>(sourceClass, targetClass, mappingInfo);
        return generater.create();
    }


    private static class Generator<T, S extends PropertiesSourceObject> extends AbstractClassGenerator<BeanTransformer<T, S>> {
        private static final AbstractClassGenerator.Source SOURCE = new Source(BeanTransformer.class.getName());
        private static final String CHAIN_FIELD_PREFIX = "converterChainOf";

        private static final Type CONVERTER_CHAIN_TYPE = Type.getType(ConverterChain.class);
        private static final Signature CONVERT_BY_CHAIN
                = new Signature("convertByChain", Constants.TYPE_OBJECT,
                new Type[]{Type.getType(ConverterChain.class), Constants.TYPE_OBJECT});
        private static final Signature GET_CONVERT_CHAIN_INSTANCE
                = new Signature("getConverterChainInstance", CONVERTER_CHAIN_TYPE,
                new Type[]{Type.getType(Function[].class)});

        private final Class<S> source;
        private final Class<T> target;
        private final Set<MappingInfoItem> infoItems;

        private Generator(Class<S> source, Class<T> target, BeanMappingInfo beanMappingInfo) {
            super(SOURCE);
            this.source = source;
            this.target = target;
            if (!Modifier.isPublic(source.getModifiers())) {
                this.setNamePrefix(source.getName());
            }
            if (!Modifier.isPublic(target.getModifiers())) {
                this.setNamePrefix(target.getName());
            }
            this.infoItems = beanMappingInfo.getMappingInfos().get(source);
        }

        /**
         * override the method to generate custom class
         *
         * @param classVisitor util to build bytecode
         */
        @Override
        public void generateClass(ClassVisitor classVisitor) {
            ClassEmitter ce = new ClassEmitter(classVisitor);

            Map<Class<? extends Function>[], String> converterFieldNameMap = initClassStructure(ce);

            initDefaultConstruct(ce, converterFieldNameMap);

            buildMethod_getTargetInstanceFrom(ce);

            buildMethod_mergeProperties(ce, converterFieldNameMap);

            buildMethod_getSourceInstanceFrom(ce);

            buildMethod_mergePropertiesToSource(ce, converterFieldNameMap);

            ce.end_class();
        }

        /**
         * using {@link ClassEmitter} to construct the class structure including :<br>
         * <ul>
         * <li>build the signature of the class</li>
         * <li>declare the field for each converterChain which is not null in the annotation</li>
         * </ul>
         *
         * @param ce the util to build bytecode
         * @return the mapping between converterChain and the fieldName
         */
        private Map<Class<? extends Function>[], String> initClassStructure(ClassEmitter ce) {
            ce.begin_class(Constants.V1_8,
                    Constants.ACC_PUBLIC,
                    getClassName(),
                    BEAN_TRANSFORMER,
                    new Type[]{},
                    Constants.SOURCE_FILE);

            class TempEntry {
                private Class<? extends Function>[] converterChain;
                private String fieldName;
            }

            return infoItems.stream()
                    .flatMap(infoItem -> {
                        TempEntry tempEntryForToSourceChain = null,
                                tempEntryForToTargetChain = null;

                        Class<? extends Function>[] toSourceChain = infoItem.getToSourceConverterChain();
                        if (ArrayUtils.isNotEmpty(toSourceChain)) {
                            tempEntryForToSourceChain = new TempEntry();
                            tempEntryForToSourceChain.converterChain = toSourceChain;
                            tempEntryForToSourceChain.fieldName = getToSourceChainFieldName(infoItem);
                        }

                        Class<? extends Function>[] toTargetChain = infoItem.getToTargetConverterChain();
                        if (ArrayUtils.isNotEmpty(toTargetChain)) {
                            tempEntryForToTargetChain = new TempEntry();
                            tempEntryForToTargetChain.converterChain = toTargetChain;
                            tempEntryForToTargetChain.fieldName = getToTargetChainFieldName(infoItem);
                        }

                        return Stream.of(tempEntryForToSourceChain, tempEntryForToTargetChain);
                    })
                    .filter(Objects::nonNull)
                    .peek(tempEntry ->
                            ce.declare_field(Constants.ACC_PRIVATE,
                                    tempEntry.fieldName,
                                    CONVERTER_CHAIN_TYPE,
                                    null)
                    )
                    .collect(toMap(tempEntry -> tempEntry.converterChain,
                            tempEntry -> tempEntry.fieldName));
        }

        private String getToTargetChainFieldName(MappingInfoItem infoItem) {
            return CHAIN_FIELD_PREFIX + "_From_" + infoItem.getSourceProperty().getName() + "_To_" + infoItem.getTargetProperty().getName();
        }

        private String getToSourceChainFieldName(MappingInfoItem infoItem) {
            return CHAIN_FIELD_PREFIX + "_From_" + infoItem.getTargetProperty().getName() + "_To_" + infoItem.getSourceProperty().getName();
        }

        /**
         * using {@link ClassEmitter} to build the byte code of the default constructor, which including the steo list as below : <br>
         * <ul>
         * <li>invoke the super class constructor</li>
         * <li>construct the instance and set to the corresponding field for each convertChain</li>
         * </ul>
         *
         * @param ce                         the util to build bytecode
         * @param converterChainFieldNameMap the mapping between converterChain and the fieldName
         */
        private void initDefaultConstruct(ClassEmitter ce, Map<Class<? extends Function>[], String> converterChainFieldNameMap) {
            CodeEmitter codeEmitter =
                    ce.begin_method(
                            Constants.ACC_PUBLIC,
                            new Signature(Constants.CONSTRUCTOR_NAME,
                                    Type.VOID_TYPE,
                                    Constants.TYPES_EMPTY
                            ),
                            Constants.TYPES_EMPTY
                    );
            codeEmitter.load_this();
            codeEmitter.super_invoke_constructor();

            converterChainFieldNameMap
                    .forEach((converterChain, converterFieldName) -> constructChainAndSetToField(codeEmitter, converterChain, converterFieldName));

            codeEmitter.return_value();
            codeEmitter.end_method();
        }

        private void constructChainAndSetToField(CodeEmitter codeEmitter, Class<? extends Function>[] converterChain, String converterFieldName) {
            Local convertArrayLocal = codeEmitter.make_local(Type.getType(Function[].class));
            codeEmitter.push(converterChain.length);
            codeEmitter.newarray(Type.getType(Function.class));
            codeEmitter.store_local(convertArrayLocal);

            for (int ind = 0, maxInd = converterChain.length; ind < maxInd; ind++) {
                Class<? extends Function> converterClass = converterChain[ind];
                Type converterType = Type.getType(converterClass);

                codeEmitter.load_local(convertArrayLocal);
                codeEmitter.push(ind);
                codeEmitter.new_instance(converterType);
                codeEmitter.dup();
                codeEmitter.invoke_constructor(converterType);
                codeEmitter.aastore();
            }

            codeEmitter.load_this();
            codeEmitter.dup();
            codeEmitter.load_local(convertArrayLocal);
            codeEmitter.invoke_virtual_this(GET_CONVERT_CHAIN_INSTANCE);
            codeEmitter.putfield(converterFieldName);
        }

        /**
         * using {@link ClassEmitter} to build the byte code of the method {@link BeanTransformer#getTargetInstanceFrom(PropertiesSourceObject)}, which including the steps listed as below : <br>
         * <ul>
         * <li>invoke the default constructor of the target class</li>
         * <li>invoke {@link BeanTransformer#mergeProperties(Object, PropertiesSourceObject)} to merge the property from source object to target object</li>
         * </ul>
         *
         * @param ce the util to build bytecode
         */
        private void buildMethod_getTargetInstanceFrom(ClassEmitter ce) {
            CodeEmitter codeEmitter = ce.begin_method(Constants.ACC_PUBLIC,
                    GET_TARGET_INSTANCE_FROM,
                    null);
            buildMethodBodyOfGetInstanceFrom(codeEmitter, target, MERGE_PROPERTIES);
            codeEmitter.end_method();
        }

        /**
         * build the body of {@link BeanTransformer#getSourceInstanceFrom(Object)} and {@link BeanTransformer#getTargetInstanceFrom(PropertiesSourceObject)}
         * the steps of this method is :<br>
         * <ul>
         * <li>invoke the target type default constructor</li>
         * <li>invoke the corresponding property merge method</li>
         * </ul>
         *
         * @param emitter               util to build bytecode instructions
         * @param newInstanceClass      the class of the new instance
         * @param propertiesMergeMethod the signature of the property merge method
         */
        private void buildMethodBodyOfGetInstanceFrom(CodeEmitter emitter
                , Class newInstanceClass
                , Signature propertiesMergeMethod) {
            Type newInstanceType = Type.getType(newInstanceClass);
            Local newInstanceLocal = emitter.make_local(newInstanceType);
            emitter.new_instance(newInstanceType);
            emitter.dup();
            emitter.invoke_constructor(newInstanceType);
            emitter.store_local(newInstanceLocal);
            emitter.load_this();
            emitter.load_local(newInstanceLocal);
            emitter.load_arg(0);
            emitter.invoke_virtual_this(propertiesMergeMethod);
            emitter.return_value();
        }

        /**
         * using {@link ClassEmitter} to build the byte code of the method {@link BeanTransformer#mergeProperties(Object, PropertiesSourceObject)},
         * foreach {@link MappingInfoItem} this method will do : <br>
         * <ul>
         * <li>build the local variable for the args,because the signature of the args actually is Object, so need to place in the local variable to provide the type signature</li>
         * <li>invoke the get method of the source instance</li>
         * <li>if this {@link MappingInfoItem#toTargetConverterChain} is not null than invoke {@link BeanTransformer#convertByChain(ConverterChain, Object)} to do convert on the property value</li>
         * <li>do check cast on the value after converted and invoke the set method to set value into target instance</li>
         * </ul>
         *
         * @param ce                         the util to build bytecode
         * @param converterChainFieldNameMap the mapping between converterChain and the fieldName
         */
        private void buildMethod_mergeProperties(ClassEmitter ce
                , Map<Class<? extends Function>[], String> converterChainFieldNameMap) {
            CodeEmitter emitter = ce.begin_method(Constants.ACC_PUBLIC,
                    MERGE_PROPERTIES,
                    null);
            buildMethodBodyOfMergeProperties(emitter
                    , converterChainFieldNameMap
                    , source
                    , target
                    , MappingInfoItem::getSourceProperty
                    , MappingInfoItem::getTargetProperty
                    , MappingInfoItem::getToTargetConverterChain);
            emitter.end_method();
        }


        /**
         * build the method for method {@link BeanTransformer#mergeProperties(Object, PropertiesSourceObject)} and {@link BeanTransformer#mergePropertiesToSource(PropertiesSourceObject, Object)}
         *
         * @param emitter                    util to build bytecode instructions
         * @param converterChainFieldNameMap the mapping between the converterChain and the fieldName
         * @param fromClass                  define which class for the instance to provide the property
         * @param toClass                    define which class for the instance to consume the property
         * @param fromPropertyExtractor      define the way to get the descriptor of the property value provider
         * @param toPropertyExtractor        define the way to get the descriptor of the property value consumer
         * @param converterChainExtractor    define the way to get the converterChain
         */
        private void buildMethodBodyOfMergeProperties(CodeEmitter emitter
                , Map<Class<? extends Function>[], String> converterChainFieldNameMap
                , Class fromClass
                , Class toClass
                , Function<MappingInfoItem, PropertyDescriptor> fromPropertyExtractor
                , Function<MappingInfoItem, PropertyDescriptor> toPropertyExtractor
                , Function<MappingInfoItem, Class<? extends Function>[]> converterChainExtractor) {

            if (infoItems != null && !infoItems.isEmpty()) {
                int instanceTo_arg = 0, instanceFrom_arg = 1;
                Type toType = Type.getType(toClass),
                        fromType = Type.getType(fromClass);
                Local toInstanceLocal = emitter.make_local(toType),
                        fromInstanceLocal = emitter.make_local(fromType);

                emitter.load_arg(instanceTo_arg);
                emitter.checkcast(toType);
                emitter.store_local(toInstanceLocal);

                emitter.load_arg(instanceFrom_arg);
                emitter.checkcast(fromType);
                emitter.store_local(fromInstanceLocal);

                for (MappingInfoItem infoItem : infoItems) {
                    Class<? extends Function>[] converterChain = converterChainExtractor.apply(infoItem);
                    String converterChainField = converterChain != null ? converterChainFieldNameMap.get(converterChain) : null;
                    buildStatementForItem(emitter,
                            fromPropertyExtractor.apply(infoItem),
                            toPropertyExtractor.apply(infoItem),
                            fromInstanceLocal,
                            toInstanceLocal,
                            converterChainField);
                }
            }

            emitter.load_arg(0);
            emitter.return_value();
        }

        /**
         * build the transform statement
         *
         * @param emitter                 uitl to build bytecode instructions
         * @param fromProperty            get value from this property
         * @param toProperty              set value to this property
         * @param fromLocal               local variable which store the instance to provide property value
         * @param toLocal                 local variable which store the instance to be set property value
         * @param converterChainFieldName if not null means that need to do value converting by the converter chain in this field
         */
        private void buildStatementForItem(CodeEmitter emitter
                , PropertyDescriptor fromProperty
                , PropertyDescriptor toProperty
                , Local fromLocal
                , Local toLocal
                , String converterChainFieldName) {
            MethodInfo read = ReflectUtils.getMethodInfo(fromProperty.getReadMethod());
            MethodInfo write = ReflectUtils.getMethodInfo(toProperty.getWriteMethod());

            if (converterChainFieldName != null) {

                emitter.load_local(toLocal);
                emitter.load_this();
                emitter.dup();
                emitter.getfield(converterChainFieldName);
                emitter.load_local(fromLocal);
                emitter.invoke(read);
                emitter.invoke_virtual_this(CONVERT_BY_CHAIN);
                emitter.checkcast(Type.getType(toProperty.getPropertyType()));
                emitter.invoke(write);
            } else {
                emitter.load_local(toLocal);
                emitter.load_local(fromLocal);
                emitter.invoke(read);
                emitter.invoke(write);
            }

        }

        private void buildMethod_getSourceInstanceFrom(ClassEmitter ce) {
            CodeEmitter emitter = ce.begin_method(Constants.ACC_PUBLIC,
                    GET_SOURCE_INSTANCE_FROM,
                    null);

            buildMethodBodyOfGetInstanceFrom(emitter, source, MERGE_PROPERTIES_TO_SOURCE);
            emitter.end_method();
        }

        /**
         * using {@link ClassEmitter} to build the byte code of the method {@link BeanTransformer#getSourceInstanceFrom(Object)}, which including the steps listed as below : <br>
         * <ul>
         * <li>invoke the default constructor of the source class</li>
         * <li>invoke {@link BeanTransformer#mergePropertiesToSource(PropertiesSourceObject, Object)} to merge the property from source object to source object</li>
         * </ul>
         *
         * @param ce the util to build bytecode
         */
        private void buildMethod_mergePropertiesToSource(ClassEmitter ce
                , Map<Class<? extends Function>[], String> converterChainFieldNameMap) {

            CodeEmitter codeEmitter = ce.begin_method(Constants.ACC_PUBLIC,
                    MERGE_PROPERTIES_TO_SOURCE,
                    null);
            buildMethodBodyOfMergeProperties(codeEmitter
                    , converterChainFieldNameMap
                    , target
                    , source
                    , MappingInfoItem::getTargetProperty
                    , MappingInfoItem::getSourceProperty
                    , MappingInfoItem::getToSourceConverterChain);

            codeEmitter.end_method();
        }

        /**
         * create an instance of {@link BeanTransformer}
         *
         * @return instance of {@link BeanTransformer}
         */
        @SuppressWarnings("unchecked")
        private BeanTransformer<T, S> create() {
            Object key = KEY_FACTORY.newInstance(target.getName(), source.getName());
            return (BeanTransformer<T, S>) super.create(key);
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return this.source.getClassLoader();
        }

        @Override
        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type);
        }

        @Override
        protected Object nextInstance(Object instance) {
            return instance;
        }

    }

    interface BeanTransformerKey {
        Object newInstance(String targetClassName, String sourceClassName);
    }


}
