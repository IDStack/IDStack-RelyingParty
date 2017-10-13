package org.idstack.relyingparty.api.configuration;

import com.google.gson.Gson;
import org.idstack.feature.Constant;
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public String handle(RuntimeException e) {
        return new Gson().toJson(Collections.singletonMap(Constant.Status.STATUS, e.getMessage()));
    }
}
