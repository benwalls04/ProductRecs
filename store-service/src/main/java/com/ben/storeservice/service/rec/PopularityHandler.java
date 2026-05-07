package com.ben.storeservice.service.rec;

import com.ben.storeservice.model.Product;
import com.ben.storeservice.model.ProductRec;
import com.ben.storeservice.model.RecType;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PopularityHandler implements RecTypeHandler {

    @Override
    public RecType getSupportedType() {
        return RecType.POPULARITY;
    }

    @Override
    public List<ProductRec> recommend(int n, Product clickedProduct, List<Product> products) {
        return products.stream()
                .filter(p -> p.getRating() != null)
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
