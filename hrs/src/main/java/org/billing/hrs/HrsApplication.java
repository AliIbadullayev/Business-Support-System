package org.billing.hrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.billing.data", "org.billing.hrs"})
public class HrsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrsApplication.class, args);
    }

}
