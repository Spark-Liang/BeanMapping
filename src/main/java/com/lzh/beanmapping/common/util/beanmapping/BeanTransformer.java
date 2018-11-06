package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Type;
import org.springframework.cglib.core.*;

import java.lang.reflect.Modifier;
import java.util.Set;

public abstract class BeanTransformer<T, S extends PropertiesSourceObject> {
    private static final BeanTransformer.BeanTransformerKey KEY_FACTORY
            = (BeanTransformer.BeanTransformerKey) KeyFactory.create(BeanTransformer.BeanTransformerKey.class);
    private static final Type BEAN_TRANSFORMER = TypeUtils.parseType("com.lzh.beanmapping.common.util.beanmapping.BeanTransformer");
    private static final Signature GET_TARGET_INSTANCE_FROM
            = new Signature("getTargetInstanceFrom", Constants.TYPE_OBJECT
            , new Type[]{Constants.TYPE_OBJECT});
    private static final Signature MERGE_PROPERTIES
            = new Signature("mergeProperties", Constants.TYPE_OBJECT
            , new Type[]{Constants.TYPE_OBJECT, Constants.TYPE_OBJECT});



    private final Class<T> targetClass;

    private final Class<S> sourceClass;

    public BeanTransformer(Class<T> targetClass, Class<S> sourceClass) {
        this.targetClass = targetClass;
        this.sourceClass = sourceClass;
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
        //TODO
        return null;
    }


    public static class Generater<T, S extends PropertiesSourceObject> extends AbstractClassGenerator<BeanTransformer<T, S>> {
        private static final AbstractClassGenerator.Source SOURCE = new Source(BeanTransformer.class.getName());
        private Class<S> source;
        private Class<T> target;

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
            initBaseClassStructure(ce);
            buildMethod_getTargetInstanceFrom(ce);
            buildMethod_mergeProperties(ce);
            ce.end_class();
        }

        void buildMethod_mergeProperties(ClassEmitter ce) {
            CodeEmitter emitter = ce.begin_method(Constants.ACC_PUBLIC,
                    MERGE_PROPERTIES,
                    null);
            buildMethodBody_mergeProperties(emitter);
            emitter.end_method();
        }

        private void buildMethodBody_mergeProperties(CodeEmitter emitter) {
            BeanMappingInfo beanMappingInfo = BeanMappingInfo.parser(target);
            Set<MappingInfoItem> infoItems = beanMappingInfo.getMappingInfos().get(source);
            if (infoItems != null && infoItems.isEmpty()) {
                for(MappingInfoItem infoItem : infoItems){
                    buildStatementForItem(emitter,infoItem);
                }
            }else{
                emitter.load_arg(0);
            }

            emitter.return_value();
        }

        private void buildStatementForItem(CodeEmitter emitter, MappingInfoItem infoItem) {
            int target_arg = 0, source_arg = 1;
            MethodInfo read = ReflectUtils.getMethodInfo(infoItem.getSourceGetter().getReadMethod());
            MethodInfo write = ReflectUtils.getMethodInfo(infoItem.getTargetSetter().getWriteMethod());

            if(infoItem.isNeedDeepCopy()){

            }else{
                emitter.load_arg(source_arg);
                if(infoItem.getConverter() != null){
//                    emitter.
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
            emitter.store_local(newTargetInstanceLocal);
            emitter.load_this();
            emitter.load_local(newTargetInstanceLocal);
            emitter.load_arg(0);
            emitter.invoke_virtual_this(MERGE_PROPERTIES);
            emitter.return_value();
        }

        void initBaseClassStructure(ClassEmitter ce) {
            ce.begin_class(Constants.V1_2,
                    Constants.ACC_PUBLIC,
                    getClassName(),
                    BEAN_TRANSFORMER,
                    new Type[]{},
                    "<generated>");
            EmitUtils.null_constructor(ce);

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
        Object newInstance(String var1, String var2);
    }


}
