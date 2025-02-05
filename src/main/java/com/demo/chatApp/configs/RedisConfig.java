package com.demo.chatApp.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;

@Configuration
public class RedisConfig {
    @Bean
    public RedisConnectionFactory connectionFactory() {
        return new JedisConnectionFactory();  // Or Lettuce depending on your choice
    }

    @Bean
    public RedisMessageListenerContainer messageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new StringMessageConverter();
    }
}
