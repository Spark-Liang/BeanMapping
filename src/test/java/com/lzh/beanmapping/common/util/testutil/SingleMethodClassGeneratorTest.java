package com.lzh.beanmapping.common.util.testutil;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.asm.Type;
import org.springframework.cglib.core.*;

public class SingleMethodClassGeneratorTest {

    private SingleMethodClassGeneratorForTest SUT;

    @Before
    public void setUp() {
        SUT = new SingleMethodClassGeneratorForTest();
    }

    @Test
    public void testGenerateRightClass() {
        //given
        String input = "1";

        //when
        TargetClass resultInstance = (TargetClass) SUT.create();
        int result = resultInstance.convert(input);

        //then
        Assertions.assertThat(result).isEqualTo(1);
    }


    @SuppressWarnings("WeakerAccess")
    public static abstract class TargetClass {

        public abstract int convert(String something);
    }
}


class SingleMethodClassGeneratorForTest extends SingleMethodClassGenerator {

    @Override
    protected Type getClassType() {
        return Type.getType(SingleMethodClassGeneratorTest.TargetClass.class);
    }

    @Override
    protected void generateConstructor(ClassEmitter classEmitter) {
        EmitUtils.null_constructor(classEmitter);
    }

    @Override
    protected void generateMethod(ClassEmitter classEmitter) {
        Type IntegerType = Type.getType(Integer.class),
                StringType = Type.getType(String.class);
        Signature methodSignature = new Signature("convert",
                Type.INT_TYPE,
                new Type[]{StringType}
        ),
                constructor_signature = new Signature(Constants.CONSTRUCTOR_NAME,
                        Type.VOID_TYPE,
                        new Type[]{StringType}),
                intValue_signature = new Signature("intValue",
                        Type.INT_TYPE,
                        Constants.TYPES_EMPTY);
        int something_arg_ind = 0;

        CodeEmitter emitter = classEmitter.begin_method(
                Constants.ACC_PUBLIC
                , methodSignature
                , Constants.TYPES_EMPTY);

        emitter.new_instance(IntegerType);
        emitter.dup();
        emitter.load_arg(something_arg_ind);
        emitter.invoke_constructor(IntegerType, constructor_signature);
        emitter.invoke_virtual(IntegerType, intValue_signature);
        emitter.return_value();

        emitter.end_method();
    }
}