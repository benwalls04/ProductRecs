package com.ben.rec_service.controller;

import com.ben.rec_service.handler.RecTypeHandler;
import com.ben.rec_service.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecTypeControllerTest {

    @Mock
    private RecTypeHandler popularityHandler;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(popularityHandler.getSupportedType()).thenReturn(RecType.POPULARITY);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RecTypeController(List.of(popularityHandler)))
                .build();
    }

    private RecRequest requestWith(int n, List<Product> products) {
        RecRequest req = new RecRequest();
        req.setN(n);
        req.setProducts(products);
        return req;
    }

    private Product product(String name, double rating) {
        Product p = new Product();
        p.setName(name);
        p.setRating(rating);
        return p;
    }

    @Test
    void returns200_withResultsFromHandler() throws Exception {
        Product p = product("Laptop", 4.5);
        ProductRec rec = new ProductRec();
        rec.setProduct(p);
        rec.setScore(4.5);

        when(popularityHandler.recommend(anyInt(), any(), any())).thenReturn(List.of(rec));

        mockMvc.perform(post("/rec/type/POPULARITY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWith(1, List.of(p)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product.name").value("Laptop"))
                .andExpect(jsonPath("$[0].score").value(4.5));
    }

    @Test
    void returns400_forUnregisteredRecType() throws Exception {
        mockMvc.perform(post("/rec/type/CATEGORY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWith(2, List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns200_withEmptyList_whenHandlerReturnsNothing() throws Exception {
        when(popularityHandler.recommend(anyInt(), any(), any())).thenReturn(List.of());

        mockMvc.perform(post("/rec/type/POPULARITY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWith(5, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void passesClickedProductToHandler() throws Exception {
        Product clicked = product("Phone", 4.0);
        Product p = product("Laptop", 4.9);
        ProductRec rec = new ProductRec();
        rec.setProduct(p);
        rec.setScore(4.9);

        when(popularityHandler.recommend(anyInt(), any(), any())).thenReturn(List.of(rec));

        RecRequest request = new RecRequest();
        request.setN(1);
        request.setClickedProduct(clicked);
        request.setProducts(List.of(p));

        mockMvc.perform(post("/rec/type/POPULARITY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product.name").value("Laptop"));
    }
}
