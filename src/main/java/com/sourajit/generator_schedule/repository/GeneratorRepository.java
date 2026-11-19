package com.sourajit.generator_schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sourajit.generator_schedule.model.FuelType;
import com.sourajit.generator_schedule.model.Generator;

public interface GeneratorRepository extends JpaRepository<Generator, Long> {
    List<Generator> findByFuelType(FuelType fuelType);

    @Query("select g from Generator g join fetch g.schedules")
    List<Generator> findAllWithSchedules();

    @EntityGraph(attributePaths = "schedules")
    @Query("select g from Generator g")
    List<Generator> findAllWithGraph();
}