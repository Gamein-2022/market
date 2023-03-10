package org.gamein.marketservergamein2022;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"org.gamein.marketservergamein2022"})
public class BackendMarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendMarketApplication.class, args);
    }
}