package com.lzh.beanmapping.common.util.beanmapping;

import org.junit.Test;
import org.springframework.cglib.core.KeyFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBeanTransformerKey {

    @Test
    public void canBeCreateFromKeyFactory() {
        //when
        BeanTransformer.BeanTransformerKey key
                = (BeanTransformer.BeanTransformerKey) KeyFactory.create(BeanTransformer.BeanTransformerKey.class);

        //then
        assertThat(key).isNotNull();
    }
}
