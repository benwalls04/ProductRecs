package com.ben.storeservice.model.data;

import com.ben.storeservice.model.Product;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DummyRetriever implements DataRetriever {

    private final RestClient restClient;

    public DummyRetriever() {
        this.restClient = RestClient.create("https://dummyjson.com");
    }

    @Override
    public List<Product> getAll() {
        DummyJsonResponse response = restClient.get()
                .uri("/products?limit=0")
                .retrieve()
                .body(DummyJsonResponse.class);

        if (response == null || response.getProducts() == null) {
            return List.of();
        }

        return response.getProducts().stream().map(this::mapToProduct).collect(Collectors.toList());
    }

    private Product mapToProduct(DummyJsonProduct dp) {
        Product product = new Product();
        product.setName(dp.getTitle());
        product.setDescription(dp.getDescription());
        product.setCategory(dp.getCategory());
        product.setRating(dp.getRating());
        product.setPrice(dp.getPrice());
        return product;
    }
}
