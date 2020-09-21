package com.github.tt4g.spring.webflux.error.handler.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class CustomErrorWebExceptionHandler
    extends DefaultErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorWebExceptionHandler.class);

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     *
     * @param errorAttributes the error attributes
     * @param resourceProperties the resources configuration properties
     * @param errorProperties the error configuration properties
     * @param applicationContext the current application context
     */
    public CustomErrorWebExceptionHandler(
        ErrorAttributes errorAttributes,
        ResourceProperties resourceProperties,
        ErrorProperties errorProperties,
        ApplicationContext applicationContext) {

        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        // WARNING: This implementation handles BindingError#getAllErrors() only if it can get it from ErrorAttributes.
        //  However, this handler is never actually called.
        //  This is because org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler
        //  will return the response first.
        //  WebFluxResponseStatusExceptionHandler is provided by
        //  org.springframework.web.reactive.config.WebFluxConfigurationSupport#responseStatusExceptionHandler().
        //
        // NOTE: Override DefaultErrorWebExceptionHandler#getRoutingFunction(ErrorAttributes)
        // NOTE: ErrorAttributes is DefaultErrorAttributes by default.
        //  Override ErrorAttributes if register ErrorAttributes implementation to @Bean.
        //  See: org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration

        // NOTE: Immediately after the call to getRoutingFunction(ErrorAttributes),
        //  RouterFunction generated here is executed.
        return RouterFunctions.route(
                request -> hasBindingErrors(request, errorAttributes),
                request -> this.createErrorResponse(request, errorAttributes))
            .and(super.getRoutingFunction(errorAttributes));
    }

    private boolean hasBindingErrors(ServerRequest request, ErrorAttributes errorAttributes) {
        List<ObjectError> objectErrors = getObjectErrors(request, errorAttributes);

        return !objectErrors.isEmpty();
    }

    private Mono<ServerResponse> createErrorResponse(
        ServerRequest request, ErrorAttributes errorAttributes) {

        LOGGER.debug("handle error.");

        ResponseErrorAttributes responseErrorAttributes =
            createResponseErrorAttributes(request, errorAttributes);

        Map<String, List<String>> nameToErrors =
            responseErrorAttributes.getNameToErrors();

        if (nameToErrors.isEmpty()) {
            LOGGER.debug("The error could not be handled because it is not a validation error.");

            // NOTE: Returns Mono#empty() if cannot create ServerResponse in this route.
            return Mono.empty();
        }

        return ServerResponse.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(nameToErrors);
    }

    private ResponseErrorAttributes createResponseErrorAttributes(
        ServerRequest request, ErrorAttributes errorAttributes) {

        List<ObjectError> objectErrors = getObjectErrors(request, errorAttributes);

        Map<String, List<String>> nameToErrors =
            convertNameToErrors(objectErrors);

        return new ResponseErrorAttributes(nameToErrors);
    }

    private List<ObjectError> getObjectErrors(ServerRequest request, ErrorAttributes errorAttributes) {
        Map<String, Object> attributes =
            errorAttributes.getErrorAttributes(
                request,
                ErrorAttributeOptions.of(
                    Include.BINDING_ERRORS));

        // NOTE: Check org.springframework.boot.web.reactive.error.DefaultErrorAttributes
        //  to know the type of the key and the corresponding object.
        List<ObjectError> objectErrors = (List<ObjectError>) attributes.get("errors");
        if (objectErrors == null) {
            return List.of();
        } else {
            return objectErrors;
        }
    }
    private Map<String, List<String>> convertNameToErrors(
        List<ObjectError> objectErrors) {

        Map<String, List<String>> nameToErrors = new HashMap<>();

        objectErrors.forEach(objectError -> {
            String name =
                objectError instanceof FieldError ?
                    ((FieldError) objectError).getField() : objectError.getObjectName();

            List<String> errors =
                nameToErrors.computeIfAbsent(name, (key) -> new ArrayList<>());

            errors.add(objectError.getDefaultMessage());
        });

        return nameToErrors;
    }

    private static class ResponseErrorAttributes {

        private final Map<String, List<String>> nameToErrors;

        private ResponseErrorAttributes(
            Map<String, List<String>> nameToErrors) {
            this.nameToErrors = nameToErrors == null ? Map.of() : nameToErrors;
        }

        public Map<String, List<String>> getNameToErrors() {
            return this.nameToErrors;
        }

    }

}
