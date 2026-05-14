package com.ben.rec_service.model;

import lombok.Data;

@Data
public class RecRequest {
    private Integer catalogId;
    private Integer n;
    private Product clickedProduct;
}
