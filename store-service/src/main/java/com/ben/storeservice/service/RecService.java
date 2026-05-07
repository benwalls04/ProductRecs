package com.ben.storeservice.service;

import com.ben.storeservice.model.GenerateRecsRequest;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.model.RecModule;
import com.ben.storeservice.model.RecType;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.ProductRepo;
import com.ben.storeservice.repo.RecModuleRepo;
import com.ben.storeservice.repo.StoreRepo;
import com.ben.storeservice.service.rec.RecTypeHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecService {

    private final RecModuleRepo recModuleRepo;
    private final ProductRepo productRepo;
    private final StoreRepo storeRepo;
    private final Map<RecType, RecTypeHandler> handlerMap;

    public RecService(RecModuleRepo recModuleRepo, ProductRepo productRepo, StoreRepo storeRepo, List<RecTypeHandler> handlers) {
        this.recModuleRepo = recModuleRepo;
        this.productRepo = productRepo;
        this.storeRepo = storeRepo;
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(RecTypeHandler::getSupportedType, h -> h));
    }

    public ResponseEntity<String> createRec(Integer storeId, RecModule rec) {
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        rec.setStore(store);
        recModuleRepo.save(rec);
        return new ResponseEntity<>("Created", HttpStatus.CREATED);
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

    @Transactional(readOnly = true)
    public ResponseEntity<Map<Integer, RecModule>> generateRecs(GenerateRecsRequest request) {
        Map<Integer, RecModule> result = new LinkedHashMap<>();

        for (Integer recId : request.getRecIds()) {
            Optional<RecModule> opt = recModuleRepo.findById(recId);
            if (opt.isEmpty()) continue;

            RecModule module = opt.get();
            List<Product> products = productRepo.findAllByCatalog(module.getStore().getCatalog());

            RecTypeHandler handler = handlerMap.get(module.getRecType());
            module.setItems(handler != null
                    ? handler.recommend(module.getN(), request.getClickedProduct(), products)
                    : List.of());

            result.put(recId, module);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
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

        if (newRec.getName() != null) oldRec.setName(newRec.getName());
        if (newRec.getN() != null) oldRec.setN(newRec.getN());
        if (newRec.getRecType() != null) oldRec.setRecType(newRec.getRecType());

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
