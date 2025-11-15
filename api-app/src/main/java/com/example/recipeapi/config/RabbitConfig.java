package com.example.recipeapi.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

// Demo Rabbit Config to handle the Data Transfer and stuff
@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "recipes.exchange";
    public static final String QUEUE = "recipes.publish.queue";
    public static final String ROUTING_KEY = "recipes.publish";

    @Bean
    public Exchange recipesExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue recipesQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding binding(Queue recipesQueue, Exchange recipesExchange) {
        return BindingBuilder.bind(recipesQueue).to(recipesExchange).with(ROUTING_KEY).noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
