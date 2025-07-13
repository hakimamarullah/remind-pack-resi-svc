package com.starline.resi.annotations;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Cacheable(keyGenerator = "pageableKeyGenerator")
public @interface CacheablePage {

    @AliasFor(annotation = Cacheable.class, attribute = "value")
    String[] value() default {};

    @AliasFor(annotation = Cacheable.class, attribute = "condition")
    String condition() default "";

    @AliasFor(annotation = Cacheable.class, attribute = "unless")
    String unless() default "";

    @AliasFor(annotation = Cacheable.class, attribute = "cacheManager")
    String cacheManager() default "";

    @AliasFor(annotation = Cacheable.class, attribute = "cacheResolver")
    String cacheResolver() default "";
}

