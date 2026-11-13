package com.mysite.sbb;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 조회 대상이 없을 때 던지는 예외. 스프링이 404 응답으로 바꿔 준다.
 * NoSuchElementException("No value present") 보다 원인이 분명하다.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "entity not found")
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }
}
