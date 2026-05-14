package com.ben.rec_service.controller;

import com.ben.rec_service.handler.RecTypeHandler;
import com.ben.rec_service.model.ProductRec;
import com.ben.rec_service.model.RecRequest;
import com.ben.rec_service.model.RecType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rec/type")
public class RecTypeController {

    private final Map<RecType, RecTypeHandler> handlerMap;

    public RecTypeController(List<RecTypeHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(RecTypeHandler::getSupportedType, h -> h));
    }

    @PostMapping("/{recType}")
    public ResponseEntity<List<ProductRec>> getRecommendations(
            @PathVariable RecType recType,
            @RequestBody RecRequest request) {

        RecTypeHandler handler = handlerMap.get(recType);
        if (handler == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                handler.recommend(request.getN(), request.getClickedProduct(), request.getProducts()),
                HttpStatus.OK
        );
    }
}
