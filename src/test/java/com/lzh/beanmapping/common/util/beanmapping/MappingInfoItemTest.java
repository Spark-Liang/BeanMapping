package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.exception.BeanMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.beans.IntrospectionException;
import java.util.Date;
import java.util.function.Function;

import static com.lzh.beanmapping.common.exception.BeanMappingException.ConstantMessage.*;
import static com.lzh.beanmapping.common.util.IntrospectorUtils.getPropertyByName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"unused","unchecked"})
public class MappingInfoItemTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MappingInfoItem SUT;

    @Before
    public void setUp() {
        SUT = spy(new MappingInfoItem());
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     */
    @Test
    public void canPassTheValidationWhenGivenMappingInfoItemIsCorrect()
            throws Exception {
        //given
        processSUTToNormalCase();

        //when
        SUT.verify();
    }

    private void processSUTToNormalCase() throws IntrospectionException {
        class TestTargetClass {
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
        String targetPropertyName = "name",
                sourcePropertyName = "sourceName";
        SUT.setTargetProperty(getPropertyByName(TestTargetClass.class, targetPropertyName));
        SUT.setSourceProperty(getPropertyByName(TestSourceClass.class, sourcePropertyName));
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     */
    @Test
    public void canPassTheValidationWhenGivenMappingInfoItemIsCorrectAndHaveConverter()
            throws Exception {
        //given
        processSUTToNormalCase();
        Class<? extends Function>[] converters = new Class[]{
                ConverterFromStringToIntegerForTest.class, ConverterFromIntegerToStringForTest.class
        };
        SUT.setToTargetConverterChain(converters);

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     */
    @Test
    public void doThrowSourceIsNullExceptionWhenGetterDescriptorIsNull()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(SOURCE_IS_NULL);
        class TestTargetClass {
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
        String targetPropertyName = "name";
        SUT.setTargetProperty(getPropertyByName(TestTargetClass.class, targetPropertyName));

        //when
        SUT.verify();

    }

    /**
     * Test {@link MappingInfoItem#verify()}
     */
    @Test
    public void doThrowGetMethodIsNullExceptionWhenSourceGetterMethodIsNull()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(GET_METHOD_IS_NULL);
        class TestTargetClass {
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
        class TestSourceClass implements PropertiesSourceObject {
            private String name;

            public void setName(String name) {
                this.name = name;
            }
        }
        String targetPropertyName = "name",
                sourcePropertyName = "name";
        SUT.setTargetProperty(getPropertyByName(TestTargetClass.class, targetPropertyName));
        SUT.setSourceProperty(getPropertyByName(TestSourceClass.class, sourcePropertyName));

        //when
        SUT.verify();

    }

    /**
     * Test {@link MappingInfoItem#verify()}
     */
    @Test
    public void doThrowSetMethodIsNullExceptionWhenSourceSetterMethodIsNull()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(SET_METHOD_IS_NULL);
        class TestTargetClass {
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
        class TestSourceClass implements PropertiesSourceObject {
            private String name;

            public String getName() {
                return name;
            }
        }
        String targetPropertyName = "name",
                sourcePropertyName = "name";
        SUT.setTargetProperty(getPropertyByName(TestTargetClass.class, targetPropertyName));
        SUT.setSourceProperty(getPropertyByName(TestSourceClass.class, sourcePropertyName));

        //when
        SUT.verify();

    }

    /**
     * Test {@link MappingInfoItem#verify()}
     */
    @Test
    public void doThrowTargetIsNullExceptionWhenSetterDescriptorIsNull()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(TARGET_IS_NULL);
        String sourcePropertyName = "sourceName";
        SUT.setSourceProperty(getPropertyByName(TestSourceClass.class, sourcePropertyName));

        //when
        SUT.verify();

    }

    /**
     * Test {@link MappingInfoItem#verify()}
     */
    @Test
    public void doThrowGetMethodIsNullExceptionWhenTargetGetterMethodIsNull()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(GET_METHOD_IS_NULL);
        class TestTargetClass {
            private String name;

            public void setName(String name) {
                this.name = name;
            }
        }
        String targetPropertyName = "name",
                sourcePropertyName = "sourceName";
        SUT.setTargetProperty(getPropertyByName(TestTargetClass.class, targetPropertyName));
        SUT.setSourceProperty(getPropertyByName(TestSourceClass.class, sourcePropertyName));

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     */
    @Test
    public void doThrowSetMethodIsNullExceptionWhenTargetSetterMethodIsNull()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(SET_METHOD_IS_NULL);
        class TestTargetClass {
            private String name;

            public String getName() {
                return name;
            }

        }
        String targetPropertyName = "name",
                sourcePropertyName = "sourceName";
        SUT.setTargetProperty(getPropertyByName(TestTargetClass.class, targetPropertyName));
        SUT.setSourceProperty(getPropertyByName(TestSourceClass.class, sourcePropertyName));

        //when
        SUT.verify();

    }

    /**
     * Test {@link MappingInfoItem#verify()}
     * assert will throw {@link BeanMappingException} when the type of source property is
     * different from the type of target property and one of the converter chain is null
     * or empty. Because in this scenario means that it will need to directly transfer
     * value between the target and source property.
     */
    @Test
    public void doThrowExceptionWhenNotHaveConverterAndNotMatchBetweenSourceAndTargetType()
    throws Exception{
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(PROPERTY_TYPE_IS_DIFFERENT);
        class TestTargetClass {
            private Integer value;

            public Integer getValue() {
                return value;
            }

            public void setValue(Integer value) {
                this.value = value;
            }
        }
        String targetPropertyName = "value",
                sourcePropertyName = "sourceName";
        SUT.setTargetProperty(getPropertyByName(TestTargetClass.class, targetPropertyName));
        SUT.setSourceProperty(getPropertyByName(TestSourceClass.class, sourcePropertyName));

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     * will throw exception when one of the converter in the Converter chain
     * do not have the default converter
     */
    @Test
    public void doThrowExceptionWhenOneOfConverterInToTargetChainNotHaveConstruct()
            throws Exception{
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(CONVERTER_NOT_HAS_DEFAULT_CONSTRUCTOR);

        processSUTToNormalCase();

        Class<? extends Function>[] converters = new Class[]{
                ConverterWithoutConstructer.class
        };
        SUT.setToTargetConverterChain(converters);

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     * will throw exception when one of the converter in the Converter chain
     * can not construct by default converter
     */
    @Test
    public void doThrowExceptionWhenOneOfConverterInToTargetChainCanNotConstruct()
            throws Exception{
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(CONVERTER_CAN_NOT_CONSTRUCT);

        processSUTToNormalCase();

        Class<? extends Function>[] converters = new Class[]{
                ConverterCanNotConstruct.class
        };
        SUT.setToTargetConverterChain(converters);

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     * will throw exception when one of the converter in the Converter chain
     * have more than one convert method names 'apply'
     */
    @Test
    public void doThrowExceptionWhenOneOfConverterInToTargetChainHasDuplicatedConverterMethod()
            throws Exception{
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(DUPLICATED_CONVERT_METHOD);

        processSUTToNormalCase();

        Class<? extends Function>[] converters = new Class[]{
                ConverterWithDuplicatedImplement.class
        };
        SUT.setToTargetConverterChain(converters);

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     * will throw exception when the Converter chain has one of the converter
     * whose input type does not match the return type of previous converter
     */
    @Test
    public void doThrowExceptionWhenOneOfConverterInToTargetChainNotMatchPreviousReturnType()
    throws Exception{
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(CONVERTER_NOT_MATCH_PREVIOUS_TYPE);

        processSUTToNormalCase();

        Class<? extends Function>[] converters = new Class[]{
                ConverterFromStringToIntegerForTest.class, ConverterFromStringToIntegerForTest.class
        };
        SUT.setToTargetConverterChain(converters);

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     * will throw exception when the return type of the converter in final of the converter chain
     * does not match the input type of the setter method
     */
    @Test
    public void doThrowExceptionWhenToTargetChainNotMatchTargetType()
    throws Exception{
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(CONVERTER_CHAIN_NOT_MATCH_RETURN_TYPE);

        processSUTToNormalCase();

        Class<? extends Function>[] converters = new Class[]{
                ConverterFromStringToIntegerForTest.class,
                ConverterFromIntegerToStringForTest.class,
                ConverterFromStringToIntegerForTest.class
        };
        SUT.setToTargetConverterChain(converters);

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     * will throw exception when the input type of the converter in first of the converter chain
     * does not match the return type of the getter method
     */
    @Test
    public void doThrowExceptionWhenToTargetChainNotMatchSourceType()
    throws Exception{
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(CONVERTER_CHAIN_NOT_MATCH_INPUT_TYPE);

        processSUTToNormalCase();

        Class<? extends Function>[] converters = new Class[]{
                ConverterFromIntegerToStringForTest.class,
                ConverterFromStringToIntegerForTest.class,
                ConverterFromStringToIntegerForTest.class
        };
        SUT.setToTargetConverterChain(converters);

        //when
        SUT.verify();
    }

    /**
     * Test {@link MappingInfoItem#verify()}
     * this test assert that the toSourceChain will do verify during the {@link MappingInfoItem#verify()} is call
     * @throws Exception
     */
    @Test
    public void shouldDoVerifyOnToSourceChain() throws Exception{
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(CONVERTER_CHAIN_NOT_MATCH_INPUT_TYPE);

        processSUTToNormalCase();

        Class<? extends Function>[] converters = new Class[]{
                ConverterFromIntegerToStringForTest.class,
                ConverterFromStringToIntegerForTest.class,
                ConverterFromStringToIntegerForTest.class
        };
        SUT.setToSourceConverterChain(converters);

        //when
        SUT.verify();

        //then
        verify(SUT,atLeastOnce()).verifyCoverterChain(converters,SUT.getTargetProperty(),SUT.getSourceProperty());
    }



    public static class TestSourceClass {
        private String sourceName;

        private Date sourceDate;

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        public Date getSourceDate() {
            return sourceDate;
        }

        public void setSourceDate(Date sourceDate) {
            this.sourceDate = sourceDate;
        }
    }

    public static class ConverterWithoutConstructer implements Function<String,String> {

        public ConverterWithoutConstructer(String value){
            value = null;
        }

        @Override
        public String apply(String o) {
            return o;
        }

    }

    public static class ConverterCanNotConstruct implements Function<String,String> {

        public ConverterCanNotConstruct(){
            throw new RuntimeException();
        }

        @Override
        public String apply(String o) {
            return o;
        }
    }

    interface FakeFunction {
        String apply(Integer o);
    }

    public static class ConverterWithDuplicatedImplement implements Function<String,Integer>,FakeFunction {

        public ConverterWithDuplicatedImplement(){}

        @Override
        public Integer apply(String o) {
            return 1;
        }

        @Override
        public String apply(Integer o) {
            return null;
        }
    }

    public static class ConverterFromStringToIntegerForTest implements Function<String,Integer> {

        public ConverterFromStringToIntegerForTest(){}

        @Override
        public Integer apply(String o) {
            return 1;
        }
    }

    public static class ConverterFromIntegerToStringForTest implements Function<Integer,String> {

        public ConverterFromIntegerToStringForTest (){}

        @Override
        public String  apply(Integer o) {
            return "";
        }
    }


}