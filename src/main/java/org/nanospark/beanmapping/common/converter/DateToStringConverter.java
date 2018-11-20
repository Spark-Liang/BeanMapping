package org.nanospark.beanmapping.common.converter;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

import static org.nanospark.beanmapping.common.converter.DefaultDateFormat.DEFAULT_DATE_FORMAT;

public class DateToStringConverter implements Function<Date, String> {
    private SimpleDateFormat formater = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

    @Override
    public String apply(Date date) {
        return formater.format(date);
    }
}
