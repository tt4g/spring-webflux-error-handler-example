package com.github.tt4g.spring.webflux.error.handler.example;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dummy {

    @NotNull
    private final String name;

    @JsonCreator
    public Dummy(@JsonProperty(value = "name", required = false) String name) {
        this.name = name;
    }

    @JsonGetter(value = "name")
    public String getName() {
        return this.name;
    }
}
