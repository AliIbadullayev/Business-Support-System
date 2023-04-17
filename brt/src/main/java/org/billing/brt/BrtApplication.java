package org.billing.brt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.billing.data", "org.billing.brt"})
public class BrtApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrtApplication.class, args);
    }

}
