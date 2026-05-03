package com.ben.storeservice.service;

import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.StoreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final StoreRepo storeRepo;
    public StoreService(StoreRepo storeRepo) {
        this.storeRepo = storeRepo;
    }

    public ResponseEntity<String> createStore(Store store) {
        storeRepo.save(store);
        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }
}
