package com.maicard.core.config;

import com.maicard.mb.constants.MbConstants;
import com.maicard.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
@ConditionalOnProperty(name="MQ_ENABLED",havingValue = "true")
public class RabbitConfig {

    @Value("${systemCode}")
    String systemCode;

    @Value("${systemServerId}")
    private int serverId;

    @Primary
    @Bean(name = "rabbitConnectionFactory")
    public ConnectionFactory rabbitConnectionFactory(
            @Value("${spring.rabbitmq.host}") String host,
            @Value("${spring.rabbitmq.port}") int port,
            @Value("${spring.rabbitmq.username}") String username,
            @Value("${spring.rabbitmq.password}") String password ) {
        return connectionFactory(host, port, username, password);
    }

    @Bean
    public FanoutExchange getExchange() {
        final String fanoutName = getExchangeName();
        log.info("Register exchange:" + fanoutName);
        return new FanoutExchange(fanoutName,true, false);
    }



    @Bean
    public Queue getSystemQueue(){
        return new Queue(getQueueName());
    }

    private String getQueueName() {
        return MbConstants.QUEUE_NAME + "-" + serverId;
    }
    private String getExchangeName(){
        return MbConstants.EXCHANGE_NAME + "-" + systemCode;
    }

    @Bean
    public Binding getSystemBind(){
        log.info("Binding queue to exchange...");
        return BindingBuilder.bind(getSystemQueue()).to(getExchange());
    }

    @Bean(name = "rabbitTemplate")
    public RabbitTemplate rabbitTemplate(
            @Qualifier("rabbitConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(JsonUtils.getSerializeInstance()));
        rabbitTemplate.setExchange(getExchangeName());
        log.info("Initialize rabbitmq...");
        return rabbitTemplate ;
    }

    @Bean(name = "txRabbitConnectionFactory")
    public ConnectionFactory txRabbitConnectionFactory(
            @Value("${spring.txrabbitmq.host}") String host,
            @Value("${spring.txrabbitmq.port}") int port,
            @Value("${spring.txrabbitmq.username}") String username,
            @Value("${spring.txrabbitmq.password}") String password ) {
        return connectionFactory(host, port, username, password );
    }

    @Bean(name = "txRabbitTemplate")
    public RabbitTemplate txRabbitTemplate(
            @Qualifier("txRabbitConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(JsonUtils.getSerializeInstance()));
        rabbitTemplate.setExchange(getExchangeName());
        log.info("Initialize tx rabbitmq...");
        return rabbitTemplate ;
    }

    public CachingConnectionFactory connectionFactory(String host, int port, String username,
                                                      String password ) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }
}
