package com.maicard.core.config;

import com.maicard.misc.EncryptPropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class EncProperityConfig {

    @Bean
    public EncryptPropertyPlaceholderConfigurer propertyConfigurer(){
        EncryptPropertyPlaceholderConfigurer encryptPropertyPlaceholderConfigurer  = new EncryptPropertyPlaceholderConfigurer();
        Resource res = new ClassPathResource("application.properties");
        encryptPropertyPlaceholderConfigurer.setLocation(res);
        return encryptPropertyPlaceholderConfigurer;
    }
}
