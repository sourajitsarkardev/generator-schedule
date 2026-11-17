package com.sourajit.generator_schedule;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.sourajit.generator_schedule.model.Generator;
import com.sourajit.generator_schedule.model.Schedule;
import com.sourajit.generator_schedule.repository.GeneratorRepository;

@SpringBootApplication
public class GeneratorScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeneratorScheduleApplication.class, args);
	}

	@Bean
	CommandLineRunner demo(GeneratorRepository repo) {
		return args -> {
			for (int i = 1; i <= 3; i++) {
				Generator gen = new Generator("Unit-" + i, i == 2 ? "HYDRO" : "COAL", 100 * i);
				for (int d = -1; d < 2; d++) {
					Schedule sch = new Schedule();
					sch.setScheduleDate(LocalDate.now().plusDays(d));
					sch.setScheduledMw(50 * i);
					gen.addSchedule(sch);
				}
				repo.save(gen); // cascade saves the schedules too
			}
		};
	}
}
