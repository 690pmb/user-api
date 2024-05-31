package pmb.user.rest;

import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pmb.user.exception.AlreadyExistException;

/**
 * Handles exceptions across the whole application.
 *
 * @see ResponseEntityExceptionHandler
 */
@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ErrorHandler.class);

  @ExceptionHandler
  public ResponseEntity<Object> handleAlreadyExistException(AlreadyExistException exception) {
    return handleException(exception, HttpStatus.CONFLICT, null, "handleAlreadyExistException");
  }

  @ExceptionHandler
  public ResponseEntity<Object> handleAuthenticationException(AuthenticationException exception) {
    return handleException(
        exception, HttpStatus.UNAUTHORIZED, null, "handleAuthenticationException");
  }

  @ExceptionHandler
  public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    return handleException(
        ex,
        HttpStatus.BAD_REQUEST,
        "Type of parameter '" + ex.getName() + "' doesn't match with definition.",
        "handleMethodArgumentTypeMismatch");
  }

  @ExceptionHandler
  protected ResponseEntity<Object> handleConstraintViolationException(
      ConstraintViolationException ex) {
    return handleException(
        ex,
        HttpStatus.BAD_REQUEST,
        ex.getConstraintViolations().stream()
            .map(c -> "Field: '" + c.getPropertyPath() + "', Message: '" + c.getMessage() + "'")
            .collect(Collectors.joining()),
        "handleConstraintViolationException");
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    return handleException(
        ex,
        HttpStatus.BAD_REQUEST,
        "Parameter '" + ex.getParameterName() + "' is missing.",
        "handleMissingServletRequestParameter");
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    return handleException(ex, HttpStatus.BAD_REQUEST, null, "MethodArgumentNotValid");
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    return handleException(ex, HttpStatus.BAD_REQUEST, null, "HttpMessageNotReadable");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> exceptionHandler(Exception e) {
    return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR, null, "Exception thrown");
  }

  private static ResponseEntity<Object> handleException(
      Exception e, HttpStatus status, String body, String message) {
    LOG.error(message, e);
    return new ResponseEntity<>(Optional.ofNullable(body).orElse(e.getMessage()), status);
  }
}
