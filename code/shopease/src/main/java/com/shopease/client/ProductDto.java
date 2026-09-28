package com.shopease.client;

import lombok.*;

/**
 * DTO for product data from the Product service.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String name;
    private Long mrp;
    private Long sellingPrice;
    private Integer stockQty;
    private String status;
    private String variantName;
}
