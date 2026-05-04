package com.ben.storeservice.repo;

import com.ben.storeservice.model.Catalog;
import com.ben.storeservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {
    List<Product> findAllByCatalog(Catalog catalog);

    @Query(value = "SELECT EXTRACT(EPOCH FROM (NOW() - MAX(created_at)))/3600 FROM product WHERE catalog_id = :catalogId", nativeQuery = true)
    Double hoursSinceLastCreated(@Param("catalogId") Integer catalogId);
}
