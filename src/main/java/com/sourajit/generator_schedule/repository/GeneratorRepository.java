package com.sourajit.generator_schedule.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sourajit.generator_schedule.model.Generator;

public interface GeneratorRepository extends JpaRepository<Generator, Long> {
    List<Generator> findByFuelType(String fuelType);
}