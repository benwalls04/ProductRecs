package com.ben.storeservice.controller;

import com.ben.storeservice.model.Page;
import com.ben.storeservice.service.PageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/page")
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @PostMapping("/{storeId}")
    public ResponseEntity<String> createPage(@PathVariable Integer storeId, @RequestBody Page page) {
        return pageService.createPage(storeId, page);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<List<Page>> getAllPages(@PathVariable Integer storeId) {
        return pageService.getAllPages(storeId);
    }

    @GetMapping("/{storeId}/{pageId}")
    public ResponseEntity<Page> getPage(@PathVariable Integer storeId, @PathVariable Integer pageId) {
        return pageService.getPage(storeId, pageId);
    }

    @PutMapping("/{storeId}/{pageId}")
    public ResponseEntity<String> updatePage(@PathVariable Integer storeId, @PathVariable Integer pageId,
            @RequestBody Page page) {
        return pageService.updatePage(storeId, pageId, page);
    }

    @DeleteMapping("/{storeId}/{pageId}")
    public ResponseEntity<String> deletePage(@PathVariable Integer storeId, @PathVariable Integer pageId) {
        return pageService.deletePage(storeId, pageId);
    }
}
