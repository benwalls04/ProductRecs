package com.ben.storeservice.model;

import lombok.Data;

import java.util.List;

@Data
public class RecRequest {
    private Integer n;
    private Product clickedProduct;
    private List<Product> products;
}
