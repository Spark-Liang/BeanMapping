package com.lzh.beanmapping.core;

import com.lzh.beanmapping.common.PropertiesSourceObject;
import com.lzh.beanmapping.common.converter.DateToStringConverter;
import com.lzh.beanmapping.common.converter.StringToDateConverter;
import com.lzh.beanmapping.core.annotation.DataMapping;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"WeakerAccess", "unchecked", "deprecation"})
public class TestBeanTransformer {

    @Before
    public void setUp() {
    }

    /**
     * Test {@link BeanTransformer#getTargetInstanceFrom(PropertiesSourceObject)}
     */
    @Test
    public void canGetTargetInstanceFromSource() {
        //given
        String sourceName = "Test", sourceDate = "01/01/2018";
        TestSourceClass sourceClassInstance = new TestSourceClass();
        sourceClassInstance.setSourceDate(sourceDate);
        sourceClassInstance.setSourceName(sourceName);

        //when
        BeanTransformer<TestTargetClass, TestSourceClass> resultInstance
                = BeanTransformer.newInstance(TestTargetClass.class, TestSourceClass.class);
        TestTargetClass resultToVerify = resultInstance.getTargetInstanceFrom(sourceClassInstance);

        //then
        assertThat(resultToVerify).isNotNull();
        assertThat(resultToVerify.getDate()).isEqualTo(new Date(sourceDate));
        assertThat(resultToVerify.getName()).isEqualTo(sourceName);
    }

    /**
     * Test {@link BeanTransformer#getTargetInstanceFrom(PropertiesSourceObject)}
     */
    @Test
    public void testCanMergePropertyFromSource() {
        //given
        String sourceName = "Test", sourceDate = "01/01/2018";
        TestSourceClass sourceClassInstance = new TestSourceClass();
        sourceClassInstance.setSourceDate(sourceDate);
        sourceClassInstance.setSourceName(sourceName);
        TestTargetClass targetClass = new TestTargetClass();

        //when
        BeanTransformer<TestTargetClass, TestSourceClass> resultInstance
                = BeanTransformer.newInstance(TestTargetClass.class, TestSourceClass.class);
        TestTargetClass resultToVerify = resultInstance.mergeProperties(targetClass, sourceClassInstance);

        //then
        assertThat(resultToVerify).isNotNull();
        assertThat(resultToVerify.getDate()).isEqualTo(new Date(sourceDate));
        assertThat(resultToVerify.getName()).isEqualTo(sourceName);
    }

    /**
     * Test {@link BeanTransformer#getTargetInstanceFrom(PropertiesSourceObject)}
     */
    @Test
    public void testCanTransformBeanBackToSource() {
        //given
        String sourceName = "Test", sourceDate = "01/01/2018";
        TestTargetClass targetClass = new TestTargetClass();
        targetClass.setDate(new StringToDateConverter().apply(sourceDate));
        targetClass.setName(sourceName);

        //when
        BeanTransformer<TestTargetClass, TestSourceClass> resultInstance
                = BeanTransformer.newInstance(TestTargetClass.class, TestSourceClass.class);
        TestSourceClass resultToVerify = resultInstance.getSourceInstanceFrom(targetClass);

        //then
        assertThat(resultToVerify).isNotNull();
        assertThat(resultToVerify.getSourceDate()).isEqualTo(sourceDate);
        assertThat(resultToVerify.getSourceName()).isEqualTo(sourceName);
    }

    /**
     * Test {@link BeanTransformer#getTargetInstanceFrom(PropertiesSourceObject)}
     */
    @Test
    public void testCanMergePropertyFromTarget() {
        //given
        String sourceName = "Test", sourceDate = "01/01/2018";
        TestSourceClass sourceClassInstance = new TestSourceClass();
        TestTargetClass targetClass = new TestTargetClass();
        targetClass.setDate(new StringToDateConverter().apply(sourceDate));
        targetClass.setName(sourceName);

        //when
        BeanTransformer<TestTargetClass, TestSourceClass> resultInstance
                = BeanTransformer.newInstance(TestTargetClass.class, TestSourceClass.class);
        TestSourceClass resultToVerify = resultInstance.mergePropertiesToSource(sourceClassInstance, targetClass);

        //then
        assertThat(resultToVerify).isNotNull();
        assertThat(resultToVerify.getSourceDate()).isEqualTo(sourceDate);
        assertThat(resultToVerify.getSourceName()).isEqualTo(sourceName);
    }


    @SuppressWarnings("unused")
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

        @DataMapping(sourceClass = TestSourceClass.class, sourceProperty = "sourceDate",
                toTargetConverterChain = StringToDateConverter.class,
                toSourceConverterChain = DateToStringConverter.class)
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

