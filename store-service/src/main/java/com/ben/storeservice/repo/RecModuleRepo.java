package com.ben.storeservice.repo;

import com.ben.storeservice.model.RecModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecModuleRepo extends JpaRepository<RecModule, Integer> {
    @Query("SELECT r FROM RecModule r WHERE r.page.store.id = :storeId")
    List<RecModule> findAllByStoreId(@Param("storeId") Integer storeId);
    List<RecModule> findAllByPageId(Integer pageId);
}
