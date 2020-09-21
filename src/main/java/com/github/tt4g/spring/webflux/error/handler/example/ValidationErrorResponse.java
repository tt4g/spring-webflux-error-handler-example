package com.github.tt4g.spring.webflux.error.handler.example;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;

public class ValidationErrorResponse {

    private final Map<String, List<String>> nameToErrors;

    public ValidationErrorResponse(Map<String, List<String>> nameToErrors) {
        this.nameToErrors = nameToErrors;
    }

    @JsonGetter("errors")
    public Map<String, List<String>> getNameToErrors() {
        return this.nameToErrors;
    }

}
