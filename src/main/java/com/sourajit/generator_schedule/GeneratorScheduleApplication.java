package com.sourajit.generator_schedule;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.sourajit.generator_schedule.model.Generator;
import com.sourajit.generator_schedule.repository.GeneratorRepository;

@SpringBootApplication
public class GeneratorScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeneratorScheduleApplication.class, args);
	}

	@Bean
	CommandLineRunner demo(GeneratorRepository repo) {
		return args -> {
			repo.save(new Generator("Unit-1", "COAL", 210));
			repo.save(new Generator("Unit-2", "HYDRO", 120));
			repo.save(new Generator("Unit-3", "COAL", 500));
			System.out.println(repo.findByFuelType("COAL"));
		};
	}
}
