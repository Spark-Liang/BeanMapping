package com.lzh.beanmapping.common.converter;

import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


public class StringToDateConverterTest {

    private StringToDateConverter SUT = new StringToDateConverter();

    @Test
    public void canConvertStringToDate() {
        //given
        String str = "01/01/2018";

        //when
        Date result = SUT.apply(str);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getDay()).isEqualTo(1);
        assertThat(result.getMonth() + 1).isEqualTo(1);
        assertThat(result.getYear() + 1900).isEqualTo(2018);
    }
}