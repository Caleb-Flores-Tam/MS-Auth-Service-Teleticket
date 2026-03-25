package com.luisalt20.auth.exception;

import com.luisalt20.auth.dto.response.ApiExceptionResponse;
import com.luisalt20.auth.util.ConstantUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class ResponseExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiExceptionResponse> methodArgumentNotValidException(
          MethodArgumentNotValidException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiExceptionResponse.builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message(exception.getBindingResult().getFieldErrors()
                            .stream().findFirst().get().getDefaultMessage())
                    .build());
  }

  @ExceptionHandler(MissingPathVariableException.class)
  public ResponseEntity<ApiExceptionResponse> missingPathVariableException(
          MissingPathVariableException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiExceptionResponse.builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message(exception.getMessage())
                    .build());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiExceptionResponse> missingServletRequestParameterException(
          MissingServletRequestParameterException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiExceptionResponse.builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message(exception.getMessage())
                    .build());
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ApiExceptionResponse> missingServletRequestPartException(
          MissingServletRequestPartException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiExceptionResponse.builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message(exception.getMessage())
                    .build());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiExceptionResponse> maxUploadSizeExceededException(
          MaxUploadSizeExceededException exception) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiExceptionResponse.builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message("El archivo excede el tamaño máximo permitido")
                    .build());
  }

  @ExceptionHandler(ApiValidateException.class)
  public ResponseEntity<ApiExceptionResponse> validationException(ApiValidateException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiExceptionResponse.builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message(exception.getLocalizedMessage())
                    .build());
  }

  @ExceptionHandler(ApiErrorException.class)
  public ResponseEntity<ApiExceptionResponse> errorException(ApiErrorException exception) {
    log.error(exception.getMessage(), exception);
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiExceptionResponse.builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message(exception.getMessage())
                    .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiExceptionResponse> exception(Exception exception) {
    log.error("Unexpected error: {}", exception.getMessage(), exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiExceptionResponse.builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message(ConstantUtil.ERROR_MESSAGE)
                    .build());
  }
}
