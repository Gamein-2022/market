package org.gamein.marketservergamein2022;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@SpringBootApplication(scanBasePackages = {"org.gamein.marketservergamein2022"})
@EnableWebMvc
public class BackendMarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendMarketApplication.class, args);
    }
}