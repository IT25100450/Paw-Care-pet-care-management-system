package com.example.pawcare.repository;

import com.example.pawcare.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p JOIN FETCH p.productCategory")
    List<Product> findAllWithCategory();

    List<Product> findByProductCategoryProductCategoryId(Integer categoryId);
}
