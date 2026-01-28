package org.egov.filestore.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleNotFound(ResponseStatusException ex) {
        log.trace("Entering handleNotFound method");
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.warn("Resource not found, returning 404 page. Status: {}", ex.getStatusCode());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body(new ClassPathResource("static/404.html"));
        }
        log.error("Unhandled ResponseStatusException with status: {}", ex.getStatusCode(), ex);
        throw ex;
    }
}