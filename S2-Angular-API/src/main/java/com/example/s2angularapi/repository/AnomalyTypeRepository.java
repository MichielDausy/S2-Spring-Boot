package com.example.s2angularapi.repository;

import com.example.s2angularapi.model.AnomalyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnomalyTypeRepository extends JpaRepository<AnomalyType, Integer> {
    AnomalyType findByName(String name);
}
