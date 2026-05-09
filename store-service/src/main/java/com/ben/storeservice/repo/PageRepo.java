package com.ben.storeservice.repo;

import com.ben.storeservice.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageRepo extends JpaRepository<Page, Integer> {
    List<Page> findAllByStoreId(Integer storeId);
}
