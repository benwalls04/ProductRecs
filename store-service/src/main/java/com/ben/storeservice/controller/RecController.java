package com.ben.storeservice.controller;

import com.ben.storeservice.model.RecModule;
import com.ben.storeservice.service.RecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rec")
public class RecController {

    private final RecService recService;

    public RecController(RecService recService) {
        this.recService = recService;
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<List<RecModule>> getAllRecs(@PathVariable Integer storeId) {
        return recService.getAllRecs(storeId);
    }

    @GetMapping("/{storeId}/{recId}")
    public ResponseEntity<RecModule> getRec(@PathVariable Integer storeId, @PathVariable Integer recId) {
        return recService.getRec(storeId, recId);
    }

    @PostMapping("/generate")
    public ResponseEntity<List<RecModule>> generateRecs(@RequestBody List<Integer> recIds) {
        return recService.generateRecs(recIds);
    }

    @DeleteMapping("/{storeId}/{recId}")
    public ResponseEntity<String> deleteRec(@PathVariable Integer storeId, @PathVariable Integer recId) {
        return recService.deleteRec(storeId, recId);
    }

    @PutMapping("/{storeId}/{recId}")
    public ResponseEntity<String> updateRec(@PathVariable Integer storeId, @PathVariable Integer recId, @RequestBody RecModule newRec) {
        return recService.updateRec(storeId, recId, newRec);
    }
}
