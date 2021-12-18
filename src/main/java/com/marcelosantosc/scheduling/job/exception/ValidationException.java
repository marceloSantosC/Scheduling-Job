package com.marcelosantosc.scheduling.job.exception;

import com.marcelosantosc.scheduling.job.model.ValidationResult;
import lombok.Getter;

import java.util.List;

public class ValidationException extends RuntimeException {

    @Getter
    private final List<String> messages;

    public ValidationException(ValidationResult result) {
        super("Existem erros de validação");
        this.messages = result.getMessages();
    }
}
