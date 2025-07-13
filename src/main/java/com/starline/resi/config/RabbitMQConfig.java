package com.starline.resi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    // Exchange names
    public static final String RESI_EXCHANGE = "resi.exchange";


    // Queue names
    public static final String RESI_SUCCESS_QUEUE = "resi.success.queue";


    // Routing keys
    public static final String RESI_SUCCESS_ADD_ROUTING_KEY = "resi.success.added";


    // Exchanges
    @Bean
    public TopicExchange resiExchange() {
        return new TopicExchange(RESI_EXCHANGE);
    }


    // Queues
    @Bean
    public Queue resiSuccessQueue() {
        return QueueBuilder.durable(RESI_SUCCESS_QUEUE)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }


    // Bindings
    @Bean
    public Binding resiSuccessBinding() {
        return BindingBuilder
                .bind(resiSuccessQueue())
                .to(resiExchange())
                .with(RESI_SUCCESS_ADD_ROUTING_KEY);
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}

