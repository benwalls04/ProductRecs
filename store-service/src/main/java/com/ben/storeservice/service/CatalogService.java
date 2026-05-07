package com.ben.storeservice.service;

import com.ben.storeservice.model.Catalog;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.model.data.DataRetriever;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.repo.CatalogRepo;
import com.ben.storeservice.repo.ProductRepo;
import com.ben.storeservice.repo.StoreRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CatalogService {
    private final CatalogRepo catalogRepo;
    private final ProductRepo productRepo;
    private final DataRetriever dataRetriever;
    private final StoreRepo storeRepo;

    public CatalogService(CatalogRepo catalogRepo, ProductRepo productRepo, StoreRepo storeRepo, DataRetriever dataRetriever) {
        this.catalogRepo = catalogRepo;
        this.productRepo = productRepo;
        this.dataRetriever = dataRetriever;
        this.storeRepo = storeRepo;
    }

    @Transactional
    public ResponseEntity<List<Product>> getAllProducts(Integer catalogId) {
        Catalog catalog = catalogRepo.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog not found"));

        List<Product> existing = productRepo.findAllByCatalog(catalog);
        LocalDateTime latest = existing.isEmpty() ? null : productRepo.latestCreatedAt(catalogId);

        if (latest == null || ChronoUnit.HOURS.between(latest, LocalDateTime.now()) >= 24) {
            productRepo.deleteByCatalogId(catalogId);

            List<Product> productList = dataRetriever.getAll();
            for (Product p : productList) {
                p.setCatalog(catalog);
            }
            productRepo.saveAll(productList);
            return new ResponseEntity<>(productList, HttpStatus.OK);
        }

        return new ResponseEntity<>(existing, HttpStatus.OK);
    }

    public ResponseEntity<Product> getProduct(Integer catalogId, Integer productId) {
        return productRepo.findById(productId)
                .filter(product -> product.getCatalog().getId().equals(catalogId))
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<String> createCatalog(Integer storeId) {
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found with id: " + storeId));

        Catalog catalog = new Catalog();
        catalog.setStore(store);
        catalogRepo.save(catalog);

        return new ResponseEntity<>("Catalog created", HttpStatus.CREATED);
    }
}