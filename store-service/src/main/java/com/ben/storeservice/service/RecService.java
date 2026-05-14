package com.ben.storeservice.service;

import com.ben.storeservice.feign.RecServiceClient;
import com.ben.storeservice.model.*;
import com.ben.storeservice.repo.PageRepo;
import com.ben.storeservice.repo.ProductRepo;
import com.ben.storeservice.repo.RecModuleRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecService {

    private final RecModuleRepo recModuleRepo;
    private final ProductRepo productRepo;
    private final PageRepo pageRepo;
    private final RecServiceClient recServiceClient;

    public RecService(RecModuleRepo recModuleRepo, ProductRepo productRepo, PageRepo pageRepo,
                      RecServiceClient recServiceClient) {
        this.recModuleRepo = recModuleRepo;
        this.productRepo = productRepo;
        this.pageRepo = pageRepo;
        this.recServiceClient = recServiceClient;
    }

    public ResponseEntity<String> createRec(Integer storeId, Integer pageId, RecModule rec) {
        Page page = pageRepo.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
        if (!page.getStore().getId().equals(storeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
        }
        rec.setPage(page);
        recModuleRepo.save(rec);
        return new ResponseEntity<>("Created", HttpStatus.CREATED);
    }

    public ResponseEntity<List<RecModule>> getAllRecs(Integer storeId) {
        return new ResponseEntity<>(recModuleRepo.findAllByStoreId(storeId), HttpStatus.OK);
    }

    public ResponseEntity<RecModule> getRec(Integer storeId, Integer recId) {
        return recModuleRepo.findById(recId)
                .filter(rec -> rec.getPage().getStore().getId().equals(storeId))
                .map(rec -> new ResponseEntity<>(rec, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<Integer, RecModule>> generateRecs(GenerateRecsRequest request) {
        Map<Integer, RecModule> result = new LinkedHashMap<>();

        for (RecModule module : recModuleRepo.findAllByPageId(request.getPageId())) {
            List<Product> products = productRepo.findAllByCatalog(module.getPage().getStore().getCatalog());

            RecRequest recRequest = new RecRequest();
            recRequest.setN(module.getN());
            recRequest.setClickedProduct(request.getClickedProduct());
            recRequest.setProducts(products);

            List<ProductRec> items = recServiceClient.getRecommendations(module.getRecType(), recRequest);

            if (items.size() < module.getN()) {
                Set<Integer> alreadyIncluded = items.stream()
                        .map(pr -> pr.getProduct().getId())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                List<Product> remaining = products.stream()
                        .filter(p -> !alreadyIncluded.contains(p.getId()))
                        .collect(Collectors.toList());

                int deficit = module.getN() - items.size();
                RecRequest fallbackRequest = new RecRequest();
                fallbackRequest.setN(deficit);
                fallbackRequest.setProducts(remaining);

                List<ProductRec> fallback = recServiceClient.getRecommendations(RecType.POPULARITY, fallbackRequest);
                List<ProductRec> combined = new ArrayList<>(items);
                combined.addAll(fallback);
                items = combined;
            }

            module.setItems(items);
            result.put(module.getId(), module);
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

        if (!oldRec.getPage().getStore().getId().equals(storeId)) {
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

        if (!rec.getPage().getStore().getId().equals(storeId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        recModuleRepo.deleteById(recId);
        return new ResponseEntity<>("Deleted", HttpStatus.OK);
    }
}
