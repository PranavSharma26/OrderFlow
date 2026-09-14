package com.orderflow.payment_service.response;

import com.orderflow.payment_service.exception.OrderNotFoundException;
import com.orderflow.payment_service.exception.PaymentAlreadyExistsException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalApiResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            org.springframework.http.server.ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response
    ) {

        if (body instanceof ApiResponse apiResponse) {
            return new ApiResponse(
                    LocalDateTime.now(),
                    apiResponse.getMessage(),
                    apiResponse.getStatus(),
                    apiResponse.isSuccess(),
                    apiResponse.getData()
            );
        }

        return body;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handleOrderNotFound(OrderNotFoundException exception) {

        return new ApiResponse(
                LocalDateTime.now(),
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                false
        );
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse handlePaymentAlreadyExists(
            PaymentAlreadyExistsException exception
    ) {

        return new ApiResponse(
                LocalDateTime.now(),
                exception.getMessage(),
                HttpStatus.CONFLICT.value(),
                false
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        return new ApiResponse(
                LocalDateTime.now(),
                message,
                HttpStatus.BAD_REQUEST.value(),
                false
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse handleGeneralException(Exception exception) {

        return new ApiResponse(
                LocalDateTime.now(),
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                false
        );
    }
}