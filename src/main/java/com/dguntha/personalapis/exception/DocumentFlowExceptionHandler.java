package com.dguntha.personalapis.exception;

import com.dguntha.personalapis.model.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class DocumentFlowExceptionHandler {

    private static final String APP_NAME = "docflow_admin_module";
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logErrorMsg(ex, "handleMethodArgumentNotValid");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(constructResponse(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST.name()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseDto> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        logErrorMsg(ex, "handleMissingServletRequestParameterException");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(constructResponse(ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST.name()));

    }
    @ExceptionHandler(DocumentIdNotFoundException.class)
    public ResponseEntity<ResponseDto> handleDocumentIdNotFoundException(DocumentIdNotFoundException ex) {
        logErrorMsg(ex, "handleDocumentIdNotFoundException");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(constructResponse(ex.getMessage(), HttpStatus.NOT_FOUND.name()));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ResponseDto> handleFileUploadException(FileUploadException ex) {
        logErrorMsg(ex, "handleFileUploadException");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(constructResponse(ex.getMessage(), HttpStatus.NOT_FOUND.name()));
    }

    @ExceptionHandler(DocumentAlreadyExistsException.class)
    public ResponseEntity<ResponseDto> handleDocumentAlreadyExistsException(DocumentAlreadyExistsException ex) {
        logErrorMsg(ex, "handleDocumentAlreadyExistsException");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(constructResponse(ex.getMessage(), HttpStatus.NOT_FOUND.name()));
    }

    @ExceptionHandler(DocumentNotPresentException.class)
    public ResponseEntity<ResponseDto> handleDocumentNotPresentFoundException(DocumentNotPresentException ex) {
        logErrorMsg(ex, "handleDocumentIdNotFoundException");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(constructResponse(ex.getMessage(), HttpStatus.NOT_FOUND.name()));
    }

    private ResponseDto constructResponse(String message, String code) {
        return  ResponseDto
                .builder()
                .code(code)
                .message(message)
                .build();
    }
    private void logErrorMsg(Throwable ex, String methodName) {
        log.error("{} - {} - {}", APP_NAME, methodName, ex.getMessage());
    }

}
