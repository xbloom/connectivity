package com.vsvz.connectivity.config;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.spring.template.SpringTemplateLoader;
import de.neuland.jade4j.spring.view.JadeViewResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * Created by liusijin on 16/2/27.
 */
@EnableWebMvc
@Configuration
@ComponentScan(basePackages = {"com.vsvz.connectivity.webmvc","com.vsvz.connectivity.service"})
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    Environment env;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        boolean devMode = true;
        Integer cachePeriod = devMode ? 0 : 31556926;

        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/")
                .setCachePeriod(cachePeriod);
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {

        JadeViewResolver jadeResolver = new JadeViewResolver();
        JadeConfiguration jadeConf = new JadeConfiguration();
        jadeConf.setCaching(false);
        jadeConf.setPrettyPrint(true);
        jadeConf.setTemplateLoader(getJadeLoader());
        jadeResolver.setConfiguration(jadeConf);
        jadeResolver.setRequestContextAttribute("rc");
        jadeResolver.setOrder(0);
        registry.viewResolver(jadeResolver);


        InternalResourceViewResolver jspResolver = new InternalResourceViewResolver();
        jspResolver.setViewClass(JstlView.class);
        jspResolver.setPrefix("/WEB-INF/views/");
        jspResolver.setSuffix(".jsp");
        jspResolver.setRequestContextAttribute("rc");
        jspResolver.setOrder(10);
        registry.viewResolver(jspResolver);

    }

    @Bean
    public SpringTemplateLoader getJadeLoader(){
        SpringTemplateLoader templateLoader = new SpringTemplateLoader();
        templateLoader.setBasePath("/WEB-INF/views/");
        templateLoader.setEncoding("UTF-8");
        templateLoader.setSuffix(".jade");
        return templateLoader;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/chatter/**");
        registry.addMapping("/user/**");
        registry.addMapping("/contact/**");
    }
}
