package com.nurun;

import com.nurun.config.ProviderModeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(ProviderModeProperties.class)
public class NurunApplication {

    public static void main(String[] args) {
        SpringApplication.run(NurunApplication.class, args);
    }

}
