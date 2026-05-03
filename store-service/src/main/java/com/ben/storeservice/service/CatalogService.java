package com.ben.storeservice.service;

import com.ben.storeservice.model.Product;
import com.ben.storeservice.repo.CatalogRepo;
import com.ben.storeservice.repo.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {
    private final CatalogRepo catalogRepo;
    private final ProductRepo productRepo;

    public CatalogService(CatalogRepo catalogRepo, ProductRepo productRepo) {
        this.catalogRepo = catalogRepo;
        this.productRepo = productRepo;
    }

    public ResponseEntity<List<Product>> getAllProducts(Integer catalogId) {
        if (!catalogRepo.existsById(catalogId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(productRepo.findAllByCatalogId(catalogId), HttpStatus.OK);
    }

    public ResponseEntity<Product> getProduct(Integer catalogId, Integer productId) {
        return productRepo.findById(productId)
                .filter(product -> product.getCatalog().getId().equals(catalogId))
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}