package com.ben.storeservice.integration;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.Catalog;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.CatalogRepo;
import com.ben.storeservice.repo.ProductRepo;
import com.ben.storeservice.repo.StoreRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CatalogIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Autowired
    private StoreRepo storeRepo;

    @Autowired
    private CatalogRepo catalogRepo;

    @Autowired
    private ProductRepo productRepo;

    @Test
    void getAllProducts_returns200WithProductsFromDb() throws Exception {
        Store store = storeRepo.save(TestFixtures.bestBuyStore());
        Catalog catalog = catalogRepo.save(TestFixtures.catalog(store));
        productRepo.save(TestFixtures.product("Laptop", 999.99, catalog));
        productRepo.save(TestFixtures.product("Phone", 499.99, catalog));

        mockMvc.perform(get("/catalog/" + catalog.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Phone"));
    }

    @Test
    void getAllProducts_returns404_whenCatalogDoesNotExist() throws Exception {
        mockMvc.perform(get("/catalog/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProduct_returns200_whenProductBelongsToCatalog() throws Exception {
        Store store = storeRepo.save(TestFixtures.bestBuyStore());
        Catalog catalog = catalogRepo.save(TestFixtures.catalog(store));
        Product product = productRepo.save(TestFixtures.product("Laptop", 999.99, catalog));

        mockMvc.perform(get("/catalog/" + catalog.getId() + "/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    void getProduct_returns404_whenProductDoesNotExist() throws Exception {
        Store store = storeRepo.save(TestFixtures.bestBuyStore());
        Catalog catalog = catalogRepo.save(TestFixtures.catalog(store));

        mockMvc.perform(get("/catalog/" + catalog.getId() + "/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProduct_returns404_whenProductBelongsToDifferentCatalog() throws Exception {
        Store store1 = storeRepo.save(TestFixtures.bestBuyStore());
        Catalog catalog1 = catalogRepo.save(TestFixtures.catalog(store1));

        Store store2 = storeRepo.save(TestFixtures.openFoodStore());
        Catalog catalog2 = catalogRepo.save(TestFixtures.catalog(store2));
        Product product = productRepo.save(TestFixtures.product("Laptop", 999.99, catalog2));

        mockMvc.perform(get("/catalog/" + catalog1.getId() + "/" + product.getId()))
                .andExpect(status().isNotFound());
    }
}