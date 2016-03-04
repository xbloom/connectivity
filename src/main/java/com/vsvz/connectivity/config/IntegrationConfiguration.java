package com.vsvz.connectivity.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * Created by liusijin on 16/2/27.
 */
@Configuration
@EnableIntegration
@IntegrationComponentScan(basePackageClasses = {IntegrationConfiguration.class})
public class IntegrationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationConfiguration.class);

    @Autowired
    Environment env;

    @Bean
    @Description("暂存队列")
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    @Description("监听MQTT消息写入channel")
    public MessageProducer mqttInbound() {
        logger.debug("{},{},{}",env.getProperty("serverside_message_service_url"),env.getProperty("serverside_app_instance_id"),env.getProperty("serverside_subscrib_topic"));
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        env.getProperty("serverside_message_service_url"),
                        env.getProperty("serverside_app_instance_id"),
                        env.getProperty("serverside_subscrib_topic").split(","));
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());

        return adapter;
    }


    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {

                //@TODO store message and handle duplicate message regard to qos 1
                System.out.println(message.getPayload());
            }
        };
    }

}
