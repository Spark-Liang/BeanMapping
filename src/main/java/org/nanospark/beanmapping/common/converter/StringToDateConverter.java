package org.nanospark.beanmapping.common.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

import static org.nanospark.beanmapping.common.converter.DefaultDateFormat.DEFAULT_DATE_FORMAT;

public class StringToDateConverter implements Function<String, Date> {

    private SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

    @Override
    public Date apply(String string) {
        try {
            return formatter.parse(string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
