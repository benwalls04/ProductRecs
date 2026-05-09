package com.ben.storeservice.service;

import com.ben.storeservice.model.Page;
import com.ben.storeservice.model.Store;
import com.ben.storeservice.repo.PageRepo;
import com.ben.storeservice.repo.StoreRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PageService {

    private final PageRepo pageRepo;
    private final StoreRepo storeRepo;

    public PageService(PageRepo pageRepo, StoreRepo storeRepo) {
        this.pageRepo = pageRepo;
        this.storeRepo = storeRepo;
    }

    public ResponseEntity<String> createPage(Integer storeId, Page page) {
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        page.setStore(store);
        pageRepo.save(page);
        return new ResponseEntity<>("Created", HttpStatus.CREATED);
    }

    public ResponseEntity<List<Page>> getAllPages(Integer storeId) {
        return new ResponseEntity<>(pageRepo.findAllByStoreId(storeId), HttpStatus.OK);
    }

    public ResponseEntity<Page> getPage(Integer storeId, Integer pageId) {
        return pageRepo.findById(pageId)
                .filter(p -> p.getStore().getId().equals(storeId))
                .map(p -> new ResponseEntity<>(p, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ResponseEntity<String> updatePage(Integer storeId, Integer pageId, Page newPage) {
        Optional<Page> opt = pageRepo.findById(pageId);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Page page = opt.get();
        if (!page.getStore().getId().equals(storeId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (newPage.getName() != null) page.setName(newPage.getName());
        pageRepo.save(page);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<String> deletePage(Integer storeId, Integer pageId) {
        Optional<Page> opt = pageRepo.findById(pageId);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Page page = opt.get();
        if (!page.getStore().getId().equals(storeId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        pageRepo.deleteById(pageId);
        return new ResponseEntity<>("Deleted", HttpStatus.OK);
    }
}
