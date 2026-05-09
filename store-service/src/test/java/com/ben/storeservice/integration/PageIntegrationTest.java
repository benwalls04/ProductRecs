package com.ben.storeservice.integration;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.Page;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.PageRepo;
import com.ben.storeservice.repo.StoreRepo;
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
class PageIntegrationTest {

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
    private PageRepo pageRepo;

    @Test
    void createPage_persistsToDatabase() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());

        mockMvc.perform(post("/page/" + store.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Home\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Created"));

        assertThat(pageRepo.findAllByStoreId(store.getId())).hasSize(1);
        assertThat(pageRepo.findAllByStoreId(store.getId()).get(0).getName()).isEqualTo("Home");
    }

    @Test
    void createPage_returns404_whenStoreNotFound() throws Exception {
        mockMvc.perform(post("/page/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Home\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPages_returnsAllPagesForStore() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        pageRepo.save(TestFixtures.page("Home", store));
        pageRepo.save(TestFixtures.page("Product Detail", store));

        mockMvc.perform(get("/page/" + store.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Home"))
                .andExpect(jsonPath("$[1].name").value("Product Detail"));
    }

    @Test
    void getAllPages_returnsEmptyList_whenStoreHasNoPages() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());

        mockMvc.perform(get("/page/" + store.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getPage_returns200_whenFoundAndStoreMatches() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        Page page = pageRepo.save(TestFixtures.page("Home", store));

        mockMvc.perform(get("/page/" + store.getId() + "/" + page.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Home"));
    }

    @Test
    void getPage_returns404_whenStoreMismatch() throws Exception {
        Store store1 = storeRepo.save(TestFixtures.dummyJsonStore());
        Store store2 = storeRepo.save(TestFixtures.dummyJsonStore());
        Page page = pageRepo.save(TestFixtures.page("Home", store2));

        mockMvc.perform(get("/page/" + store1.getId() + "/" + page.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePage_persistsChangesToDatabase() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        Page page = pageRepo.save(TestFixtures.page("Home", store));

        mockMvc.perform(put("/page/" + store.getId() + "/" + page.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Landing Page\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));

        Page updated = pageRepo.findById(page.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Landing Page");
    }

    @Test
    void updatePage_returns403_whenStoreMismatch() throws Exception {
        Store store1 = storeRepo.save(TestFixtures.dummyJsonStore());
        Store store2 = storeRepo.save(TestFixtures.dummyJsonStore());
        Page page = pageRepo.save(TestFixtures.page("Home", store2));

        mockMvc.perform(put("/page/" + store1.getId() + "/" + page.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePage_removesFromDatabase() throws Exception {
        Store store = storeRepo.save(TestFixtures.dummyJsonStore());
        Page page = pageRepo.save(TestFixtures.page("Home", store));

        mockMvc.perform(delete("/page/" + store.getId() + "/" + page.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));

        assertThat(pageRepo.findById(page.getId())).isEmpty();
    }

    @Test
    void deletePage_returns403_whenStoreMismatch() throws Exception {
        Store store1 = storeRepo.save(TestFixtures.dummyJsonStore());
        Store store2 = storeRepo.save(TestFixtures.dummyJsonStore());
        Page page = pageRepo.save(TestFixtures.page("Home", store2));

        mockMvc.perform(delete("/page/" + store1.getId() + "/" + page.getId()))
                .andExpect(status().isForbidden());

        assertThat(pageRepo.findById(page.getId())).isPresent();
    }
}
