package com.ben.rec_service.model;

import lombok.*;

@Data
public class Product {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private Double rating;
}
