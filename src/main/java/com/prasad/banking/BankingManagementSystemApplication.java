package com.prasad.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Banking Management System.
 *
 * @SpringBootApplication combines:
 *   - @Configuration       (marks class as bean source)
 *   - @EnableAutoConfiguration (auto-configures Spring based on classpath)
 *   - @ComponentScan      (scans this package and sub-packages for beans)
 */
@SpringBootApplication
public class BankingManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingManagementSystemApplication.class, args);
    }
}
