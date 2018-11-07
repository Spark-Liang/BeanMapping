package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Type;
import org.springframework.cglib.core.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

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

    private static final Signature CONVERTER_METHOD;

    static {
        Method convertMethod = Function.class.getDeclaredMethods()[0];
        CONVERTER_METHOD = new Signature(convertMethod.getName(),
                Type.getReturnType(convertMethod),
                Type.getArgumentTypes(convertMethod));
    }

    public BeanTransformer() {
    }

    public abstract T getTargetInstanceFrom(S source);

    public abstract T mergeProperties(T target, S source);


    /**
     * @param targetClass
     * @param sourceClass
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T, S extends PropertiesSourceObject> BeanTransformer newInstance(Class<T> targetClass, Class<S> sourceClass) {
        Generater<T, S> generater = new Generater<>();
        generater.setTarget(targetClass);
        generater.setSource(sourceClass);
        return generater.create();
    }


    public static class Generater<T, S extends PropertiesSourceObject> extends AbstractClassGenerator<BeanTransformer<T, S>> {
        private static final AbstractClassGenerator.Source SOURCE = new Source(BeanTransformer.class.getName());
        public static final String CONVERTER_FIELD_PREFIX = "converterOf";
        private Class<S> source;
        private Class<T> target;
        private BeanMappingInfo beanMappingInfo;


        public Generater() {
            super(SOURCE);
        }

        /**
         * override the method to generate custom class
         *
         * @param classVisitor
         * @throws Exception
         */
        @Override
        public void generateClass(ClassVisitor classVisitor) throws Exception {
            ClassEmitter ce = new ClassEmitter(classVisitor);

            Map<Function, String> converterFieldMap = initClassStructure(ce);

            initDefaultConstruct(ce, converterFieldMap);

            buildMethod_getTargetInstanceFrom(ce);

            buildMethod_mergeProperties(ce, converterFieldMap);

            ce.end_class();
        }

        private Map<Function, String> initClassStructure(ClassEmitter ce) {
            ce.begin_class(Constants.V1_8,
                    Constants.ACC_PUBLIC,
                    getClassName(),
                    BEAN_TRANSFORMER,
                    new Type[]{},
                    Constants.SOURCE_FILE);

            Set<MappingInfoItem> infoItems = beanMappingInfo.getMappingInfos().get(source);
            Map<Function, String> fieldConverterMap = new HashMap<>();
            infoItems.stream()
                    .filter(infoItem -> infoItem.getConverter() != null)
                    .forEach(infoItem -> {
                        String converterFieldName = getConverterFieldName(infoItem);
                        ce.declare_field(
                                Constants.ACC_PRIVATE,
                                converterFieldName,
                                Type.getType(infoItem.getConverter().getClass()),
                                null
                        );
                        fieldConverterMap.put(infoItem.getConverter(), converterFieldName);
                    });
            return fieldConverterMap;
        }

        private String getConverterFieldName(MappingInfoItem infoItem) {
            return CONVERTER_FIELD_PREFIX + "_From_" + infoItem.getSourceGetter().getName() + "_To_" + infoItem.getSourceGetter().getName();
        }

        void buildMethod_mergeProperties(ClassEmitter ce, Map<Function, String> converterFieldMap) {
            CodeEmitter emitter = ce.begin_method(Constants.ACC_PUBLIC,
                    MERGE_PROPERTIES,
                    null);
            buildMethodBody_mergeProperties(emitter, converterFieldMap);
            emitter.end_method();
        }

        private void buildMethodBody_mergeProperties(CodeEmitter emitter, Map<Function, String> converterFieldMap) {
            Set<MappingInfoItem> infoItems = beanMappingInfo.getMappingInfos().get(source);

            if (infoItems != null && !infoItems.isEmpty()) {
                int target_arg = 0, source_arg = 1;
                Type targetType = Type.getType(target),
                        sourceType = Type.getType(source);
                Local targetLocal = emitter.make_local(targetType),
                        sourceLocal = emitter.make_local(sourceType);

                emitter.load_arg(target_arg);
                emitter.checkcast(targetType);
                emitter.store_local(targetLocal);

                emitter.load_arg(source_arg);
                emitter.checkcast(sourceType);
                emitter.store_local(sourceLocal);

                for (MappingInfoItem infoItem : infoItems) {
                    buildStatementForItem(emitter, infoItem, targetLocal, sourceLocal, converterFieldMap);
                }
            }

            emitter.load_arg(0);
            emitter.return_value();
        }

        private void buildStatementForItem(CodeEmitter emitter
                , MappingInfoItem infoItem
                , Local targetLocal
                , Local sourceLocal
                , Map<Function, String> converterFieldMap) {
            MethodInfo read = ReflectUtils.getMethodInfo(infoItem.getSourceGetter().getReadMethod());
            MethodInfo write = ReflectUtils.getMethodInfo(infoItem.getTargetSetter().getWriteMethod());

            if (infoItem.isNeedDeepCopy()) {

            } else {
                if (infoItem.getConverter() != null) {
                    Function converter = infoItem.getConverter();
                    String coverterFieldName = converterFieldMap.get(converter);

                    emitter.load_local(targetLocal);
                    emitter.load_this();
                    emitter.getfield(coverterFieldName);
                    emitter.load_local(sourceLocal);
                    emitter.invoke(read);
                    emitter.invoke_interface(Type.getType(Function.class), CONVERTER_METHOD);
                    emitter.checkcast(Type.getType(infoItem.getTargetSetter().getPropertyType()));
                    emitter.invoke(write);
                } else {
                    emitter.load_local(targetLocal);
                    emitter.load_local(sourceLocal);
                    emitter.invoke(read);
                    emitter.invoke(write);
                }
            }
        }


        void buildMethod_getTargetInstanceFrom(ClassEmitter ce) {
            CodeEmitter codeEmitter = ce.begin_method(Constants.ACC_PUBLIC,
                    GET_TARGET_INSTANCE_FROM,
                    null);
            buildMethodBody_getTargetInstanceFrom(codeEmitter);
            codeEmitter.end_method();
        }

        void buildMethodBody_getTargetInstanceFrom(CodeEmitter emitter) {
            Type targetType = Type.getType(target);
            Local newTargetInstanceLocal = emitter.make_local(targetType);

            emitter.new_instance(targetType);
            emitter.dup();
            emitter.invoke_constructor(targetType);
            emitter.store_local(newTargetInstanceLocal);
            emitter.load_this();
            emitter.load_local(newTargetInstanceLocal);
            emitter.load_arg(0);
            emitter.invoke_virtual_this(MERGE_PROPERTIES);
            emitter.return_value();
        }

        void initDefaultConstruct(ClassEmitter ce, Map<Function, String> converterFieldMap) {
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

            converterFieldMap.entrySet().stream()
                    .forEach(entry -> {
                        Function converter = entry.getKey();
                        String converterFieldName = entry.getValue();
                        Type converterType = Type.getType(converter.getClass());

                        codeEmitter.load_this();
                        codeEmitter.new_instance(converterType);
                        codeEmitter.dup();
                        codeEmitter.invoke_constructor(converterType);
                        codeEmitter.putfield(converterFieldName);
                    });

            codeEmitter.return_value();
            codeEmitter.end_method();
        }

        /**
         * create an instance of {@link BeanTransformer}
         *
         * @return instance of {@link BeanTransformer}
         */
        public BeanTransformer<T, S> create() {
            Object key = KEY_FACTORY.newInstance(target.getName(), source.getName());
            return (BeanTransformer<T, S>) super.create(key);
        }

        public void setSource(Class source) {
            if (!Modifier.isPublic(source.getModifiers())) {
                this.setNamePrefix(source.getName());
            }
            this.source = source;
        }

        public void setTarget(Class target) {
            if (!Modifier.isPublic(target.getModifiers())) {
                this.setNamePrefix(target.getName());
            }
            this.target = target;
            beanMappingInfo = BeanMappingInfo.parser(target);
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return this.source.getClassLoader();
        }

        @Override
        protected Object firstInstance(Class type) throws Exception {
            return ReflectUtils.newInstance(type);
        }

        @Override
        protected Object nextInstance(Object instance) throws Exception {
            return instance;
        }


    }

    interface BeanTransformerKey {
        Object newInstance(String targetClassName, String sourceClassName);
    }


}
