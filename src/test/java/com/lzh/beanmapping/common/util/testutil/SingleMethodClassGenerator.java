package com.lzh.beanmapping.common.util.testutil;

import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Type;
import org.springframework.cglib.core.*;
import org.springframework.cglib.core.ReflectUtils;

import java.lang.reflect.Modifier;

public abstract class SingleMethodClassGenerator extends AbstractClassGenerator {
    private static final Source SOURCE = new Source(SingleMethodClassGenerator.class.getName());
    private static final GeneratorKey KEY_FACTORY = (GeneratorKey) KeyFactory.create(GeneratorKey.class);
    private final Type targetClassType;

    protected SingleMethodClassGenerator(){
        super(SOURCE);
        targetClassType = getClassType();
        Class targetClass = targetClassType.getClass();
        if (!Modifier.isPublic(targetClass.getModifiers())) {
            this.setNamePrefix(targetClass.getName());
        }
    }

    @Override
    protected ClassLoader getDefaultClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    protected Object firstInstance(Class type) throws Exception {
        return ReflectUtils.newInstance(type);
    }

    @Override
    protected Object nextInstance(Object instance) throws Exception {
        return instance;
    }

    @Override
    public void generateClass(ClassVisitor classVisitor) throws Exception {
        ClassEmitter ce = new ClassEmitter(classVisitor);

        ce.begin_class(Constants.V1_8,
                Constants.ACC_PUBLIC,
                getClassName(),
                targetClassType,
                Constants.TYPES_EMPTY,
                Constants.SOURCE_FILE);

        generateConstructor(ce);

        generateMethod(ce);

        ce.end_class();
    }

    interface GeneratorKey {
        Object newInstance(String classname);
    }

    public final Object create(){
        Object key = KEY_FACTORY.newInstance(targetClassType.getClassName());
        return super.create(key);
    }

    protected abstract Type getClassType();

    protected abstract void generateConstructor(ClassEmitter classEmitter);

    protected abstract void generateMethod(ClassEmitter classEmitter);


}
