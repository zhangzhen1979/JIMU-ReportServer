package com.thinkdifferent.data2pdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableOpenApi
public class Data2pdfApplication {

    public static void main(String[] args) {
        SpringApplication.run(Data2pdfApplication.class, args);
    }

}
