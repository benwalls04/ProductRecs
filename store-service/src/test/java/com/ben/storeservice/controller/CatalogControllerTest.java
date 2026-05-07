package com.ben.storeservice.controller;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.service.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private CatalogController catalogController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(catalogController).build();
    }

    @Test
    void getAllProducts_returns200WithProducts() throws Exception {
        // no catalog arg to avoid circular reference during JSON serialization
        Product product = TestFixtures.product("Laptop", 999.99);
        when(catalogService.getAllProducts(1))
                .thenReturn(new ResponseEntity<>(List.of(product), HttpStatus.OK));

        mockMvc.perform(get("/catalog/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(999.99));
    }

    @Test
    void getAllProducts_returns404_whenCatalogNotFound() throws Exception {
        when(catalogService.getAllProducts(99))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/catalog/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProduct_returns200WithProduct() throws Exception {
        Product product = TestFixtures.product("Laptop", 999.99);
        when(catalogService.getProduct(1, 10))
                .thenReturn(new ResponseEntity<>(product, HttpStatus.OK));

        mockMvc.perform(get("/catalog/1/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getProduct_returns404_whenNotFound() throws Exception {
        when(catalogService.getProduct(1, 99))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/catalog/1/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCatalog_returns201_whenStoreExists() throws Exception {
        when(catalogService.createCatalog(1))
                .thenReturn(new ResponseEntity<>("Catalog created", HttpStatus.CREATED));

        mockMvc.perform(post("/catalog/1"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Catalog created"));
    }

    @Test
    void createCatalog_returns404_whenStoreNotFound() throws Exception {
        when(catalogService.createCatalog(99))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/catalog/99"))
                .andExpect(status().isNotFound());
    }
}