package com.ben.rec_service;

import com.ben.rec_service.model.Product;
import com.ben.rec_service.model.ProductRec;

public class TestFixtures {

    public static Product product(String name, Double price) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        return p;
    }

    public static Product product(String name, Double price, String description, String category, Double rating) {
        Product p = product(name, price);
        p.setDescription(description);
        p.setCategory(category);
        p.setRating(rating);
        return p;
    }

    public static ProductRec productRec(Product product, double score) {
        ProductRec rec = new ProductRec();
        rec.setProduct(product);
        rec.setScore(score);
        return rec;
    }
}
