package org.billing.data;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
class DataApplicationTests {

    @Test
    @Sql({"/script.sql"})
    void contextLoads() {

    }

}
