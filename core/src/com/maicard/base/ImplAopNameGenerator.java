package com.maicard.base;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

public class ImplAopNameGenerator implements BeanNameGenerator{
	  @Override
	    public String generateBeanName(BeanDefinition paramBeanDefinition,  
	            BeanDefinitionRegistry paramBeanDefinitionRegistry) {  
	        String[] strs = paramBeanDefinition.getBeanClassName().split("\\.");  
	        String shortName = strs[strs.length - 1];  	          
	        return ImplNameTranslate.translate(shortName);
	    }  
}
