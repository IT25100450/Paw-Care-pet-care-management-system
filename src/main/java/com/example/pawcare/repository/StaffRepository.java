package com.example.pawcare.repository;

import com.example.pawcare.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByEmailAndPassword(String email, String password);

    @Query("SELECT s FROM Staff s JOIN FETCH s.role WHERE s.email = :email AND s.password = :password")
    Optional<Staff> findByEmailAndPasswordWithRole(String email, String password);

    @Query("SELECT s FROM Staff s JOIN FETCH s.role r WHERE r.roleName = :roleName")
    List<Staff> findByRoleName(String roleName);

    @Query("SELECT s FROM Staff s JOIN FETCH s.role")
    List<Staff> findAllWithRole();
}
