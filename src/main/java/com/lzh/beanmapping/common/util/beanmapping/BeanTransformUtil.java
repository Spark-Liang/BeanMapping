package com.lzh.beanmapping.common.util.beanmapping;

import com.lzh.beanmapping.common.PropertiesSourceObject;

public class BeanTransformUtil {
    /**
     * create a new POJO base on given target class and class of properties provider
     *
     * @param target class of target instance
     * @param source instance of the properties provider
     * @param <T>    raw type of target class
     * @return an instance of target class
     * @throws {@link ClassNotFoundException}
     */
    public static <T> T newInstanceFrom(Class<T> target, PropertiesSourceObject source) throws ClassNotFoundException {
        //TODO
        return null;
    }

    public static <T> T mergeProperties(T target, PropertiesSourceObject source) {
        //TODO
        return null;
    }

    public static <T, S extends PropertiesSourceObject> BeanTransformer<T, S> getInstance(Class<T> targetClass, Class<S> sourceClass) {
        //TODO
        return null;
    }
}
