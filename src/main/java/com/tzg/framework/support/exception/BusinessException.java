package com.tzg.framework.support.exception;

/**
 * @author: Mark
 * Description: 全局业务异常
 */
public class BusinessException extends RuntimeException {

    public BusinessException( String message ) {
        super( message );
    }

}
