package org.billing.crm.advice;

import lombok.extern.slf4j.Slf4j;
import org.billing.crm.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CrmExceptionHandler {

    @ExceptionHandler({BadAbonentAddException.class, BadAbonentPayException.class, BadChangeTariffException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        log.error("Occurred BadRequestException with message: {}({})", ex.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NotFoundAbonentException.class, NotFoundReportException.class, NotFoundUserException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> notFoundException(RuntimeException ex) {
        log.error("Occurred NotFoundException with message: {}({})", ex.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({JwtAuthenticationException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> authException(RuntimeException ex) {
        log.error("Occurred JwtAuthException with message: {}({})", ex.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}