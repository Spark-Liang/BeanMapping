package org.nanospark.beanmapping.core;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.nanospark.beanmapping.common.PropertiesSourceObject;
import org.nanospark.beanmapping.common.exception.BeanMappingException;
import org.nanospark.beanmapping.common.util.IntrospectorUtils;
import org.nanospark.beanmapping.core.annotation.DataMapping;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MappingInfoItem.class})
@SuppressWarnings("unused")
public class TestBeanMappingInfo {
    private static final String PARSE_METHOD = "parseMappingInfo";
    private static final String SOURCE_NAME = "sourceName";
    private static final String SOURCE_DATE = "sourceDate";

    private BeanMappingInfo SUT;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @SuppressWarnings("RedundantThrows")
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void canParseInfoItemFromField() throws Exception{
        //given

        class TestClass {
            @DataMapping(sourceClass = TestSourceClass.class ,
                    sourceProperty = SOURCE_NAME,
            toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                    ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
        SUT = getSUT(TestClass.class);

        //when
        callSUTTestMethod();

        //then
        Set<MappingInfoItem> results = SUT.getMappingInfos().get(TestSourceClass.class);
        assertThat(results).isNotEmpty();
        MappingInfoItem result = results.iterator().next();
        assertThat(result.getSourceProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestSourceClass.class,SOURCE_NAME));
        assertThat(result.getTargetProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestClass.class,"name"));
        assertThat(result.getToSourceConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterUsingGenericType.class});
    }

    @Test
    public void canParseInfoItemFromGetMethod() throws Exception {
        //given
        class TestClassUseGetMethod {

            private String name;

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
        SUT = getSUT(TestClassUseGetMethod.class);

        //when
        callSUTTestMethod();

        //then
        Set<MappingInfoItem> results = SUT.getMappingInfos().get(TestSourceClass.class);
        assertThat(results).isNotEmpty();
        MappingInfoItem result = results.iterator().next();
        assertThat(result.getSourceProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestSourceClass.class, SOURCE_NAME));
        assertThat(result.getTargetProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestClassUseGetMethod.class, "name"));
        assertThat(result.getToSourceConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterUsingGenericType.class});
    }

    @Test
    public void canParseInfoItemFromSetMethod() throws Exception {
        //given
        class TestClassUseSetMethod {

            private String name;

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
        SUT = getSUT(TestClassUseSetMethod.class);

        //when
        callSUTTestMethod();

        //then
        Set<MappingInfoItem> results = SUT.getMappingInfos().get(TestSourceClass.class);
        assertThat(results).isNotEmpty();
        MappingInfoItem result = results.iterator().next();
        assertThat(result.getSourceProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestSourceClass.class, SOURCE_NAME));
        assertThat(result.getTargetProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestClassUseSetMethod.class, "name"));
        assertThat(result.getToSourceConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterUsingGenericType.class});
    }

    @Test
    public void willIgnoreWhenNotPlaceOnGetOrSetMethod()
            throws Exception {
        //given
        class TestClassPlaceInOtherMethod {

            private String name;

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_DATE)
            public void testMethod() {
            }
        }
        SUT = getSUT(TestClassPlaceInOtherMethod.class);

        //when
        callSUTTestMethod();

        //then
        Set<MappingInfoItem> results = SUT.getMappingInfos().get(TestSourceClass.class);
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(1);
        MappingInfoItem result = results.iterator().next();
        assertThat(result.getSourceProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestSourceClass.class, SOURCE_NAME));
        assertThat(result.getTargetProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestClassPlaceInOtherMethod.class, "name"));
        assertThat(result.getToSourceConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterUsingGenericType.class});
    }

    @Test
    public void shouldThrowExceptionWhenDuplicateDefineOnGetAndSetMethodInOneProperty()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(BeanMappingException.ConstantMessage.DUPLICATE_DEFINE_ON_SAME_PROPERTY);
        class DuplicateDefineOnGetAndSetMethod {

            private String name;

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            public String getName() {
                return name;
            }

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            public void setName(String name) {
                this.name = name;
            }

        }
        SUT = getSUT(DuplicateDefineOnGetAndSetMethod.class);

        //when
        callSUTTestMethod();

        //then
        Set<MappingInfoItem> results = SUT.getMappingInfos().get(TestSourceClass.class);
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(1);
        MappingInfoItem result = results.iterator().next();
        assertThat(result.getSourceProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestSourceClass.class, SOURCE_NAME));
        assertThat(result.getTargetProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(DuplicateDefineOnGetAndSetMethod.class, "name"));
        assertThat(result.getToSourceConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterUsingGenericType.class});
    }

    @Test
    public void shouldThrowExceptionWhenDuplicateDefineOnGetMethodAndFieldInOneProperty()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(BeanMappingException.ConstantMessage.DUPLICATE_DEFINE_ON_SAME_PROPERTY);
        class DuplicateDefineOnGetMethodAndField {

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            private String name;

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

        }
        SUT = getSUT(DuplicateDefineOnGetMethodAndField.class);

        //when
        callSUTTestMethod();

        //then
        Set<MappingInfoItem> results = SUT.getMappingInfos().get(TestSourceClass.class);
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(1);
        MappingInfoItem result = results.iterator().next();
        assertThat(result.getSourceProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestSourceClass.class, SOURCE_NAME));
        assertThat(result.getTargetProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(DuplicateDefineOnGetMethodAndField.class, "name"));
        assertThat(result.getToSourceConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterUsingGenericType.class});
    }

    @Test
    public void shouldThrowExceptionWhenDuplicateDefineOnSetMethodAndFieldInOneProperty()
            throws Exception {
        //given
        expectedException.expect(BeanMappingException.class);
        expectedException.expectMessage(BeanMappingException.ConstantMessage.DUPLICATE_DEFINE_ON_SAME_PROPERTY);
        class DuplicateDefineOnSetMethodAndField {

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            private String name;

            public String getName() {
                return name;
            }

            @DataMapping(sourceClass = TestSourceClass.class,
                    sourceProperty = SOURCE_NAME,
                    toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                            ConverterFromIntegerToStringForTest.class},
                    toTargetConverterChain = {ConverterUsingGenericType.class})
            public void setName(String name) {
                this.name = name;
            }

        }
        SUT = getSUT(DuplicateDefineOnSetMethodAndField.class);

        //when
        callSUTTestMethod();

        //then
        Set<MappingInfoItem> results = SUT.getMappingInfos().get(TestSourceClass.class);
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(1);
        MappingInfoItem result = results.iterator().next();
        assertThat(result.getSourceProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestSourceClass.class, SOURCE_NAME));
        assertThat(result.getTargetProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(DuplicateDefineOnSetMethodAndField.class, "name"));
        assertThat(result.getToSourceConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterUsingGenericType.class});
    }


    /**
     * will throw {@link BeanMappingException} when configuration in the annotation
     * not match the restriction of {@link MappingInfoItem}
     */
    @Test
    @Ignore
    public void throwExceptionWhenInfoItemIsNotCorrect(){
        //TODO
        //given
        expectedException.expect(BeanMappingException.class);

        //when

        //then
    }

    @Test
    public void canReturnSameValueWhenParseTwice() throws Exception {
        //when
        BeanMappingInfo result1 = BeanMappingInfo.parse(NormalTargetClass.class),
                result2 = BeanMappingInfo.parse(NormalTargetClass.class);

        //then
        assertThat(result1).isNotNull();
        Set<MappingInfoItem> infoItemsInResult1 = result1.getMappingInfos().get(TestSourceClass.class);
        assertThat(Objects.equals(result1, result2)).isTrue();
        doVerifyInfoItemsOnNormalTargetClassCase(infoItemsInResult1);
    }

    private void doVerifyInfoItemsOnNormalTargetClassCase(Set<MappingInfoItem> results) throws IntrospectionException {
        assertThat(results).isNotEmpty();
        MappingInfoItem result = results.iterator().next();
        assertThat(result.getSourceProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(TestSourceClass.class, SOURCE_NAME));
        assertThat(result.getTargetProperty())
                .isEqualTo(IntrospectorUtils.findPropertyByName(NormalTargetClass.class, "name"));
        assertThat(result.getToSourceConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterUsingGenericType.class});
    }

    private BeanMappingInfo getSUT(Class targetClass) throws Exception{
        Constructor<BeanMappingInfo> constructor = BeanMappingInfo.class.getDeclaredConstructor(Class.class);
        constructor.setAccessible(true);
        return constructor.newInstance(targetClass);
    }

    private void callSUTTestMethod() throws Exception{
        Method method = SUT.getClass().getDeclaredMethod(PARSE_METHOD);
        method.setAccessible(true);
        method.invoke(SUT);
    }

    private static class TestSourceClass implements PropertiesSourceObject {
        private String sourceName;
        private String sourceDate;

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        public String getSourceDate() {
            return sourceDate;
        }

        public void setSourceDate(String sourceDate) {
            this.sourceDate = sourceDate;
        }
    }

    private static class NormalTargetClass {
        @DataMapping(sourceClass = TestSourceClass.class,
                sourceProperty = SOURCE_NAME,
                toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class},
                toTargetConverterChain = {ConverterUsingGenericType.class})
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}


