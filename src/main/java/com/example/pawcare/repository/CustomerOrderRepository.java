package com.example.pawcare.repository;

import com.example.pawcare.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Integer> {
    @Query("SELECT DISTINCT o FROM CustomerOrder o JOIN FETCH o.owner JOIN FETCH o.items i JOIN FETCH i.product WHERE o.owner.ownerId = :ownerId ORDER BY o.orderDate DESC")
    List<CustomerOrder> findByOwnerOrderByDateDesc(@Param("ownerId") Integer ownerId);
}
