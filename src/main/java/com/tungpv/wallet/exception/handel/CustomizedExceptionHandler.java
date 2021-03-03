package com.tungpv.wallet.exception.handel;

import com.tungpv.wallet.exception.BadRequestException;
import com.tungpv.wallet.exception.DataNotFoundException;
import com.tungpv.wallet.exception.ErrorObject;
import com.tungpv.wallet.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

@ControllerAdvice
public class CustomizedExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorObject> customHandleNotFound(Exception ex, WebRequest request) {
        logger.error(ex.getMessage(), ex);

        ErrorObject errors = new ErrorObject();
        errors.setMessage(ex.getMessage());
        errors.setCode(HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorObject> customHandleServiceException(Exception ex, WebRequest request) {
        logger.error(ex.getMessage());
        ErrorObject errors = new ErrorObject();
        errors.setMessage(ex.getMessage());
        errors.setCode(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorObject> customHandleBadRequestException(Exception ex, WebRequest request) {
        logger.error(ex.getMessage());
        ErrorObject errors = new ErrorObject();
        errors.setMessage(ex.getMessage());
        errors.setCode(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorObject> handleConstraintViolationException(
            ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        logger.error(e.getMessage(), e);
        ErrorObject errors = new ErrorObject();
        String messageEx = "";
        for (ConstraintViolation<?> violation : violations) {
            if (StringUtils.isEmpty(messageEx)) {
                messageEx = violation.getMessage();
            }
        }

        if (!StringUtils.isEmpty(messageEx)) {
            errors.setMessage(messageSource.getMessage(messageEx, null, LocaleContextHolder.getLocale()));
        }

        errors.setCode(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({TransactionSystemException.class, DataIntegrityViolationException.class})
    protected ResponseEntity<ErrorObject> customHandleException(Exception e) {
        ErrorObject error = new ErrorObject();
        String message;
        Throwable cause;
        Throwable resultCause = e;
        while ((cause = resultCause.getCause()) != null && resultCause != cause) {
            resultCause = cause;
        }
        if (resultCause instanceof ConstraintViolationException) {
            message = ((ConstraintViolationException) resultCause).getConstraintViolations()
                    .iterator()
                    .next()
                    .getMessage();
            if (!StringUtils.isEmpty(message)) {
                message = messageSource.getMessage(message, null, LocaleContextHolder.getLocale());
            }
        } else {
            logger.error(e.getMessage(), e);
            message = "Unknown error";
        }
        error.setMessage(message);
        error.setCode(HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
