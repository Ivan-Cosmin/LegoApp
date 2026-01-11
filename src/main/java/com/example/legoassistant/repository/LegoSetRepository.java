package com.example.legoassistant.repository;

import com.example.legoassistant.model.LegoSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegoSetRepository extends JpaRepository<LegoSet, Long> {
    // Basic CRUD is automatically provided by JpaRepository
}