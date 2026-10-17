package com.librarymanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class LibraryManagementApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that Spring application context boots successfully with all beans and configurations
    }
}
