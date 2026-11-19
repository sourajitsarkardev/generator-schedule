package com.sourajit.generator_schedule.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private FuelType fuelType;

    @Positive
    private double capacityMw;

    private Generator(String name, FuelType fuelType, double capacityMw) {
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

    public static Generator of(String name, FuelType fuelType, double capacityMw) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Generator name must not be blank");
        }
        if (fuelType == null) {
            throw new IllegalArgumentException("Fuel type must not be null");
        }
        if (capacityMw <= 0) {
            throw new IllegalArgumentException("Capacity must be positive, got: " + capacityMw);
        }

        // 2. fuel-specific checks (only reached when the general ones pass)
        switch (fuelType) {
            case COAL -> {
                if (capacityMw > 1000) {
                    throw new IllegalArgumentException("COAL unit cannot exceed 500 MW, got: " + capacityMw);
                }
            }
            case HYDRO -> {
                if (capacityMw > 300) {
                    throw new IllegalArgumentException("Hydro unit cannot exceed 300 MW, got: " + capacityMw);
                }
            }
            case SOLAR -> {
                if (capacityMw > 100) {
                    throw new IllegalArgumentException("Solar unit cannot exceed 100 MW, got: " + capacityMw);
                }
            }

            default -> {
            } // no extra rule for the other fuel types
        }
        return new Generator(name, fuelType, capacityMw);
    }

}