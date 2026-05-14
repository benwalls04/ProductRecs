package com.ben.rec_service.model;

import lombok.Data;

@Data
public class ProductRec {
    private Product product;
    private double score;
}
