package com.maicard.misc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

@Slf4j
public class ApplicationReadyListner implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        EncryptPropertyPlaceholderConfigurer sysProperty = null;
        try{
            sysProperty = event.getApplicationContext().getBean(EncryptPropertyPlaceholderConfigurer.class);
        }catch (Exception e){
            log.error("系统未配置Bean:" + EncryptPropertyPlaceholderConfigurer.class);
        }
        String appName = event.getSpringApplication().getMainApplicationClass().getSimpleName();
        String msg = "+++ " + appName + " server ";
        if (sysProperty != null) {
            msg += sysProperty.getProperty("systemCode") + "#" + sysProperty.getProperty("systemServerId");
        }
        msg += " start up @" + event.getApplicationContext().getEnvironment().getProperty("local.server.port");
        msg += " +++";
        log.info(msg);
    }
}
