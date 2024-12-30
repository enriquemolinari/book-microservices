package web;

import api.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllMyException(
            Exception ex,
            WebRequest request) {

        return new ResponseEntity<Object>(Map.of(MESSAGE_KEY,
                ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Object> handleAllBusinessExceptions(
            Exception ex,
            WebRequest request) {

        return new ResponseEntity<Object>(Map.of(MESSAGE_KEY,
                ex.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }
}
