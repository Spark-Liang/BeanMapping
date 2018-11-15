package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.annotation.DataMapping;
import com.lzh.beanmapping.common.converter.StringToDateConverter;
import com.lzh.beanmapping.common.exception.BeanMappingException;
import com.lzh.beanmapping.common.util.IntrospectorUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MappingInfoItem.class})
public class TestBeanMappingInfo {
    private static final String PARSE_METHOD = "parseMappingInfo";
    private static final String SOURCE_NAME = "sourceName";
    private static final String SOURCE_DATE = "sourceDate";

    private BeanMappingInfo SUT;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
    }

   /* @Test
    public void testParserClass() throws Exception {
        //given
        Map<String, PropertyDescriptor> propertyDescriptorMapOfSource = getPrpertyDescriptor(TestSourceClass.class);
        Map<String, PropertyDescriptor> propertyDescriptorMapOfTarget = getPrpertyDescriptor(TestTargetClass.class);

        //when
        SUT.parserMappingInfo();

        //then
        Set<MappingInfoItem> mappingInfoItems = SUT.getMappingInfos().get(TestSourceClass.class);
        assertThat(mappingInfoItems.size()).isEqualTo(2);
        MappingInfoItem itemWithoutConverter = new MappingInfoItem();
        itemWithoutConverter.setSourceProperty(propertyDescriptorMapOfSource.get("sourceName"));
        itemWithoutConverter.setTargetProperty(propertyDescriptorMapOfTarget.get("name"));
//        itemWithoutConverter.setNeedDeepCopy(false);
//        itemWithoutConverter.setConverter(null);
        MappingInfoItem itemWithConverter = new MappingInfoItem();
        itemWithConverter.setSourceProperty(propertyDescriptorMapOfSource.get("sourceDate"));
        itemWithConverter.setTargetProperty(propertyDescriptorMapOfTarget.get("date"));
//        itemWithConverter.setNeedDeepCopy(false);
//        itemWithConverter.setConverter(new StringToDateConverter());
        assertThat(mappingInfoItems).contains(itemWithoutConverter);
        assertThat(mappingInfoItems).contains(itemWithConverter);

    }*/

    private Map<String, PropertyDescriptor> getPrpertyDescriptor(Class clazz) throws Exception {
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        return Arrays.stream(propertyDescriptors)
                .collect(Collectors
                        .toMap(PropertyDescriptor::getName, e -> e));
    }


    @Test
    public void canParseInfoItemFromField() throws Exception{
        //given
        class TestClass {
            @DataMapping(sourceClass = TestSourceClass.class ,
                    sourceProperty = SOURCE_NAME,
            toSourceConverterChain = {ConverterFromStringToIntegerForTest.class,
                    ConverterFromIntegerToStringForTest.class},
            toTargetConverterChain = {ConverterForTest.class})
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
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterForTest.class});
        assertThat(result.getToTargetConverterChain())
                .isEqualTo(new Class[]{ConverterFromStringToIntegerForTest.class,
                        ConverterFromIntegerToStringForTest.class});
    }

    @Test
    public void canParseInfoItemFromGetMethod(){
        //TODO
    }

    @Test
    public void canParseInfoItemFromSetMethod(){
        //TODO
    }

    @Test
    public void throwExceptionWhenNotPlaceOnGetOrSetMethod(){
        //TODO
    }

    /**
     * will throw {@link BeanMappingException} when configuration in the annotation
     * not match the restriction of {@link MappingInfoItem}
     */
    @Test
    public void throwExceptionWhenInfoItemIsNotCorrect(){
        //TODO
        //given
        expectedException.expect(BeanMappingException.class);

        //when

        //then
    }

    private BeanMappingInfo getSUT(Class targetClass) throws Exception{
        Constructor<BeanMappingInfo> constructor = BeanMappingInfo.class.getDeclaredConstructor(new Class[]{Class.class});
        constructor.setAccessible(true);
        return constructor.newInstance(targetClass);
    }

    private void callSUTTestMethod() throws Exception{
        Method method = SUT.getClass().getDeclaredMethod(PARSE_METHOD,new Class[]{});
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

    private static class ConverterFromStringToIntegerForTest implements Function<String,Integer> {

        public ConverterFromStringToIntegerForTest(){}

        @Override
        public Integer apply(String o) {
            return 1;
        }
    }

    private static class ConverterFromIntegerToStringForTest implements Function<Integer,String> {

        public ConverterFromIntegerToStringForTest (){}

        @Override
        public String  apply(Integer o) {
            return "";
        }
    }

    private static class ConverterForTest implements Function<Object,Object>{

        public ConverterForTest(){}

        @Override
        public Object apply(Object o) {
            return o;
        }
    }



}


