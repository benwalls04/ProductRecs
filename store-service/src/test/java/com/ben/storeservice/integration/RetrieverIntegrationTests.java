package com.ben.storeservice.integration;

import com.ben.storeservice.model.Product;
import com.ben.storeservice.model.data.DataRetriever;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Retriever Integration Tests")
class RetrieverIntegrationTests {

    @Autowired
    private DataRetriever dummyRetriever;

    @Test
    @DisplayName("getAll() returns a non-empty list")
    public void dummyRetrieverGetAll_returnsEntries() {
        List<Product> result = dummyRetriever.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("First product has no null fields")
    public void dummyRetrieverGetAll_firstProductHasNoNullFields() {
        List<Product> result = dummyRetriever.getAll();

        assertThat(!result.isEmpty());
        Product first = result.get(0);

        assertThat(first.getName()).isNotNull();
        assertThat(first.getDescription()).isNotNull();
        assertThat(first.getCategory()).isNotNull();
        assertThat(first.getRating()).isNotNull();
        assertThat(first.getPrice()).isNotNull();
    }
}
