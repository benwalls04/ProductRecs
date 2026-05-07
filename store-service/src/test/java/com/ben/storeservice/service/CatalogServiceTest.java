package com.ben.storeservice.service;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.Catalog;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.model.data.DataRetriever;
import com.ben.storeservice.repo.CatalogRepo;
import com.ben.storeservice.repo.ProductRepo;
import com.ben.storeservice.repo.StoreRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock private CatalogRepo catalogRepo;
    @Mock private ProductRepo productRepo;
    @Mock private StoreRepo storeRepo;
    @Mock private DataRetriever dataRetriever;

    @InjectMocks private CatalogService catalogService;

    // --- getAllProducts ---

    @Test
    void getAllProducts_returns404_whenCatalogNotFound() {
        when(catalogRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getAllProducts(99))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getAllProducts_fetchesFromRetriever_whenCacheIsEmpty() {
        Store store = TestFixtures.dummyJsonStore();
        Catalog catalog = TestFixtures.catalog(store);
        List<Product> fetched = List.of(TestFixtures.product("Laptop", 999.99));

        when(catalogRepo.findById(1)).thenReturn(Optional.of(catalog));
        when(productRepo.findAllByCatalog(catalog)).thenReturn(List.of());
        when(dataRetriever.getAll()).thenReturn(fetched);

        ResponseEntity<List<Product>> response = catalogService.getAllProducts(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(fetched);
        verify(productRepo).deleteByCatalogId(1);
        verify(productRepo).saveAll(fetched);
    }

    @Test
    void getAllProducts_fetchesFromRetriever_whenCacheIsStale() {
        Store store = TestFixtures.dummyJsonStore();
        Catalog catalog = TestFixtures.catalog(store);
        List<Product> existing = List.of(TestFixtures.product("OldProduct", 50.0, catalog));
        List<Product> fetched = List.of(TestFixtures.product("NewProduct", 100.0));

        when(catalogRepo.findById(1)).thenReturn(Optional.of(catalog));
        when(productRepo.findAllByCatalog(catalog)).thenReturn(existing);
        when(productRepo.latestCreatedAt(1)).thenReturn(LocalDateTime.now().minusHours(25));
        when(dataRetriever.getAll()).thenReturn(fetched);

        ResponseEntity<List<Product>> response = catalogService.getAllProducts(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(productRepo).deleteByCatalogId(1);
        verify(productRepo).saveAll(fetched);
    }

    @Test
    void getAllProducts_returnsFromCache_whenCacheIsValid() {
        Store store = TestFixtures.dummyJsonStore();
        Catalog catalog = TestFixtures.catalog(store);
        List<Product> existing = List.of(TestFixtures.product("Laptop", 999.99, catalog));

        when(catalogRepo.findById(1)).thenReturn(Optional.of(catalog));
        when(productRepo.findAllByCatalog(catalog)).thenReturn(existing);
        when(productRepo.latestCreatedAt(1)).thenReturn(LocalDateTime.now().minusHours(1));

        ResponseEntity<List<Product>> response = catalogService.getAllProducts(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(existing);
        verify(productRepo, never()).deleteByCatalogId(any());
        verify(dataRetriever, never()).getAll();
    }

    // --- getProduct ---

    @Test
    void getProduct_returns200_whenProductBelongsToCatalog() {
        Store store = TestFixtures.dummyJsonStore();
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

        assertThat(catalogService.getProduct(1, 99).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProduct_returns404_whenProductBelongsToDifferentCatalog() {
        Store store = TestFixtures.dummyJsonStore();
        Catalog catalog = TestFixtures.catalog(store);
        catalog.setId(2);
        Product product = TestFixtures.product("Laptop", 999.99, catalog);

        when(productRepo.findById(10)).thenReturn(Optional.of(product));

        assertThat(catalogService.getProduct(1, 10).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- createCatalog ---

    @Test
    void createCatalog_returns201_whenStoreExists() {
        Store store = TestFixtures.dummyJsonStore();
        when(storeRepo.findById(1)).thenReturn(Optional.of(store));

        ResponseEntity<String> response = catalogService.createCatalog(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(catalogRepo).save(any(Catalog.class));
    }

    @Test
    void createCatalog_throws404_whenStoreNotFound() {
        when(storeRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.createCatalog(99))
                .isInstanceOf(ResponseStatusException.class);
    }
}
