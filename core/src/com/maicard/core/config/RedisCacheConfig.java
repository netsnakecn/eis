package com.maicard.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@Slf4j
public class RedisCacheConfig extends CachingConfigurerSupport {

    private final RedisConnectionFactory factory;

    /**
     * 此处使用的lettuce连接池
     * factory初始化位置在 LettuceConnectionConfiguration
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {

        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 序列化器
        template.setValueSerializer(serializer());
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 缓存管理器
     * -> 使用redis做cache缓存注解管理
     * -> 修改原生的序列化方式改为json方式
     */
    @Override
    @Bean
    @Primary
    public RedisCacheManager cacheManager() {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer()));
        log.info("Initialize spring redis cache...");
        return RedisCacheManager.builder(factory).cacheDefaults(cacheConfiguration).build();
    }

    /**
     * json序列化器
     */
    private Jackson2JsonRedisSerializer<Object> serializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = JsonUtils.getSerializeInstance();
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }
}