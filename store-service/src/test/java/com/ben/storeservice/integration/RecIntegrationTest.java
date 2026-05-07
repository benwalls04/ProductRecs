package com.ben.storeservice.integration;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.RecModule;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.RecModuleRepo;
import com.ben.storeservice.repo.StoreRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecIntegrationTest {

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
    private RecModuleRepo recModuleRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createRec_persistsToDatabase() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());

        mockMvc.perform(post("/rec/" + store.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Top Picks\", \"n\": 5, \"recType\": \"POPULARITY\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Created"));

        assertThat(recModuleRepo.findAllByStoreId(store.getId())).hasSize(1);
        assertThat(recModuleRepo.findAllByStoreId(store.getId()).get(0).getName()).isEqualTo("Top Picks");
    }

    @Test
    void createRec_returns404_whenStoreNotFound() throws Exception {
        mockMvc.perform(post("/rec/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Top Picks\", \"n\": 5, \"recType\": \"POPULARITY\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRecs_returnsAllModulesForStore() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        recModuleRepo.save(TestFixtures.recModule("Top Picks", 5, store));
        recModuleRepo.save(TestFixtures.recModule("New Arrivals", 10, store));

        mockMvc.perform(get("/rec/" + store.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Top Picks"))
                .andExpect(jsonPath("$[1].name").value("New Arrivals"));
    }

    @Test
    void getAllRecs_returnsEmptyList_whenStoreHasNoModules() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());

        mockMvc.perform(get("/rec/" + store.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getRec_returns200_whenFoundAndStoreMatches() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        RecModule rec = recModuleRepo.save(TestFixtures.recModule("Top Picks", 5, store));

        mockMvc.perform(get("/rec/" + store.getId() + "/" + rec.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Top Picks"))
                .andExpect(jsonPath("$.n").value(5));
    }

    @Test
    void getRec_returns404_whenStoreMismatch() throws Exception {
        Store store1 = storeRepo.save(TestFixtures.dummyJsonStore());
        Store store2 = storeRepo.save(TestFixtures.dummyJsonStore());
        RecModule rec = recModuleRepo.save(TestFixtures.recModule("Top Picks", 5, store2));

        mockMvc.perform(get("/rec/" + store1.getId() + "/" + rec.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateRecs_returns200_withPopulatedItems() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        com.ben.storeservice.repo.CatalogRepo catalogRepo = webApplicationContext
                .getBean(com.ben.storeservice.repo.CatalogRepo.class);
        com.ben.storeservice.repo.ProductRepo productRepo = webApplicationContext
                .getBean(com.ben.storeservice.repo.ProductRepo.class);

        com.ben.storeservice.model.Catalog catalog = catalogRepo.save(TestFixtures.catalog(store));
        store.setCatalog(catalog);
        productRepo.save(TestFixtures.product("Laptop", 999.99, "desc", "electronics", 4.8, catalog));

        RecModule rec = recModuleRepo.save(TestFixtures.recModule("Top Picks", 5, store));

        mockMvc.perform(post("/rec/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recIds\": [" + rec.getId() + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + rec.getId() + ".name").value("Top Picks"))
                .andExpect(jsonPath("$." + rec.getId() + ".items[0].product.name").value("Laptop"));
    }

    @Test
    void generateRecs_skipsUnknownIds() throws Exception {
        mockMvc.perform(post("/rec/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recIds\": [9999]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.9999").doesNotExist());
    }

    @Test
    void updateRec_persistsChangesToDatabase() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        RecModule rec = recModuleRepo.save(TestFixtures.recModule("Top Picks", 5, store));

        mockMvc.perform(put("/rec/" + store.getId() + "/" + rec.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Best Sellers\", \"n\": 10}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));

        RecModule updated = recModuleRepo.findById(rec.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Best Sellers");
        assertThat(updated.getN()).isEqualTo(10);
    }

    @Test
    void updateRec_returns403_whenStoreMismatch() throws Exception {
        Store store1 = storeRepo.save(TestFixtures.dummyJsonStore());
        Store store2 = storeRepo.save(TestFixtures.dummyJsonStore());
        RecModule rec = recModuleRepo.save(TestFixtures.recModule("Top Picks", 5, store2));

        mockMvc.perform(put("/rec/" + store1.getId() + "/" + rec.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Name\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteRec_removesFromDatabase() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        RecModule rec = recModuleRepo.save(TestFixtures.recModule("Top Picks", 5, store));

        mockMvc.perform(delete("/rec/" + store.getId() + "/" + rec.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));

        assertThat(recModuleRepo.findById(rec.getId())).isEmpty();
    }

    @Test
    void deleteRec_returns403_whenStoreMismatch() throws Exception {
        Store store1 = storeRepo.save(TestFixtures.dummyJsonStore());
        Store store2 = storeRepo.save(TestFixtures.dummyJsonStore());
        RecModule rec = recModuleRepo.save(TestFixtures.recModule("Top Picks", 5, store2));

        mockMvc.perform(delete("/rec/" + store1.getId() + "/" + rec.getId()))
                .andExpect(status().isForbidden());

        assertThat(recModuleRepo.findById(rec.getId())).isPresent();
    }
}