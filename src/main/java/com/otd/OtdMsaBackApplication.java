package com.otd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "com.otd.configuration.feignclient")
@SpringBootApplication(scanBasePackages = {
        "com.otd",
        "com.otd.configuration",
        "com.otd.otd_user",
        "com.otd.otd_pointshop",
        "com.otd.otd_pointShop",
        "com.otd.otd_challenge",
        "com.otd.otd_admin"
        }
)
@ComponentScan(basePackages = {
        "com.otd",
        "com.otd.configuration",
        "com.otd.otd_user",
        "com.otd.otd_challenge",
        "com.otd.otd_pointshop",
        "com.otd.otd_admin"
})
@EntityScan(basePackages = {
        "com.otd.otd_user.entity",
        "com.otd.otd_challenge.entity",
        "com.otd.otd_pointshop.entity",
        "com.otd.otd_admin.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.otd.otd_user.application",
        "com.otd.otd_challenge.application.challenge.Repository",
        "com.otd.otd_pointshop.repository",
        "com.otd.otd_admin.application.admin.Repository"
})
public class OtdMsaBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(OtdMsaBackApplication.class, args);
    }
}