package com.ecommerce.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the User microservice.
 *
 * @SpringBootApplication bundles three annotations:
 *   - @Configuration      (this class can declare beans)
 *   - @EnableAutoConfiguration (Spring Boot wires up Tomcat, JPA, JSON, etc.)
 *   - @ComponentScan      (finds @RestController/@Service/@Repository below this package)
 */
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
