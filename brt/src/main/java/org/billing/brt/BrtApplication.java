package org.billing.brt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"org.billing.data", "org.billing.brt"}, exclude = SecurityAutoConfiguration.class)
public class BrtApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrtApplication.class, args);
    }

}
