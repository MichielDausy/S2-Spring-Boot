package com.example.s2angularapi.repository;

import com.example.s2angularapi.model.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignRepository extends JpaRepository<Sign, Integer> {
    Sign findByName(String name);
}
