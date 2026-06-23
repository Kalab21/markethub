package com.markethub.app.repository;

import com.markethub.app.model.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"seller", "reviews"})
    @Query("select p from Product p where p.seller.userId = :id")
    List<Product> findBySellerId(@Param("id") long id);

    @EntityGraph(attributePaths = {"seller", "reviews"})
    @Query("select p from Product p where p.quantity > 0")
    List<Product> findNotOutOfStockProducts();

    @EntityGraph(attributePaths = {"seller"})
    @Query("select p from Product p where p.name = :name")
    List<Product> findAllByProductName(String name);
}
