package com.shopease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Main application entry point for ShopEase.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ShopEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopEaseApplication.class, args);
    }
}
