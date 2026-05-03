package com.ben.storeservice.repo;

import com.ben.storeservice.model.RecModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecModuleRepo extends JpaRepository<RecModule, Integer> {
}
