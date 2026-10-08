package com.hainam.worksphere;

import me.paulschwarz.springdotenv.DotenvConfig;
import me.paulschwarz.springdotenv.DotenvPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorksphereApplication {

    public static void main(String[] args) {
        // Set timezone mặc định cho JVM là UTC (chuẩn microservice)
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));

        SpringApplication app = new SpringApplication(WorksphereApplication.class);

        // Configure dotenv
        app.addInitializers(applicationContext -> {
            DotenvConfig config = new DotenvConfig(new java.util.Properties());
            DotenvPropertySource propertySource = new DotenvPropertySource(config);
            applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
        });

        app.run(args);
    }
}
