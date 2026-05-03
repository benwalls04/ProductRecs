package com.ben.storeservice.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString(exclude = "catalog")
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id")
    private Catalog catalog;
}
