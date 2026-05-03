package com.ben.storeservice.controller;

import com.ben.storeservice.model.Product;
import com.ben.storeservice.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;
    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
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
