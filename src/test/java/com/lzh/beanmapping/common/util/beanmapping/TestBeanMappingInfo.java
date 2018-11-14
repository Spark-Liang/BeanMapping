package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.annotation.DataMapping;
import com.lzh.beanmapping.common.converter.StringToDateConverter;
import org.junit.Before;
import org.junit.Test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBeanMappingInfo {

    private BeanMappingInfo SUT;

    @Before
    public void setUp() throws Exception {
        Constructor<BeanMappingInfo> constructor = BeanMappingInfo.class.getDeclaredConstructor(new Class[]{Class.class});
        constructor.setAccessible(true);
        SUT = constructor.newInstance(TestTargetClass.class);
    }

    @Test
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

    }

    private Map<String, PropertyDescriptor> getPrpertyDescriptor(Class clazz) throws Exception {
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        return Arrays.stream(propertyDescriptors)
                .collect(Collectors
                        .toMap(PropertyDescriptor::getName, e -> e));
    }

}

class TestSourceClass implements PropertiesSourceObject {
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

class TestTargetClass {

    @DataMapping(sourceClass = TestSourceClass.class, sourceProperty = "sourceName")
    private String name;

    @DataMapping(sourceClass = TestSourceClass.class, sourceProperty = "sourceDate", converter = StringToDateConverter.class)
    private Date date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
