package com.ben.storeservice.repo;

import com.ben.storeservice.model.Catalog;
import com.ben.storeservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {
    List<Product> findAllByCatalog(Catalog catalog);

    @Query("SELECT MAX(p.createdAt) FROM Product p WHERE p.catalog.id = :catalogId")
    LocalDateTime latestCreatedAt(@Param("catalogId") Integer catalogId);

    void deleteByCatalogId(Integer catalogId);
}
