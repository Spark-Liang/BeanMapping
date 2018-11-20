package org.nanospark.beanmapping.common.util;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


public class ReflectUtilsTest {

    @Test
    public void canGetAllFieldFromGivenClass() {
        //given
        Class baseClass = TestClassExtendObject.class,
                extendClass = TestClassExtendOtherClass.class;

        //when
        Field[] fields = ReflectUtils.getAllDeclaredFields(TestClassExtendOtherClass.class);

        //then
        assertThat(fields).isNotNull();
        assertThat(fields).contains(ArrayUtils.concat(baseClass.getDeclaredFields(), extendClass.getDeclaredFields()));
    }

    @Test
    public void canGetAllFieldFromGivenClassAndStopByCertainClass() {
        //given
        Class baseClass = TestClassExtendObject.class,
                extendClass = TestClassExtendOtherClass.class;

        //when
        Field[] fields = ReflectUtils.getAllDeclaredFields(TestClassExtendOtherClass.class, TestClassExtendOtherClass.class);

        //then
        assertThat(fields).isNotNull();
        assertThat(fields).contains(extendClass.getDeclaredFields());
    }

    @Test
    public void canGetAllFieldFromGivenClassWhenGiveNullToStopClass() {
        //given
        Class baseClass = TestClassExtendObject.class,
                extendClass = TestClassExtendOtherClass.class;

        //when
        Field[] fields = ReflectUtils.getAllDeclaredFields(TestClassExtendOtherClass.class, (Class) null);

        //then
        assertThat(fields).isNotNull();
        assertThat(fields).contains(ArrayUtils.concat(baseClass.getDeclaredFields(), extendClass.getDeclaredFields()));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenStopClassIsSubClassOfGivenClass() {
        //when
        ReflectUtils.getAllDeclaredFields(TestClassExtendObject.class, TestClassExtendOtherClass.class);
    }

    @Test
    public void canDoFilterBasedOnGivenPredicated() throws Exception {
        //given
        Class baseClass = TestClassExtendObject.class,
                extendClass = TestClassExtendOtherClass.class;

        //when
        Field[] fields = ReflectUtils.getAllDeclaredFields(TestClassExtendOtherClass.class, field -> field.getName().equals("date"));

        //then
        assertThat(fields).isNotNull();
        assertThat(fields).hasSize(1);
        assertThat(fields).contains(extendClass.getDeclaredField("date"));
    }

}

class TestClassExtendObject {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class TestClassExtendOtherClass extends TestClassExtendObject {
    private String betterName;

    private Date date;

    public String getBetterName() {
        return betterName;
    }

    public void setBetterName(String betterName) {
        this.betterName = betterName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}