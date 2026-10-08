package com.hainam.worksphere.shared.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.hainam.worksphere.user.mapper",
    "com.hainam.worksphere.auth.mapper"
})
public class MapStructConfig {
}
