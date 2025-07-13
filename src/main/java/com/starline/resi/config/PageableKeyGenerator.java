package com.starline.resi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("pageableKeyGenerator")
@Slf4j
public class PageableKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder key = new StringBuilder("pageable_");
        key.append(method.getName());

        for (Object param : params) {
            if (param instanceof Pageable pageable) {
                key.append(":page=").append(pageable.getPageNumber())
                        .append(":size=").append(pageable.getPageSize());

                if (pageable.getSort().isSorted()) {
                    key.append(":sort=");
                    pageable.getSort().forEach(order ->
                            key.append(order.getProperty())
                                    .append("-").append(order.getDirection()).append(",")
                    );
                }
            } else {
                key.append(":").append(param);
            }
        }

        log.debug("Generated key using PageableKeyGenerator: {}", key);
        return key.toString();
    }
}

