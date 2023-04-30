package org.billing.hrs;

import org.billing.data.repositories.ReportRepository;
import org.billing.hrs.services.HrsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HrsApplicationTests {
    @Autowired
    private HrsService service;
    @Autowired
    private ReportRepository repository;
    @Test
    void contextLoads() {

    }

}
