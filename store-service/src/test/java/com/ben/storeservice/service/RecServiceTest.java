package com.ben.storeservice.service;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.*;
import com.ben.storeservice.model.GenerateRecsRequest;
import com.ben.storeservice.repo.PageRepo;
import com.ben.storeservice.repo.ProductRepo;
import com.ben.storeservice.repo.RecModuleRepo;
import com.ben.storeservice.service.rec.CategoryHandler;
import com.ben.storeservice.service.rec.PopularityHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecServiceTest {

    @Mock private RecModuleRepo recModuleRepo;
    @Mock private ProductRepo productRepo;
    @Mock private PageRepo pageRepo;

    private RecService recService;

    @BeforeEach
    void setUp() {
        recService = new RecService(recModuleRepo, productRepo, pageRepo,
                List.of(new PopularityHandler(), new CategoryHandler()));
    }

    private Store storeWithId(int id) {
        Store store = TestFixtures.dummyJsonStore();
        store.setId(id);
        return store;
    }

    private Store storeWithIdAndCatalog(int id) {
        Store store = storeWithId(id);
        Catalog catalog = TestFixtures.catalog(store);
        store.setCatalog(catalog);
        return store;
    }

    private Page pageForStore(Store store) {
        Page page = new Page();
        page.setStore(store);
        return page;
    }

    private Page pageWithId(int id, Store store) {
        Page page = new Page();
        page.setId(id);
        page.setStore(store);
        return page;
    }

    private RecModule recModuleForStore(int storeId) {
        Store store = storeWithId(storeId);
        RecModule rec = TestFixtures.recModule("Top Picks", 5, pageForStore(store));
        rec.setId(10);
        return rec;
    }

    // --- getAllRecs ---

    @Test
    void getAllRecs_returnsEmptyList_whenStoreHasNoModules() {
        when(recModuleRepo.findAllByStoreId(1)).thenReturn(List.of());

        ResponseEntity<List<RecModule>> response = recService.getAllRecs(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getAllRecs_returns200WithList() {
        Store store = storeWithId(1);
        List<RecModule> recs = List.of(TestFixtures.recModule("Top Picks", 5, pageForStore(store)));
        when(recModuleRepo.findAllByStoreId(1)).thenReturn(recs);

        ResponseEntity<List<RecModule>> response = recService.getAllRecs(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(recs);
    }

    // --- createRec ---

    @Test
    void createRec_returns201_whenPageExists() {
        Store store = storeWithId(1);
        Page page = new Page();
        page.setId(5);
        page.setStore(store);
        when(pageRepo.findById(5)).thenReturn(Optional.of(page));

        RecModule rec = new RecModule();
        rec.setName("Top Picks");
        rec.setN(5);

        ResponseEntity<String> response = recService.createRec(1, 5, rec);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Created");
        verify(recModuleRepo).save(rec);
    }

    @Test
    void createRec_throws404_whenPageNotFound() {
        when(pageRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recService.createRec(1, 99, new RecModule()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void createRec_throws404_whenPageBelongsToDifferentStore() {
        Store otherStore = storeWithId(2);
        Page page = new Page();
        page.setId(5);
        page.setStore(otherStore);
        when(pageRepo.findById(5)).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> recService.createRec(1, 5, new RecModule()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    // --- getRec ---

    @Test
    void getRec_returns200_whenFoundAndStoreMatches() {
        RecModule rec = recModuleForStore(1);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        ResponseEntity<RecModule> response = recService.getRec(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(rec);
    }

    @Test
    void getRec_returns404_whenNotFound() {
        when(recModuleRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(recService.getRec(1, 99).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getRec_returns404_whenStoreMismatch() {
        RecModule rec = recModuleForStore(2);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        assertThat(recService.getRec(1, 10).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- generateRecs ---

    @Test
    void generateRecs_returns200_withModulesOnPage() {
        Store store = storeWithIdAndCatalog(1);
        Catalog catalog = store.getCatalog();
        Page page = pageWithId(1, store);
        RecModule rec = TestFixtures.recModule("Top Picks", 2, page);
        rec.setId(10);

        Product p1 = TestFixtures.product("Laptop", 999.99, "desc", "electronics", 4.8, catalog);
        Product p2 = TestFixtures.product("Phone", 499.99, "desc", "electronics", 4.2, catalog);

        when(recModuleRepo.findAllByPageId(1)).thenReturn(List.of(rec));
        when(productRepo.findAllByCatalog(catalog)).thenReturn(List.of(p1, p2));

        GenerateRecsRequest request = new GenerateRecsRequest();
        request.setPageId(1);

        ResponseEntity<Map<Integer, RecModule>> response = recService.generateRecs(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey(10);
        assertThat(response.getBody().get(10).getItems()).hasSize(2);
    }

    @Test
    void generateRecs_returnsEmptyMap_whenPageHasNoModules() {
        when(recModuleRepo.findAllByPageId(99)).thenReturn(List.of());

        GenerateRecsRequest request = new GenerateRecsRequest();
        request.setPageId(99);

        ResponseEntity<Map<Integer, RecModule>> response = recService.generateRecs(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void generateRecs_sortsPopularityByRatingDesc() {
        Store store = storeWithIdAndCatalog(1);
        Catalog catalog = store.getCatalog();
        Page page = pageWithId(1, store);
        RecModule rec = TestFixtures.recModule("Top Picks", 1, page);
        rec.setId(10);

        Product low  = TestFixtures.product("Low",  10.0, "d", "cat", 2.0, catalog);
        Product high = TestFixtures.product("High", 20.0, "d", "cat", 5.0, catalog);

        when(recModuleRepo.findAllByPageId(1)).thenReturn(List.of(rec));
        when(productRepo.findAllByCatalog(catalog)).thenReturn(List.of(low, high));

        GenerateRecsRequest request = new GenerateRecsRequest();
        request.setPageId(1);
        Map<Integer, RecModule> body = recService.generateRecs(request).getBody();

        assertThat(body.get(10).getItems().get(0).getProduct().getName()).isEqualTo("High");
    }

    @Test
    void generateRecs_categoryHandler_filtersCorrectly() {
        Store store = storeWithIdAndCatalog(1);
        Catalog catalog = store.getCatalog();
        Page page = pageWithId(1, store);
        RecModule rec = TestFixtures.recModule("Electronics", 5, RecType.CATEGORY, page);
        rec.setId(10);

        Product match   = TestFixtures.product("Laptop", 999.99, "d", "electronics", 4.5, catalog);
        Product noMatch = TestFixtures.product("Shirt",  29.99,  "d", "clothing",    4.9, catalog);

        when(recModuleRepo.findAllByPageId(1)).thenReturn(List.of(rec));
        when(productRepo.findAllByCatalog(catalog)).thenReturn(List.of(match, noMatch));

        Product clickedProduct = TestFixtures.product("Widget", 50.0, "d", "electronics", 4.0);
        GenerateRecsRequest request = new GenerateRecsRequest();
        request.setPageId(1);
        request.setClickedProduct(clickedProduct);

        List<ProductRec> items = recService.generateRecs(request).getBody().get(10).getItems();

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getProduct().getName()).isEqualTo("Laptop");
    }

    @Test
    void generateRecs_fallsBackToPopularity_whenResultsAreScarce() {
        Store store = storeWithIdAndCatalog(1);
        Catalog catalog = store.getCatalog();
        Page page = pageWithId(1, store);
        RecModule rec = TestFixtures.recModule("Electronics", 3, RecType.CATEGORY, page);
        rec.setId(10);

        Product categoryMatch = TestFixtures.product("Laptop", 999.99, "d", "electronics", 4.5, catalog);
        categoryMatch.setId(1);
        Product popular1 = TestFixtures.product("Phone", 499.99, "d", "clothing", 4.8, catalog);
        popular1.setId(2);
        Product popular2 = TestFixtures.product("Watch", 299.99, "d", "clothing", 4.6, catalog);
        popular2.setId(3);

        when(recModuleRepo.findAllByPageId(1)).thenReturn(List.of(rec));
        when(productRepo.findAllByCatalog(catalog)).thenReturn(List.of(categoryMatch, popular1, popular2));

        Product clickedProduct = TestFixtures.product("Widget", 50.0, "d", "electronics", 4.0);
        GenerateRecsRequest request = new GenerateRecsRequest();
        request.setPageId(1);
        request.setClickedProduct(clickedProduct);

        List<ProductRec> items = recService.generateRecs(request).getBody().get(10).getItems();

        assertThat(items).hasSize(3);
        assertThat(items.get(0).getProduct().getName()).isEqualTo("Laptop");   // category match
        assertThat(items.get(1).getProduct().getName()).isEqualTo("Phone");    // fallback: highest rated
        assertThat(items.get(2).getProduct().getName()).isEqualTo("Watch");    // fallback: second highest
    }

    @Test
    void generateRecs_doesNotFallBack_whenResultsMeetN() {
        Store store = storeWithIdAndCatalog(1);
        Catalog catalog = store.getCatalog();
        Page page = pageWithId(1, store);
        RecModule rec = TestFixtures.recModule("Electronics", 2, RecType.CATEGORY, page);
        rec.setId(10);

        Product match1 = TestFixtures.product("Laptop", 999.99, "d", "electronics", 4.5, catalog);
        match1.setId(1);
        Product match2 = TestFixtures.product("Tablet", 599.99, "d", "electronics", 4.3, catalog);
        match2.setId(2);
        Product match3 = TestFixtures.product("Phone",  499.99, "d", "electronics", 4.1, catalog);
        match3.setId(3);

        when(recModuleRepo.findAllByPageId(1)).thenReturn(List.of(rec));
        when(productRepo.findAllByCatalog(catalog)).thenReturn(List.of(match1, match2, match3));

        Product clickedProduct = TestFixtures.product("Widget", 50.0, "d", "electronics", 4.0);
        GenerateRecsRequest request = new GenerateRecsRequest();
        request.setPageId(1);
        request.setClickedProduct(clickedProduct);

        List<ProductRec> items = recService.generateRecs(request).getBody().get(10).getItems();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getProduct().getName()).isEqualTo("Laptop");
        assertThat(items.get(1).getProduct().getName()).isEqualTo("Tablet");
    }

    // --- updateRec ---

    @Test
    void updateRec_returns404_whenNotFound() {
        when(recModuleRepo.findById(99)).thenReturn(Optional.empty());

        RecModule update = new RecModule();
        update.setName("New Name");

        assertThat(recService.updateRec(1, 99, update).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateRec_returns403_whenStoreMismatch() {
        RecModule rec = recModuleForStore(2);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        RecModule update = new RecModule();
        update.setName("New Name");

        assertThat(recService.updateRec(1, 10, update).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updateRec_returns200_andUpdatesAllFields() {
        RecModule rec = recModuleForStore(1);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        RecModule update = new RecModule();
        update.setName("Updated Name");
        update.setN(10);
        update.setRecType(RecType.POPULARITY);

        ResponseEntity<String> response = recService.updateRec(1, 10, update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Success");
        assertThat(rec.getName()).isEqualTo("Updated Name");
        assertThat(rec.getN()).isEqualTo(10);
        assertThat(rec.getRecType()).isEqualTo(RecType.POPULARITY);
        verify(recModuleRepo).save(rec);
    }

    @Test
    void updateRec_skipsNullFields() {
        RecModule rec = recModuleForStore(1);
        rec.setName("Original");
        rec.setN(5);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        recService.updateRec(1, 10, new RecModule());

        assertThat(rec.getName()).isEqualTo("Original");
        assertThat(rec.getN()).isEqualTo(5);
    }

    // --- deleteRec ---

    @Test
    void deleteRec_returns404_whenNotFound() {
        when(recModuleRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(recService.deleteRec(1, 99).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteRec_returns403_whenStoreMismatch() {
        RecModule rec = recModuleForStore(2);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        assertThat(recService.deleteRec(1, 10).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteRec_returns200_andDeletes() {
        RecModule rec = recModuleForStore(1);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        ResponseEntity<String> response = recService.deleteRec(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Deleted");
        verify(recModuleRepo).deleteById(10);
    }
}
