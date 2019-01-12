package com.schumskich.cryptotrickstr.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriTemplateHandler;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(CryptoCompareConfig cryptoCompareConfig) {
        DefaultUriTemplateHandler defaultUriTemplateHandler = new DefaultUriTemplateHandler();
        defaultUriTemplateHandler.setBaseUrl(cryptoCompareConfig.getUrl());
        return new RestTemplateBuilder()
                .uriTemplateHandler(defaultUriTemplateHandler)
                .build();
    }

}
