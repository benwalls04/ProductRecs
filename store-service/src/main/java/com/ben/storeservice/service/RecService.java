package com.ben.storeservice.service;

import com.ben.storeservice.model.GenerateRecsRequest;
import com.ben.storeservice.model.Page;
import com.ben.storeservice.model.Product;
import com.ben.storeservice.model.ProductRec;
import com.ben.storeservice.model.RecModule;
import com.ben.storeservice.model.RecType;
import com.ben.storeservice.repo.PageRepo;
import com.ben.storeservice.repo.ProductRepo;
import com.ben.storeservice.repo.RecModuleRepo;
import com.ben.storeservice.service.rec.RecTypeHandler;
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
    private final Map<RecType, RecTypeHandler> handlerMap;

    public RecService(RecModuleRepo recModuleRepo, ProductRepo productRepo, PageRepo pageRepo, List<RecTypeHandler> handlers) {
        this.recModuleRepo = recModuleRepo;
        this.productRepo = productRepo;
        this.pageRepo = pageRepo;
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(RecTypeHandler::getSupportedType, h -> h));
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

            RecTypeHandler handler = handlerMap.get(module.getRecType());
            List<ProductRec> items = handler != null
                    ? handler.recommend(module.getN(), request.getClickedProduct(), products)
                    : List.of();

            if (items.size() < module.getN()) {
                Set<Integer> alreadyIncluded = items.stream()
                        .map(pr -> pr.getProduct().getId())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                List<Product> remaining = products.stream()
                        .filter(p -> !alreadyIncluded.contains(p.getId()))
                        .collect(Collectors.toList());

                int deficit = module.getN() - items.size();
                RecTypeHandler popularityHandler = handlerMap.get(RecType.POPULARITY);
                if (popularityHandler != null) {
                    List<ProductRec> fallback = popularityHandler.recommend(deficit, null, remaining);
                    List<ProductRec> combined = new ArrayList<>(items);
                    combined.addAll(fallback);
                    items = combined;
                }
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
