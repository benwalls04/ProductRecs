package com.ben.storeservice.service;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.StoreRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepo storeRepo;

    @InjectMocks
    private StoreService storeService;

    @Test
    void createStore_savesStoreAndReturns201() {
        Store store = TestFixtures.dummyJsonStore();

        ResponseEntity<String> response = storeService.createStore(store);

        verify(storeRepo).save(store);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Success");
    }
}