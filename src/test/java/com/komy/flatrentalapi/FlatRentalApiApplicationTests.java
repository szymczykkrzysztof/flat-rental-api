package com.komy.flatrentalapi;

import com.komy.flatrentalapi.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlatRentalApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
