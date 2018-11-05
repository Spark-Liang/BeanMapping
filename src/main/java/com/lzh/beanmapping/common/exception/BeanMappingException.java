package com.lzh.beanmapping.common.exception;

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
}
