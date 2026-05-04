package com.ben.storeservice.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DummyJsonProduct {
    private Integer id;
    private String title;
    private String description;
    private String category;
    private Double price;
    private Double rating;
    private List<String> tags;
}

