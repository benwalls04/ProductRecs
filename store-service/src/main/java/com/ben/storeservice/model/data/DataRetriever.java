package com.ben.storeservice.model.data;

import com.ben.storeservice.model.Product;

import java.util.List;

public interface DataRetriever {
    List<Product> getAll();
}
