package com.ben.storeservice.service;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.feign.RecServiceClient;
import com.ben.storeservice.model.*;
import com.ben.storeservice.repo.PageRepo;
import com.ben.storeservice.repo.ProductRepo;
import com.ben.storeservice.repo.RecModuleRepo;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecServiceTest {

    @Mock private RecModuleRepo recModuleRepo;
    @Mock private ProductRepo productRepo;
    @Mock private PageRepo pageRepo;
    @Mock private RecServiceClient recServiceClient;

    private RecService recService;

    @BeforeEach
    void setUp() {
        recService = new RecService(recModuleRepo, productRepo, pageRepo, recServiceClient);
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
        Page page = pageWithId(5, store);
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
        Page page = pageWithId(5, otherStore);
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
    void generateRecs_delegatesToRecServiceAndReturnsItems() {
        Store store = storeWithIdAndCatalog(1);
        Page page = pageWithId(1, store);
        RecModule module = TestFixtures.recModule("Top Picks", 2, page);
        module.setId(10);

        Product p1 = TestFixtures.product("Laptop", 999.0, "d", "electronics", 4.8, store.getCatalog());
        p1.setId(1);
        Product p2 = TestFixtures.product("Phone", 499.0, "d", "electronics", 4.2, store.getCatalog());
        p2.setId(2);

        ProductRec rec1 = TestFixtures.productRec(p1, 4.8);
        ProductRec rec2 = TestFixtures.productRec(p2, 4.2);

        when(recModuleRepo.findAllByPageId(1)).thenReturn(List.of(module));
        when(productRepo.findAllByCatalog(store.getCatalog())).thenReturn(List.of(p1, p2));
        when(recServiceClient.getRecommendations(eq(RecType.POPULARITY), any())).thenReturn(List.of(rec1, rec2));

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
    void generateRecs_callsFallback_whenResultsAreScarce() {
        Store store = storeWithIdAndCatalog(1);
        Page page = pageWithId(1, store);
        RecModule module = TestFixtures.recModule("Electronics", 3, RecType.CATEGORY, page);
        module.setId(10);

        Product categoryMatch = TestFixtures.product("Laptop", 999.0, "d", "electronics", 4.5, store.getCatalog());
        categoryMatch.setId(1);
        Product popular = TestFixtures.product("Phone", 499.0, "d", "clothing", 4.8, store.getCatalog());
        popular.setId(2);

        ProductRec categoryRec  = TestFixtures.productRec(categoryMatch, 4.5);
        ProductRec fallbackRec  = TestFixtures.productRec(popular, 4.8);

        when(recModuleRepo.findAllByPageId(1)).thenReturn(List.of(module));
        when(productRepo.findAllByCatalog(store.getCatalog())).thenReturn(List.of(categoryMatch, popular));
        when(recServiceClient.getRecommendations(eq(RecType.CATEGORY), any())).thenReturn(List.of(categoryRec));
        when(recServiceClient.getRecommendations(eq(RecType.POPULARITY), any())).thenReturn(List.of(fallbackRec));

        GenerateRecsRequest request = new GenerateRecsRequest();
        request.setPageId(1);
        request.setClickedProduct(TestFixtures.product("Widget", 50.0, "d", "electronics", 4.0));

        List<ProductRec> items = recService.generateRecs(request).getBody().get(10).getItems();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getProduct().getName()).isEqualTo("Laptop");
        assertThat(items.get(1).getProduct().getName()).isEqualTo("Phone");
    }

    @Test
    void generateRecs_doesNotCallFallback_whenResultsMeetN() {
        Store store = storeWithIdAndCatalog(1);
        Page page = pageWithId(1, store);
        RecModule module = TestFixtures.recModule("Top Picks", 2, page);
        module.setId(10);

        Product p1 = TestFixtures.product("A", 10.0, "d", "cat", 4.5, store.getCatalog());
        p1.setId(1);
        Product p2 = TestFixtures.product("B", 20.0, "d", "cat", 4.0, store.getCatalog());
        p2.setId(2);

        when(recModuleRepo.findAllByPageId(1)).thenReturn(List.of(module));
        when(productRepo.findAllByCatalog(store.getCatalog())).thenReturn(List.of(p1, p2));
        when(recServiceClient.getRecommendations(eq(RecType.POPULARITY), any()))
                .thenReturn(List.of(TestFixtures.productRec(p1, 4.5), TestFixtures.productRec(p2, 4.0)));

        GenerateRecsRequest request = new GenerateRecsRequest();
        request.setPageId(1);

        recService.generateRecs(request);

        verify(recServiceClient, times(1)).getRecommendations(any(), any());
    }

    // --- updateRec ---

    @Test
    void updateRec_returns404_whenNotFound() {
        when(recModuleRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(recService.updateRec(1, 99, new RecModule()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateRec_returns403_whenStoreMismatch() {
        RecModule rec = recModuleForStore(2);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        assertThat(recService.updateRec(1, 10, new RecModule()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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
