package com.example.legoassistant.repository;

import com.example.legoassistant.model.BuildingStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingStepRepository extends JpaRepository<BuildingStep, Long> {
}

