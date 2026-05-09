package com.ben.storeservice.model;

import lombok.Data;

@Data
public class GenerateRecsRequest {
    private Integer pageId;
    private Product clickedProduct;
}
