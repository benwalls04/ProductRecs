package com.ben.storeservice.controller;

import com.ben.storeservice.model.Page;
import com.ben.storeservice.service.PageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Mock
    private PageService pageService;

    @InjectMocks
    private PageController pageController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pageController).build();
    }

    private Page simplePage(String name) {
        Page page = new Page();
        page.setName(name);
        return page;
    }

    @Test
    void createPage_returns201() throws Exception {
        when(pageService.createPage(eq(1), any(Page.class)))
                .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        mockMvc.perform(post("/page/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simplePage("Home"))))
                .andExpect(status().isCreated())
                .andExpect(content().string("Created"));
    }

    @Test
    void createPage_returns404_whenStoreNotFound() throws Exception {
        when(pageService.createPage(eq(99), any(Page.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/page/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simplePage("Home"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPages_returns200WithList() throws Exception {
        when(pageService.getAllPages(1))
                .thenReturn(new ResponseEntity<>(List.of(simplePage("Home")), HttpStatus.OK));

        mockMvc.perform(get("/page/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Home"));
    }

    @Test
    void getPage_returns200_whenFound() throws Exception {
        when(pageService.getPage(1, 10))
                .thenReturn(new ResponseEntity<>(simplePage("Home"), HttpStatus.OK));

        mockMvc.perform(get("/page/1/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Home"));
    }

    @Test
    void getPage_returns404_whenNotFound() throws Exception {
        when(pageService.getPage(1, 99))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/page/1/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePage_returns200_onSuccess() throws Exception {
        when(pageService.updatePage(eq(1), eq(10), any(Page.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        mockMvc.perform(put("/page/1/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simplePage("Updated"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
    }

    @Test
    void updatePage_returns404_whenNotFound() throws Exception {
        when(pageService.updatePage(eq(1), eq(99), any(Page.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/page/1/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simplePage("X"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePage_returns200_onSuccess() throws Exception {
        when(pageService.deletePage(1, 10))
                .thenReturn(new ResponseEntity<>("Deleted", HttpStatus.OK));

        mockMvc.perform(delete("/page/1/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }

    @Test
    void deletePage_returns404_whenNotFound() throws Exception {
        when(pageService.deletePage(1, 99))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(delete("/page/1/99"))
                .andExpect(status().isNotFound());
    }
}
