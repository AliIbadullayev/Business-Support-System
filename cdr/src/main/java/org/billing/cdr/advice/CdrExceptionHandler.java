package org.billing.cdr.advice;

import lombok.extern.slf4j.Slf4j;
import org.billing.cdr.exceptions.GenerateCDRException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CdrExceptionHandler {

    @ExceptionHandler({GenerateCDRException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        log.error("Occurred BadRequestException with message: {}({})", ex.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}