package com.sourajit.generator_schedule.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.CascadeType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString

public class Generator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String fuelType;

    @Positive
    double capacityMw;

    public Generator(String name, String fuelType, double capacityMw) {
        this.name = name;
        this.fuelType = fuelType;
        this.capacityMw = capacityMw;
    }

    @OneToMany(mappedBy = "generator", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Schedule> schedules = new ArrayList<>();

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        schedule.setGenerator(this);
    }

}