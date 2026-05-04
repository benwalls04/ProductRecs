package com.ben.storeservice.service;

import com.ben.storeservice.model.Catalog;
import com.ben.storeservice.model.data.DataRetriever;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.repo.CatalogRepo;
import com.ben.storeservice.repo.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CatalogService {
    private final CatalogRepo catalogRepo;
    private final ProductRepo productRepo;
    private final DataRetriever dataRetriever;

    public CatalogService(CatalogRepo catalogRepo, ProductRepo productRepo, DataRetriever dataRetriever) {
        this.catalogRepo = catalogRepo;
        this.productRepo = productRepo;
        this.dataRetriever = dataRetriever;
    }

    public ResponseEntity<List<Product>> getAllProducts(Integer catalogId) {
        Catalog catalog = catalogRepo.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog not found"));


        if (productRepo.findAllByCatalog(catalog).isEmpty() || productRepo.hoursSinceLastCreated(catalogId) >= 24) {
            List<Product> productList = dataRetriever.getAll();
            for (Product p : productList) {
                p.setCatalog(catalog);
            }
            productRepo.saveAll(productList);
            return new ResponseEntity<>(productList, HttpStatus.OK);
        }

        return new ResponseEntity<>(productRepo.findAllByCatalog(catalog), HttpStatus.OK);
    }

    public ResponseEntity<Product> getProduct(Integer catalogId, Integer productId) {
        return productRepo.findById(productId)
                .filter(product -> product.getCatalog().getId().equals(catalogId))
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}