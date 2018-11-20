package org.nanospark.beanmapping.common.exception;

public class BeanMappingException extends RuntimeException {

    public BeanMappingException() {
    }

    public BeanMappingException(String message) {
        super(message);
    }

    public BeanMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanMappingException(Throwable cause) {
        super(cause);
    }

    public interface ConstantMessage {
        String SYSTEM_EXCEPTION = "cause by the system exception";

        String SOURCE_IS_NULL = "source propertyDescriptor is null";
        String TARGET_IS_NULL = "target propertyDescriptor is null";

        String GET_METHOD_IS_NULL = "the get method of the property is null";
        String SET_METHOD_IS_NULL = "the set method of the property is null";

        String PROPERTY_TYPE_IS_DIFFERENT = "the type of the source property and the target property is different";

        String CONVERTER_NOT_HAS_DEFAULT_CONSTRUCTOR = "one of the converter in the chain does not have the default constructor";
        String CONVERTER_CAN_NOT_CONSTRUCT = "one of the converter in the chain can not construct by the default constructor";

        String NOT_CONVERT_METHOD = "the given converter does not have the convert method";
        String DUPLICATED_CONVERT_METHOD = "duplicate convert method names 'apply' in this class";
        String CONVERTER_NOT_MATCH_PREVIOUS_TYPE ="the input type of the converter can not be assigned from the return type of previous converter";
        String CONVERTER_CHAIN_NOT_MATCH_INPUT_TYPE = "the input type of the converter chain can not be assigned from the return parameter of property get method";
        String CONVERTER_CHAIN_NOT_MATCH_RETURN_TYPE = "the output type of the converter chain can assigned to the input parameter of property set method";
        String GENERIC_TYPE_OF_CONVERTER_IS_NOT_OBJECT = "the converter use generic type on convert method, but the signature type is not Object.class  ";

        String FIELD_NOT_IS_NOT_A_PROPERTY = "DataMapping annotation is place on a field which is not a property";
        String CAN_NOT_FOUND_PROPERTY_ON_SOURCE_CLASS = "can not find the property on the source class";
        String DUPLICATE_DEFINE_ON_SAME_PROPERTY = "duplicate define using this annotation on the same property";

        String DO_CONVERT_FAILED = "convert failed ";
    }
}
