package com.thinkdifferent.reportserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import springfox.documentation.oas.annotations.EnableOpenApi;

@EnableDiscoveryClient
@EnableOpenApi
@SpringBootApplication
public class ReportApplication {

    public static void main(String[] args) {
        // Druid监控页面：访问http://127.0.0.1:8080/druid/index.html
        SpringApplication.run(ReportApplication.class, args);
    }

}
