package com.ben.storeservice.service;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.Page;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.PageRepo;
import com.ben.storeservice.repo.StoreRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {

    @Mock private PageRepo pageRepo;
    @Mock private StoreRepo storeRepo;

    private PageService pageService;

    @BeforeEach
    void setUp() {
        pageService = new PageService(pageRepo, storeRepo);
    }

    private Store storeWithId(int id) {
        Store store = TestFixtures.dummyJsonStore();
        store.setId(id);
        return store;
    }

    private Page pageForStore(int storeId) {
        Page page = TestFixtures.page(storeWithId(storeId));
        page.setId(10);
        return page;
    }

    // --- createPage ---

    @Test
    void createPage_returns201_whenStoreExists() {
        Store store = storeWithId(1);
        when(storeRepo.findById(1)).thenReturn(Optional.of(store));

        Page page = new Page();
        page.setName("Home");

        ResponseEntity<String> response = pageService.createPage(1, page);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Created");
        verify(pageRepo).save(page);
    }

    @Test
    void createPage_throws404_whenStoreNotFound() {
        when(storeRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pageService.createPage(99, new Page()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    // --- getAllPages ---

    @Test
    void getAllPages_returnsEmptyList_whenStoreHasNoPages() {
        when(pageRepo.findAllByStoreId(1)).thenReturn(List.of());

        ResponseEntity<List<Page>> response = pageService.getAllPages(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getAllPages_returns200WithList() {
        Store store = storeWithId(1);
        List<Page> pages = List.of(TestFixtures.page("Home", store));
        when(pageRepo.findAllByStoreId(1)).thenReturn(pages);

        ResponseEntity<List<Page>> response = pageService.getAllPages(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pages);
    }

    // --- getPage ---

    @Test
    void getPage_returns200_whenFoundAndStoreMatches() {
        Page page = pageForStore(1);
        when(pageRepo.findById(10)).thenReturn(Optional.of(page));

        ResponseEntity<Page> response = pageService.getPage(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
    }

    @Test
    void getPage_returns404_whenNotFound() {
        when(pageRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(pageService.getPage(1, 99).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPage_returns404_whenStoreMismatch() {
        Page page = pageForStore(2);
        when(pageRepo.findById(10)).thenReturn(Optional.of(page));

        assertThat(pageService.getPage(1, 10).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- updatePage ---

    @Test
    void updatePage_returns404_whenNotFound() {
        when(pageRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(pageService.updatePage(1, 99, new Page()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updatePage_returns403_whenStoreMismatch() {
        Page page = pageForStore(2);
        when(pageRepo.findById(10)).thenReturn(Optional.of(page));

        assertThat(pageService.updatePage(1, 10, new Page()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updatePage_returns200_andUpdatesName() {
        Page page = pageForStore(1);
        page.setName("Old Name");
        when(pageRepo.findById(10)).thenReturn(Optional.of(page));

        Page update = new Page();
        update.setName("New Name");

        ResponseEntity<String> response = pageService.updatePage(1, 10, update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Success");
        assertThat(page.getName()).isEqualTo("New Name");
        verify(pageRepo).save(page);
    }

    @Test
    void updatePage_skipsNullName() {
        Page page = pageForStore(1);
        page.setName("Original");
        when(pageRepo.findById(10)).thenReturn(Optional.of(page));

        pageService.updatePage(1, 10, new Page());

        assertThat(page.getName()).isEqualTo("Original");
    }

    // --- deletePage ---

    @Test
    void deletePage_returns404_whenNotFound() {
        when(pageRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(pageService.deletePage(1, 99).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePage_returns403_whenStoreMismatch() {
        Page page = pageForStore(2);
        when(pageRepo.findById(10)).thenReturn(Optional.of(page));

        assertThat(pageService.deletePage(1, 10).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deletePage_returns200_andDeletes() {
        Page page = pageForStore(1);
        when(pageRepo.findById(10)).thenReturn(Optional.of(page));

        ResponseEntity<String> response = pageService.deletePage(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Deleted");
        verify(pageRepo).deleteById(10);
    }
}
