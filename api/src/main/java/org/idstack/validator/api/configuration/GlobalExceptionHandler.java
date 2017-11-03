package org.idstack.validator.api.configuration;

import com.google.gson.Gson;
import org.idstack.feature.Constant;
import org.idstack.feature.exception.GlobalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

/**
 * @author Chanaka Lakmal
 * @date 27/8/2017
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handle(Exception e) {
        return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, new GlobalException(e.toString(), e.getStackTrace())));
    }
}
