package org.nanospark.beanmapping.common.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

@SuppressWarnings("WeakerAccess")
public class IntrospectorUtils {

    /**
     * get all the {@link PropertyDescriptor} from the given class but not contain the {@link PropertyDescriptor} from the super class
     * ,and do filter by the given name
     *
     * @param clazz        the class that provide all {@link PropertyDescriptor}
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
     * @param clazz        the class that provide all {@link PropertyDescriptor}
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
     * @param clazz        the class that provide all {@link PropertyDescriptor}
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
                    .orElse(null);
        } else {
            return null;
        }
    }

    /**
     * get all {@link PropertyDescriptor} until Object.class
     *
     * @param clazz the class that provide {@link PropertyDescriptor}
     * @return all the {@link PropertyDescriptor}
     */
    public static PropertyDescriptor[] getAllPropertyDescriptors(Class clazz)
            throws IntrospectionException {
        return getPropertyDescriptors(clazz, Object.class);
    }

    /**
     * get all {@link PropertyDescriptor} until the given stop class
     *
     * @param clazz     the class that provide {@link PropertyDescriptor}
     * @param stopClass the class to stop find {@link PropertyDescriptor}
     * @return all the {@link PropertyDescriptor}
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Class clazz, Class stopClass)
            throws IntrospectionException {
        return Introspector.getBeanInfo(clazz, stopClass).getPropertyDescriptors();
    }
}
