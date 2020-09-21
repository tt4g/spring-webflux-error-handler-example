package com.github.tt4g.spring.webflux.error.handler.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestController
public class ErrorController {

    @GetMapping("/throwError")
    public Mono<String> throwError() {
        throw new RuntimeException("returnError");
    }

    @GetMapping("/returnError")
    public Mono<String> returnError() {
        return Mono.error(new RuntimeException("returnError"));
    }

    @GetMapping("/pathVariable/{value}")
    public Mono<String> pathVariable(
        @PathVariable(name = "value", required = true) Integer value) {
        return Mono.just("Path variable: " + value);
    }

    @PostMapping("/validationError")
    public Mono<Dummy> validationError(@Validated @RequestBody Dummy dummy) {
        return Mono.just(dummy);
    }

    @PostMapping("/validationErrorWithErrors")
    public Mono<ResponseEntity<Object>> validationErrorWithErrors(
        @Validated @RequestBody Mono<Dummy> dummyMono) {

        return dummyMono
            .<ResponseEntity<Object>>map(dummy ->
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dummy)
            )
            .onErrorResume(
                WebExchangeBindException.class::isInstance,
                ex -> {
                    WebExchangeBindException webExchangeBindException = (WebExchangeBindException) ex;

                    BindingResult bindingResult = webExchangeBindException.getBindingResult();

                    Map<String, List<String>> nameToErrors = new HashMap<>();
                    bindingResult.getAllErrors().forEach(objectError -> {
                        String name =
                            objectError instanceof FieldError ?
                                ((FieldError) objectError).getField() : objectError.getObjectName();

                        List<String> errors = nameToErrors.computeIfAbsent(name, key -> new ArrayList<>());

                        errors.add(objectError.getDefaultMessage());
                    });

                    return Mono.just(
                        ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ValidationErrorResponse(nameToErrors)));
                });
    }

}
