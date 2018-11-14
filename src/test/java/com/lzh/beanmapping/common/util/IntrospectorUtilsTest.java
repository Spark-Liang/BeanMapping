package com.lzh.beanmapping.common.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.powermock.api.mockito.PowerMockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({IntrospectorUtils.class, Introspector.class})
@PowerMockIgnore("javax.management.*")
public class IntrospectorUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test {@link IntrospectorUtils#getPropertyByName(Class, String)}
     */
    @Test
    public void shouldReturnDescriptorWhenExists() throws Exception {
        //given
        String propertyName = "name";

        //when
        PropertyDescriptor result = IntrospectorUtils.getPropertyByName(TestClass.class, propertyName);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(propertyName);
    }

    /**
     * Test {@link IntrospectorUtils#getPropertyByName(Class, String)}
     */
    @Test
    public void shouldReturnNullWhenPropertyNoExists() throws Exception {
        //when
        PropertyDescriptor result = IntrospectorUtils.getPropertyByName(TestClass.class, "name1");

        //then
        assertThat(result).isNull();
    }

    /**
     * Test {@link IntrospectorUtils#getPropertyByName(Class, String)}
     */
    @Test
    public void shouldThrowExceptionWhenCanNotParserTheGivenClass() throws Exception {
        //given
        expectedException.expect(IntrospectionException.class);
        mockStatic(Introspector.class);
        when(Introspector.getBeanInfo(any(), any()))
                .thenThrow(new IntrospectionException("Test"));
        //when
        IntrospectorUtils.getPropertyByName(TestClass.class, "name1");

    }

    /**
     * Test {@link IntrospectorUtils#findPropertyByName(Class, String)}
     */
    @Test
    public void canFindPropertyFromSuperClass() throws Exception {
        //given
        String propertyName = "name";

        //when
        PropertyDescriptor result = IntrospectorUtils.findPropertyByName(TestSubClass.class, propertyName);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(propertyName);
    }

    /**
     * Test {@link IntrospectorUtils#getPropertyByName(Class, Class, String)}
     */
    @Test
    public void shouldThrowExceptionWhenStopClassIsNotSuperClass() throws Exception {
        //given
        expectedException.expect(IntrospectionException.class);

        //when
        IntrospectorUtils.getPropertyByName(TestClass.class, TestSubClass.class, "name");
    }

    /**
     * Test {@link IntrospectorUtils#getPropertyByName(Class, Class, String)}
     */
    @Test
    public void shouldReturnNullWhenFindPropertyInEmptyClass() throws Exception {

        //when
        PropertyDescriptor result = IntrospectorUtils.getPropertyByName(TestSubClass.class, TestClass.class, "name");

        //then
        assertThat(result).isNull();
    }

    static class TestClass {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class TestSubClass extends TestClass {

    }
}