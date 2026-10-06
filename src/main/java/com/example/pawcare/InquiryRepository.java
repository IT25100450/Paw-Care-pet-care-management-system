package com.example.pawcare.repository;

import com.example.pawcare.model.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {
    List<Inquiry> findByOwnerOwnerId(Integer ownerId);
    List<Inquiry> findAllByOrderByCreatedAtDesc();
}
