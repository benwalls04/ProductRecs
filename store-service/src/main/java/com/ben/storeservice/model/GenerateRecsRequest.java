package com.ben.storeservice.model;

import lombok.Data;

import java.util.List;

@Data
public class GenerateRecsRequest {
    private List<Integer> recIds;
    private Product clickedProduct;
}
