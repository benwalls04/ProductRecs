package com.ben.storeservice.controller;

import com.ben.storeservice.model.Catalog;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;
    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping("/{storeId}")
    public ResponseEntity<String> createCatalog(@PathVariable Integer storeId) {
        return catalogService.createCatalog(storeId);
    }

    @GetMapping("/{catalogId}")
    public ResponseEntity<List<Product>> getAllProducts(@PathVariable Integer catalogId) {
        return catalogService.getAllProducts(catalogId);
    }

    @GetMapping("/{catalogId}/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer catalogId, @PathVariable Integer productId) {
        return catalogService.getProduct(catalogId, productId);
    }
}
