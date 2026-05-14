package com.ben.rec_service.controller;

import com.ben.rec_service.model.*;
import com.ben.rec_service.handler.RecTypeHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rec/type")
public class RecTypeController {

    private final Map<RecType, RecTypeHandler> handlerMap;
    private final ProductRepo productRepo;
    private final CatalogRepo catalogRepo;

    public RecTypeController(List<RecTypeHandler> handlers, ProductRepo productRepo, CatalogRepo catalogRepo) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(RecTypeHandler::getSupportedType, h -> h));
        this.productRepo = productRepo;
        this.catalogRepo = catalogRepo;
    }

    @PostMapping("/{recType}")
    public ResponseEntity<List<ProductRec>> getRecommendations(
            @PathVariable RecType recType,
            @RequestBody RecRequest request) {

        Catalog catalog = catalogRepo.findById(request.getCatalogId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog not found"));

        List<Product> products = productRepo.findAllByCatalog(catalog);

        RecTypeHandler handler = handlerMap.get(recType);
        if (handler == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(handler.recommend(request.getN(), request.getClickedProduct(), products), HttpStatus.OK);
    }
}
