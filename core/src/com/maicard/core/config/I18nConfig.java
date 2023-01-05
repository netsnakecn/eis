package com.maicard.core.config;

import com.maicard.core.constants.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class I18nConfig {

    @Bean("messageSource")
    public ResourceBundleMessageSource getMessageSource(){
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setDefaultEncoding(Constants.DEFAULT_CHARSET);
        resourceBundleMessageSource.setBasename("classpath:pconfig/message");
        resourceBundleMessageSource.setUseCodeAsDefaultMessage(true);
        return resourceBundleMessageSource;
    }
}
