package com.ben.storeservice.service;

import com.ben.storeservice.TestFixtures;
import com.ben.storeservice.model.RecModule;
import com.ben.storeservice.model.RecType;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.RecModuleRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecServiceTest {

    @Mock
    private RecModuleRepo recModuleRepo;

    @InjectMocks
    private RecService recService;

    private Store storeWithId(int id) {
        Store store = TestFixtures.bestBuyStore();
        store.setId(id);
        return store;
    }

    private RecModule recModuleForStore(int storeId) {
        return TestFixtures.recModule("Top Picks", 5, storeWithId(storeId));
    }

    @Test
    void getAllRecs_returns200WithList() {
        Store store = storeWithId(1);
        List<RecModule> recs = List.of(TestFixtures.recModule("Top Picks", 5, store));
        when(recModuleRepo.findAllByStoreId(1)).thenReturn(recs);

        ResponseEntity<List<RecModule>> response = recService.getAllRecs(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(recs);
    }

    @Test
    void getRec_returns200_whenFoundAndStoreMatches() {
        RecModule rec = recModuleForStore(1);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        ResponseEntity<RecModule> response = recService.getRec(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(rec);
    }

    @Test
    void getRec_returns404_whenNotFound() {
        when(recModuleRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(recService.getRec(1, 99).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getRec_returns404_whenStoreMismatch() {
        RecModule rec = recModuleForStore(2); // belongs to store 2
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        assertThat(recService.getRec(1, 10).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void generateRecs_returns501() {
        ResponseEntity<List<RecModule>> response = recService.generateRecs(List.of(1, 2, 3));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    void updateRec_returns404_whenNotFound() {
        when(recModuleRepo.findById(99)).thenReturn(Optional.empty());

        RecModule update = new RecModule();
        update.setName("New Name");

        assertThat(recService.updateRec(1, 99, update).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateRec_returns403_whenStoreMismatch() {
        RecModule rec = recModuleForStore(2);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        RecModule update = new RecModule();
        update.setName("New Name");

        assertThat(recService.updateRec(1, 10, update).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updateRec_returns200_andUpdatesAllFields() {
        RecModule rec = recModuleForStore(1);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        RecModule update = new RecModule();
        update.setName("Updated Name");
        update.setN(10);
        update.setRecType(RecType.POPULARITY);

        ResponseEntity<String> response = recService.updateRec(1, 10, update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Success");
        assertThat(rec.getName()).isEqualTo("Updated Name");
        assertThat(rec.getN()).isEqualTo(10);
        assertThat(rec.getRecType()).isEqualTo(RecType.POPULARITY);
        verify(recModuleRepo).save(rec);
    }

    @Test
    void updateRec_skipsNullFields() {
        RecModule rec = recModuleForStore(1);
        rec.setName("Original");
        rec.setN(5);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        recService.updateRec(1, 10, new RecModule()); // all null fields

        assertThat(rec.getName()).isEqualTo("Original");
        assertThat(rec.getN()).isEqualTo(5);
    }

    @Test
    void deleteRec_returns404_whenNotFound() {
        when(recModuleRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(recService.deleteRec(1, 99).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteRec_returns403_whenStoreMismatch() {
        RecModule rec = recModuleForStore(2);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        assertThat(recService.deleteRec(1, 10).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteRec_returns200_andDeletes() {
        RecModule rec = recModuleForStore(1);
        when(recModuleRepo.findById(10)).thenReturn(Optional.of(rec));

        ResponseEntity<String> response = recService.deleteRec(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Deleted");
        verify(recModuleRepo).deleteById(10);
    }
}