package com.prasad.banking;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base annotation — Spring Boot loads the full application context for tests.
 * Individual test classes import from this or use @ExtendWith(MockitoExtension.class)
 * for pure unit tests that don't need the Spring context.
 */
@SpringBootTest
class BankingManagementSystemApplicationTests {

    // Tests run when you execute: mvn test
    // This verifies the Spring application context loads correctly.
    void contextLoads() {
    }
}
