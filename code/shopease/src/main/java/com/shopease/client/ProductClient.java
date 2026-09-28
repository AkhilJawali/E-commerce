package com.shopease.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Feign client for Product service.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@FeignClient(name = "product-service", url = "${product-service.url:http://localhost:8081}")
public interface ProductClient {

    /**
     * Get a single product by ID.
     * 
     * @param id the product ID
     * @return the product DTO
     */
    @GetMapping("/api/v1/products/{id}")
    ProductDto getProduct(@PathVariable Long id);

    /**
     * Get multiple products by IDs (batch fetch).
     * 
     * @param ids list of product IDs
     * @return map of product ID to product DTO
     */
    @GetMapping("/api/v1/products/batch")
    Map<Long, ProductDto> getProducts(@RequestParam List<Long> ids);
}
