package org.billing.cdr;

import org.billing.cdr.services.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication(scanBasePackages = {"org.billing.data", "org.billing.cdr"})
public class CdrApplication {
    @Autowired
    GeneratorService generatorService;

    public static void main(String[] args) {
//        TODO remove context closing
        SpringApplication.run(CdrApplication.class, args).close();
    }

    @Bean
    public CommandLineRunner runner(){
        return runner -> {
            File file = generatorService.generateCDRFile();
            System.out.println(file.getPath());
        };
    }
}
