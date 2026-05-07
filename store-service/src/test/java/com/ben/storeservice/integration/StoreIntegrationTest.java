package com.ben.storeservice.integration;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.model.StoreType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoreIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Autowired
    private StoreRepo storeRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createStore_persistsToDatabase() throws Exception {
        Store store = TestFixtures.dummyJsonStore();

        mockMvc.perform(post("/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(store)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success"));

        assertThat(storeRepo.findAll()).hasSize(1);
        assertThat(storeRepo.findAll().get(0).getStoreName()).isEqualTo("DummyJSON");
        assertThat(storeRepo.findAll().get(0).getStoreType()).isEqualTo(StoreType.DUMMY_JSON);
    }
}