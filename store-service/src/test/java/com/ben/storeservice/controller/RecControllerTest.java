package com.ben.storeservice.controller;

import com.ben.storeservice.model.RecModule;
import com.ben.storeservice.model.RecType;
import com.ben.storeservice.service.RecService;
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
class RecControllerTest {

    @Mock
    private RecService recService;

    @InjectMocks
    private RecController recController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recController).build();
    }

    // no store ref to avoid circular reference during JSON serialization
    private RecModule simpleRec(String name, int n) {
        RecModule rec = new RecModule();
        rec.setName(name);
        rec.setN(n);
        rec.setRecType(RecType.POPULARITY);
        return rec;
    }

    @Test
    void createRec_returns201() throws Exception {
        when(recService.createRec(eq(1), eq(5), any(RecModule.class)))
                .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        mockMvc.perform(post("/rec/1?pageId=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simpleRec("Top Picks", 5))))
                .andExpect(status().isCreated())
                .andExpect(content().string("Created"));
    }

    @Test
    void createRec_returns404_whenPageNotFound() throws Exception {
        when(recService.createRec(eq(1), eq(99), any(RecModule.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/rec/1?pageId=99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simpleRec("Top Picks", 5))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRecs_returns200WithList() throws Exception {
        when(recService.getAllRecs(1))
                .thenReturn(new ResponseEntity<>(List.of(simpleRec("Top Picks", 5)), HttpStatus.OK));

        mockMvc.perform(get("/rec/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Top Picks"));
    }

    @Test
    void getRec_returns200_whenFound() throws Exception {
        when(recService.getRec(1, 10))
                .thenReturn(new ResponseEntity<>(simpleRec("Top Picks", 5), HttpStatus.OK));
        mockMvc.perform(get("/rec/1/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Top Picks"));
    }

    @Test
    void getRec_returns404_whenNotFound() throws Exception {
        when(recService.getRec(1, 99))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/rec/1/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateRecs_returns200() throws Exception {
        when(recService.generateRecs(any()))
                .thenReturn(new ResponseEntity<>(java.util.Map.of(), HttpStatus.OK));

        mockMvc.perform(post("/rec/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageId\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateRec_returns200_onSuccess() throws Exception {
        when(recService.updateRec(eq(1), eq(10), any(RecModule.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        mockMvc.perform(put("/rec/1/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simpleRec("New Name", 10))))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
    }

    @Test
    void updateRec_returns404_whenNotFound() throws Exception {
        when(recService.updateRec(eq(1), eq(99), any(RecModule.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/rec/1/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simpleRec("X", 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRec_returns200_onSuccess() throws Exception {
        when(recService.deleteRec(1, 10))
                .thenReturn(new ResponseEntity<>("Deleted", HttpStatus.OK));

        mockMvc.perform(delete("/rec/1/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }

    @Test
    void deleteRec_returns404_whenNotFound() throws Exception {
        when(recService.deleteRec(1, 99))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(delete("/rec/1/99"))
                .andExpect(status().isNotFound());
    }
}