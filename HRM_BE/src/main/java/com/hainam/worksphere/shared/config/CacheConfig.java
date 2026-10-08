package com.hainam.worksphere.shared.config;

import com.hainam.worksphere.shared.cache.FallbackCacheManager;
import com.hainam.worksphere.shared.config.properties.CacheFallbackProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@Slf4j
public class CacheConfig {

    private final CacheFallbackProperties fallbackProperties;

    public static final String USER_CACHE = "users";
    public static final String USER_BY_EMAIL_CACHE = "usersByEmail";
    public static final String ROLE_CACHE = "roles";
    public static final String ROLE_BY_CODE_CACHE = "rolesByCode";
    public static final String PERMISSION_CACHE = "permissions";
    public static final String PERMISSION_BY_CODE_CACHE = "permissionsByCode";
    public static final String USER_ROLES_CACHE = "userRoles";
    public static final String USER_PERMISSIONS_CACHE = "userPermissions";
    public static final String ROLE_PERMISSIONS_CACHE = "rolePermissions";
    public static final String ACTIVE_ROLES_CACHE = "activeRoles";
    public static final String ACTIVE_PERMISSIONS_CACHE = "activePermissions";
    public static final String SYSTEM_ROLES_CACHE = "systemRoles";
    public static final String SYSTEM_PERMISSIONS_CACHE = "systemPermissions";
    public static final String DEPARTMENT_CACHE = "departments";
    public static final String EMPLOYEE_CACHE = "employees";
    public static final String EMPLOYEE_SALARY_CACHE = "employeeSalaries";
    public static final String ATTENDANCE_CACHE = "attendances";
    public static final String WORK_SHIFT_CACHE = "workShifts";
    public static final String CONTRACT_CACHE = "contracts";
    public static final String LEAVE_REQUEST_CACHE = "leaveRequests";
    public static final String PAYROLL_CACHE = "payrolls";
    public static final String INSURANCE_CACHE = "insurances";
    public static final String DEGREE_CACHE = "degrees";
    public static final String RELATIVE_CACHE = "relatives";
    public static final String STORE_CACHE = "stores";

    private GenericJackson2JsonRedisSerializer redisSerializer() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class)
                                .build(),
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                )
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(redisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(redisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean("redisCacheManager")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = getCacheConfigurations(defaultConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisCacheManager redisCacheManager) {
        log.info("Initializing cache manager with fallback enabled: {}", fallbackProperties.isEnabled());
        return new FallbackCacheManager(redisCacheManager, fallbackProperties.isEnabled());
    }

    /**
     * Configure individual cache settings
     */
    private Map<String, RedisCacheConfiguration> getCacheConfigurations(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User cache - 30 minutes TTL
        cacheConfigurations.put(USER_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(USER_BY_EMAIL_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Role cache - 1 hour TTL (roles change less frequently)
        cacheConfigurations.put(ROLE_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(ROLE_BY_CODE_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // Permission cache - 1 hour TTL (permissions change less frequently)
        cacheConfigurations.put(PERMISSION_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(PERMISSION_BY_CODE_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // User roles and permissions cache - 15 minutes TTL (authorization data)
        cacheConfigurations.put(USER_ROLES_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(USER_PERMISSIONS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(ROLE_PERMISSIONS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Active and System cache - 1 hour TTL (rarely changes)
        cacheConfigurations.put(ACTIVE_ROLES_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(ACTIVE_PERMISSIONS_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(SYSTEM_ROLES_CACHE, defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put(SYSTEM_PERMISSIONS_CACHE, defaultConfig.entryTtl(Duration.ofHours(2)));

        // Department cache - 1 hour TTL
        cacheConfigurations.put(DEPARTMENT_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // Employee cache - 30 minutes TTL
        cacheConfigurations.put(EMPLOYEE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Employee salary cache - 30 minutes TTL
        cacheConfigurations.put(EMPLOYEE_SALARY_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Attendance cache - 10 minutes TTL
        cacheConfigurations.put(ATTENDANCE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Work shift cache - 1 hour TTL
        cacheConfigurations.put(WORK_SHIFT_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // Contract cache - 30 minutes TTL
        cacheConfigurations.put(CONTRACT_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Leave request cache - 15 minutes TTL
        cacheConfigurations.put(LEAVE_REQUEST_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Payroll cache - 30 minutes TTL
        cacheConfigurations.put(PAYROLL_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Insurance cache - 30 minutes TTL
        cacheConfigurations.put(INSURANCE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Degree cache - 30 minutes TTL
        cacheConfigurations.put(DEGREE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Relative cache - 30 minutes TTL
        cacheConfigurations.put(RELATIVE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Store cache - 1 hour TTL
        cacheConfigurations.put(STORE_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        return cacheConfigurations;
    }

    /**
     * Fallback cache manager that disables caching when Redis is unavailable
     */
    @Bean("noOpCacheManager")
    @ConditionalOnProperty(name = "app.cache.fallback.enabled", havingValue = "false")
    public CacheManager noOpCacheManager() {
        log.warn("Cache fallback is disabled. Application will fail if Redis is unavailable.");
        return new org.springframework.cache.support.NoOpCacheManager();
    }
}

