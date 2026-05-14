package com.ben.rec_service.handler;

import com.ben.rec_service.model.Product;
import com.ben.rec_service.model.ProductRec;
import com.ben.rec_service.model.RecType;

import java.util.List;

public interface RecTypeHandler {
    RecType getSupportedType();
    List<ProductRec> recommend(int n, Product clickedProduct, List<Product> products);
}
