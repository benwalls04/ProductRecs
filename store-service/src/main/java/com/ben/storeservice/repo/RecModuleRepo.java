package com.ben.storeservice.repo;

import com.ben.storeservice.model.RecModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecModuleRepo extends JpaRepository<RecModule, Integer> {
    List<RecModule> findAllByStoreId(Integer storeId);
}
