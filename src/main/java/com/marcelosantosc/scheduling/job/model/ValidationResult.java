package com.marcelosantosc.scheduling.job.model;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationResult {

    @Setter
    private boolean valid;
    private final List<String> messages;

    public ValidationResult() {
        this.valid = true;
        this.messages = new ArrayList<>();
    }

    public void addValidation(boolean validationResult, String messageError) {
        this.valid = this.valid && validationResult;
        if (!validationResult) messages.add(messageError);
    }


}
