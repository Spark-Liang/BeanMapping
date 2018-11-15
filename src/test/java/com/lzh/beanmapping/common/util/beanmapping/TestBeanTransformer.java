package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.annotation.DataMapping;
import com.lzh.beanmapping.common.converter.StringToDateConverter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cglib.core.DebuggingClassWriter;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBeanTransformer {

    @Before
    public void setUp(){
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY,"E:\\Java\\temp data\\cglib classes");
    }

    @Test
    public void testCanTransformBean(){
        //given
        String sourceName = "Test",sourceDate = "01/01/2018";
        TestSourceClass sourceClassInstance = new TestSourceClass();
        sourceClassInstance.setSourceDate(sourceDate);
        sourceClassInstance.setSourceName(sourceName);

        //when
        BeanTransformer<TestTargetClass,TestSourceClass> resultInstance
                = BeanTransformer.newInstance(TestTargetClass.class,TestSourceClass.class);
        TestTargetClass resultToVerify = resultInstance.getTargetInstanceFrom(sourceClassInstance);

        //then
        assertThat(resultToVerify).isNotNull();
        assertThat(resultToVerify.getDate()).isEqualTo(new Date(sourceDate));
        assertThat(resultToVerify.getName()).isEqualTo(sourceName);
    }












    public static class TestSourceClass implements PropertiesSourceObject {
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

    public static class TestTargetClass {

        @DataMapping(sourceClass = TestSourceClass.class, sourceProperty = "sourceName")
        private String name;

//        @DataMapping(sourceClass = TestSourceClass.class, sourceProperty = "sourceDate", converter = StringToDateConverter.class)
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
}

