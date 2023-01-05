package com.maicard.core.config;

import com.maicard.utils.JsonUtils;
import com.maicard.views.FrontJsonView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;


@Slf4j
@Configuration
public class MvcConfig  extends WebMvcConfigurationSupport {

    @Override
    public void configurePathMatch(PathMatchConfigurer config){
        log.info("Register suffix path pattern match.");
        config.setUseTrailingSlashMatch(true);
        config.setUseSuffixPatternMatch(true);
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        MappingJackson2JsonView view = new MappingJackson2JsonView(JsonUtils.getInstance());
        registry.enableContentNegotiation(view);
        registry.enableContentNegotiation(false, view);

    }




/*    @Bean
    public ServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean bean = new ServletRegistrationBean(dispatcherServlet);
        bean.addUrlMappings("*.json");
        return bean;
    }*/

/*
    @Bean
    public ContentNegotiatingViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager, List<ViewResolver> viewResolvers) {

        ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
        viewResolver.setContentNegotiationManager(manager);

        // 设置默认view, default view 每次都会添加到 真正可用的视图列表中, json视图没有对应的ViewResolver
        View jackson2JsonView = new MappingJackson2JsonView();
        viewResolver.setDefaultViews(Collections.singletonList(jackson2JsonView));

        Map<String, MediaType> mapping = Collections.singletonMap("json", MediaType.valueOf("application/json;charset=UTF-8")); //实测需要加上这个，否则之前通过实体属性序列化成json，返回json的方法容易出错。

        MappingMediaTypeFileExtensionResolver extensionsResolver = new MappingMediaTypeFileExtensionResolver(mapping);

        manager.addFileExtensionResolvers(extensionsResolver);

        viewResolver.setViewResolvers(viewResolvers);
        return viewResolver;
    }*/
}
