package com.lzh.beanmapping.common.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

public class IntrospectorUtils {

    /**
     * get all the {@link PropertyDescriptor} from the given class but not contain the {@link PropertyDescriptor} from the super class
     * ,and do filter by the given name
     *
     * @param clazz        the class that provide all property
     * @param propertyName name of the needed property
     * @return the needed {@link PropertyDescriptor} or null when no one match the name
     */
    public static PropertyDescriptor getPropertyByName(Class clazz, String propertyName)
            throws IntrospectionException {
        return getPropertyByName(clazz, null, propertyName);
    }

    /**
     * get all the {@link PropertyDescriptor} from the given class and include the {@link PropertyDescriptor} from the super class
     * ,and do filter by the given name
     *
     * @param clazz        the class that provide all property
     * @param propertyName name of the needed property
     * @return the needed {@link PropertyDescriptor} or null when no one match the name
     */
    public static PropertyDescriptor findPropertyByName(Class clazz, String propertyName)
            throws IntrospectionException {
        return getPropertyByName(clazz, Object.class, propertyName);
    }

    /**
     * get all the {@link PropertyDescriptor} from the given class and only contain the {@link PropertyDescriptor} from the class that extends stop class
     * ,and do filter by the given name
     *
     * @param clazz        the class that provide all property
     * @param stopClass    restrict the scope to search the {@link PropertyDescriptor}
     *                     when input null means only search the {@link PropertyDescriptor} in first parameter
     * @param propertyName name of the needed property
     * @return the needed {@link PropertyDescriptor} or null when no one match the name
     */
    public static PropertyDescriptor getPropertyByName(Class clazz, Class stopClass, String propertyName)
            throws IntrospectionException {
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz, stopClass)
                .getPropertyDescriptors();
        if (propertyDescriptors != null && propertyDescriptors.length > 0) {
            return Arrays.stream(propertyDescriptors)
                    .filter(e -> e.getName().equals(propertyName))
                    .findFirst()
                    .orElseGet(() -> null);
        } else {
            return null;
        }
    }
}