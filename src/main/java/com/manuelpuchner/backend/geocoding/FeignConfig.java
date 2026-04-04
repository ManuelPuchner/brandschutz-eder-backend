package com.manuelpuchner.backend.geocoding;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

// No @Configuration here!
public class FeignConfig {

    @Bean
    public RequestInterceptor nominatimUserAgentInterceptor() {
        return requestTemplate -> requestTemplate
                .header("User-Agent", "com.manuelpuchner.backend/1.0 manuelpuchner@icloud.com")
                .header("Accept-Language", "en");
    }
}