package com.ben.storeservice.service;

import com.ben.storeservice.model.RecModule;
import com.ben.storeservice.repo.RecModuleRepo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecService {

    private final RecModuleRepo recModuleRepo;

    public RecService(RecModuleRepo recModuleRepo) {
        this.recModuleRepo = recModuleRepo;
    }

    public ResponseEntity<List<RecModule>> getAllRecs(Integer storeId) {
        return new ResponseEntity<>(recModuleRepo.findAllByStoreId(storeId), HttpStatus.OK);
    }

    public ResponseEntity<RecModule> getRec(Integer storeId, Integer recId) {
        return recModuleRepo.findById(recId)
                .filter(rec -> rec.getStore().getId().equals(storeId))
                .map(rec -> new ResponseEntity<>(rec, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<List<RecModule>> generateRecs(List<Integer> recIds) {
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_IMPLEMENTED);
    }

    @Transactional
    public ResponseEntity<String> updateRec(Integer storeId, Integer recId, RecModule newRec) {
        Optional<RecModule> rec = recModuleRepo.findById(recId);
        if (rec.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        RecModule oldRec = rec.get();

        if (!oldRec.getStore().getId().equals(storeId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (newRec.getName() != null)
            oldRec.setName(newRec.getName());
        if (newRec.getN() != null)
            oldRec.setN(newRec.getN());
        if (newRec.getRecType() != null)
            oldRec.setRecType(newRec.getRecType());

        recModuleRepo.save(oldRec);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<String> deleteRec(Integer storeId, Integer recId) {
        Optional<RecModule> recOpt = recModuleRepo.findById(recId);
        if (recOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        RecModule rec = recOpt.get();

        if (!rec.getStore().getId().equals(storeId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        recModuleRepo.deleteById(recId);
        return new ResponseEntity<>("Deleted", HttpStatus.OK);
    }
}
