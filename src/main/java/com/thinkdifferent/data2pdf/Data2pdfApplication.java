package com.thinkdifferent.data2pdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableDiscoveryClient
@EnableOpenApi
public class Data2pdfApplication {

    public static void main(String[] args) {
        // Druid监控页面：访问http://127.0.0.1:8080/druid/index.html
        SpringApplication.run(Data2pdfApplication.class, args);
    }

}
