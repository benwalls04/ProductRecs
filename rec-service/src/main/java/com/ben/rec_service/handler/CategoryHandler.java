package com.ben.rec_service.handler;

import com.ben.rec_service.model.Product;
import com.ben.rec_service.model.ProductRec;
import com.ben.rec_service.model.RecType;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryHandler implements RecTypeHandler {

    @Override
    public RecType getSupportedType() {
        return RecType.CATEGORY;
    }

    @Override
    public List<ProductRec> recommend(int n, Product clickedProduct, List<Product> products) {
        if (clickedProduct == null || clickedProduct.getCategory() == null)
            return List.of();

        String category = clickedProduct.getCategory();
        return products.stream()
                .filter(p -> p.getRating() != null)
                .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                .sorted(Comparator.comparingDouble(Product::getRating).reversed())
                .limit(n)
                .map(p -> {
                    ProductRec rec = new ProductRec();
                    rec.setProduct(p);
                    rec.setScore(p.getRating());
                    return rec;
                })
                .collect(Collectors.toList());
    }
}
