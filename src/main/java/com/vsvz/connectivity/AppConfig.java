package com.vsvz.connectivity;

import com.vsvz.connectivity.config.IntegrationConfiguration;
import com.vsvz.connectivity.config.MvcConfiguration;
import com.vsvz.connectivity.webmvc.CORSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * Created by liusijin on 16/2/26.
 */

@Configuration
@PropertySource("classpath:message_config.properties")
@Import({IntegrationConfiguration.class})
public class AppConfig implements ApplicationListener, WebApplicationInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    Environment env;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        logger.info(event.getSource()+"===="+event.toString());
    }

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        // 根 context，加载必要的bean
        AnnotationConfigWebApplicationContext rootContext = getRootContext();
        container.addListener(new ContextLoaderListener(rootContext));

        // springmvc context
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(MvcConfiguration.class);
        ServletRegistration.Dynamic dispatcher =
                container.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        FilterRegistration encodeFilter = container.addFilter("CharacterEncodingFilter", CharacterEncodingFilter.class);
        encodeFilter.setInitParameter("encoding", "UTF-8");
        encodeFilter.setInitParameter("forceEncoding", "true");
        encodeFilter.addMappingForUrlPatterns(null, false, "/*");



//        BrokerService brokerService = new BrokerService();
//        brokerService.setBrokerName("in-memory");
//        brokerService.setPersistent(false);
//        brokerService.setUseJmx(false);
//        try {
//            brokerService.addConnector("mqtt+nio://0.0.0.0:1883");
//            brokerService.addConnector("ws://0.0.0.0:1884");
//            brokerService.setUseShutdownHook(true);
//            brokerService.start();
//            brokerService.waitUntilStarted();
//            logger.info("ActiveMQ embedded broker started successfully...");
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }

    }

    private AnnotationConfigWebApplicationContext getRootContext() {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class);
        return rootContext;
    }
}
