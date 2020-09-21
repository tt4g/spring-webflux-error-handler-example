package com.github.tt4g.spring.webflux.error.handler.example;

import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

@Configuration
public class WebConfig {

    @Bean
    ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }

    /**
     * Override {@link ErrorWebExceptionHandler}.
     *
     * See Original implementation:
     * {@link ErrorWebFluxAutoConfiguration#errorWebExceptionHandler(org.springframework.boot.web.reactive.error.ErrorAttributes, ResourceProperties, ObjectProvider, ServerCodecConfigurer, ApplicationContext)}.
     *
     * @return {@link ErrorWebExceptionHandler}
     */
    @Bean
    ErrorWebExceptionHandler customErrorWebExceptionHandler(
        ErrorAttributes errorAttributes,
        ResourceProperties resourceProperties,
        ServerProperties serverProperties,
        ObjectProvider<ViewResolver> viewResolvers,
        ServerCodecConfigurer serverCodecConfigurer,
        ApplicationContext applicationContext) {

        CustomErrorWebExceptionHandler customErrorWebExceptionHandler =
            new CustomErrorWebExceptionHandler(
                    errorAttributes,
                    resourceProperties,
                    serverProperties.getError(),
                    applicationContext);
        customErrorWebExceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        customErrorWebExceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        customErrorWebExceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());

        return customErrorWebExceptionHandler;
    }

}
