package com.maicard.misc;

import com.maicard.core.service.impl.ContextUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ApplicationRefreshListner implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ContextUtils.setContext(event.getApplicationContext());

    }
}
