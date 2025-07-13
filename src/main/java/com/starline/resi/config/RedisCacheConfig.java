package com.starline.resi.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Configuration(proxyBeanMethods = false)
@Slf4j
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final RedisCacheProp redisCacheProp;
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisCacheProp.getHost(), redisCacheProp.getPort());
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().build();
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        var genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        genericJackson2JsonRedisSerializer.configure(this::configureObjectMapper);
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer));
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        log.info("Setup Courier cache with TTL of {} hours", redisCacheProp.getCourierTtlHours());
        cacheConfigs.put("courier", baseConfig.entryTtl(Duration.ofHours(redisCacheProp.getCourierTtlHours())));

        log.info("Setup Resi cache with TTL of {} minutes", redisCacheProp.getResiTtlMinutes());
        cacheConfigs.put("resi", baseConfig.entryTtl(Duration.ofMinutes(redisCacheProp.getResiTtlMinutes())));

        return builder -> builder
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(cacheConfigs);
    }


    private void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.findAndRegisterModules()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setTimeZone(TimeZone.getDefault());
    }

}
