package com.ben.storeservice.service;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.Catalog;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.CatalogRepo;
import com.ben.storeservice.repo.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CatalogRepo catalogRepo;

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void getAllProducts_returns200WithProducts_whenCatalogExists() {
        Store store = TestFixtures.bestBuyStore();
        Catalog catalog = TestFixtures.catalog(store);
        List<Product> products = List.of(
                TestFixtures.product("Laptop", 999.99, catalog),
                TestFixtures.product("Phone", 499.99, catalog)
        );

        when(catalogRepo.existsById(1)).thenReturn(true);
        when(productRepo.findAllByCatalogId(1)).thenReturn(products);

        ResponseEntity<List<Product>> response = catalogService.getAllProducts(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(products);
    }

    @Test
    void getAllProducts_returns404_whenCatalogNotFound() {
        when(catalogRepo.existsById(99)).thenReturn(false);

        ResponseEntity<List<Product>> response = catalogService.getAllProducts(99);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProduct_returns200_whenProductBelongsToCatalog() {
        Store store = TestFixtures.bestBuyStore();
        Catalog catalog = TestFixtures.catalog(store);
        catalog.setId(1);
        Product product = TestFixtures.product("Laptop", 999.99, catalog);

        when(productRepo.findById(10)).thenReturn(Optional.of(product));

        ResponseEntity<Product> response = catalogService.getProduct(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(product);
    }

    @Test
    void getProduct_returns404_whenProductNotFound() {
        when(productRepo.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<Product> response = catalogService.getProduct(1, 99);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProduct_returns404_whenProductBelongsToDifferentCatalog() {
        Store store = TestFixtures.bestBuyStore();
        Catalog catalog = TestFixtures.catalog(store);
        catalog.setId(2); // belongs to catalog 2, not 1
        Product product = TestFixtures.product("Laptop", 999.99, catalog);

        when(productRepo.findById(10)).thenReturn(Optional.of(product));

        ResponseEntity<Product> response = catalogService.getProduct(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}