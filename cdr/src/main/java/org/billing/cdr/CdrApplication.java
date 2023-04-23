package org.billing.cdr;

import org.billing.cdr.services.GeneratorService;
import org.billing.data.dto.PhoneBalanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication(scanBasePackages = {"org.billing.data", "org.billing.cdr"}, exclude = SecurityAutoConfiguration.class)
public class CdrApplication {
    @Autowired
    GeneratorService generatorService;

    public static void main(String[] args) {
        SpringApplication.run(CdrApplication.class, args).close();

    }

    @Bean
    public CommandLineRunner runner(){
        return runner -> {
            File cdr = generatorService.generateCDRFile();
            System.out.println(cdr.getPath());
            PhoneBalanceDto phoneBalanceDto = generatorService.furtherTariffication(cdr);
            System.out.println(phoneBalanceDto.getPhoneBalances().get(0));
        };
    }
}
