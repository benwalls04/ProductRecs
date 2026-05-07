package com.ben.storeservice.service.rec;

import com.ben.storeservice.model.Product;
import com.ben.storeservice.model.ProductRec;
import com.ben.storeservice.model.RecType;

import java.util.List;

public interface RecTypeHandler {
    RecType getSupportedType();
    List<ProductRec> recommend(int n, Product clickedProduct, List<Product> products);
}
