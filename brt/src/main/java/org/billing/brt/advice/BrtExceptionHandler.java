package org.billing.brt.advice;

import lombok.extern.slf4j.Slf4j;
import org.billing.brt.exceptions.FurtherTarifficationException;
import org.billing.brt.exceptions.NotFoundReportException;
import org.billing.brt.exceptions.NotFoundSubscriberException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BrtExceptionHandler {

    @ExceptionHandler({FurtherTarifficationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        log.error("Occurred BadRequestException with message: {}({})", ex.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NotFoundReportException.class, NotFoundSubscriberException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> notFoundException(RuntimeException ex) {
        log.error("Occurred NotFoundException with message: {}({})", ex.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}