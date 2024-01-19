package com.example.s2angularapi.repository;

import com.example.s2angularapi.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainRepository extends JpaRepository<Train, Integer> {
    Train findByName(String name);
}
