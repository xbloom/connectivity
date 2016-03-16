package com.vsvz.connectivity.config;

import com.vsvz.connectivity.model.JSONMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.*;

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
    @Description("B/C通信暂存队列")
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @Description("监听MQTT消息写入channel")
    public MessageProducer mqttInbound() {
        logger.debug("{},{},{}", env.getProperty("serverside_message_service_url"), env.getProperty("serverside_app_instance_id"), env.getProperty("serverside_subscrib_topic"));
        DefaultMqttPahoClientFactory fac = new DefaultMqttPahoClientFactory();
        fac.setCleanSession(false);
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        env.getProperty("serverside_message_service_url"),
                        env.getProperty("serverside_listener_instance_id"),
                        fac,
                        env.getProperty("serverside_subscrib_topic").split(","));
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInboundChannel());

        return adapter;
    }

    @Bean
    @Transformer(inputChannel = "mqttInboundChannel",outputChannel = "mqttInputChannel")
    public JsonToObjectTransformer toMessageTransformer() {
        return new JsonToObjectTransformer(JSONMessage.class){
            Logger handlerLogger = LoggerFactory.getLogger(IntegrationConfiguration.class.getPackage().getName()+".JsonToObjectTransformer for mqttInboundChannel");
            @Override
            protected Object doTransform(Message<?> message) throws Exception {
                try{
                    JSONMessage object = (JSONMessage)super.doTransform(message);
                    return this.getMessageBuilderFactory()
                            .withPayload(message.getPayload())
                            .copyHeaders(message.getHeaders())
                            .setHeaderIfAbsent("JSONMessage",object)
                            .build();
                }catch (Exception e){
                    if(handlerLogger.isDebugEnabled()){
                        handlerLogger.debug("not a JSONMessage, avoid transform for '{}'",message.getPayload());
                    }
                    return message;
                }
            }
        };
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {

            Logger handlerLogger = LoggerFactory.getLogger(IntegrationConfiguration.class.getPackage().getName()+".IntegrationConfiguration.MessageHandler for mqttInputChannel");
            private String INSERT_LOG_SQL = "insert into tm_message_log(client,msg_from,msg_to,msg_type,body,create_ts,duplicate,msg) values(?,?,?,?,?,FROM_UNIXTIME(? * 0.001),?,?)";

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {

                //@TODO store message and handle duplicate message regard to qos 1
                String topic = (String)message.getHeaders().get(MqttHeaders.TOPIC);
                Boolean duplicated = (Boolean)message.getHeaders().get(MqttHeaders.DUPLICATE);
                JSONMessage obj = (JSONMessage)message.getHeaders().get("JSONMessage");
                if(obj!=null){
                    jdbcTemplate.update(INSERT_LOG_SQL,
                            obj.getHeader().getClient(),
                            obj.getHeader().getFrom(),//user id
                            obj.getHeader().getTo(),//user id
                            obj.getHeader().getType(),
                            obj.getBody()==null?null:obj.getBody().toString(),
                            obj.getHeader().getTimestamp(),//create time in client side
                            duplicated,
                            message.getPayload());//JSONMessage string format
                    if(handlerLogger.isDebugEnabled()){
                        handlerLogger.debug("write JSON message, with payload {}", message.getPayload());
                    }
                } else {
                    jdbcTemplate.update(INSERT_LOG_SQL,
                            "unknown",
                            "unknown",//fix me
                            topic,//user id
                            null,
                            null,
                            message.getHeaders().get(MessageHeaders.TIMESTAMP),// server side time
                            duplicated,
                            message.getPayload());
                    if(handlerLogger.isDebugEnabled())
                        handlerLogger.debug("write unknown message of topic:{}, with payload {}", topic, message.getPayload());
                }
            }
        };
    }

    @MessagingGateway(defaultRequestChannel = "jsonObjectSendChannel")
    public interface MyGateway {
        @Gateway(requestChannel = "jsonObjectSendChannel", headers = @GatewayHeader(name = MqttHeaders.TOPIC, expression="#args[0].header.to"))
        void sendMsg(JSONMessage msg);
    }

    @Bean
    @Transformer(inputChannel = "jsonObjectSendChannel", outputChannel = "rawMqttOutboundChannel")
    public ObjectToJsonTransformer toJSONTransformer() {
        return new ObjectToJsonTransformer(){
            protected Object doTransform(Message<?> message) throws Exception {
                MessageHeaders headers = message.getHeaders();
                Long timestamp = headers.getTimestamp();
                JSONMessage obj = (JSONMessage) message.getPayload();
                obj.getHeader().setTimestamp(timestamp);

//                headers.put(MqttHeaders.TOPIC,obj.getHeader().getTo());
                return super.doTransform(message);
            }
        };
    }

    @Bean
    public MessageChannel rawMqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "rawMqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(
                        env.getProperty("serverside_publish_instance_id"), mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(1);
        messageHandler.setDefaultTopic("opr.1001");
        return messageHandler;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(env.getProperty("serverside_message_service_url"));
        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "rawMqttOutboundChannel")
    public LoggingHandler loggingHandler() {
        return new LoggingHandler("DEBUG");
    }

}
