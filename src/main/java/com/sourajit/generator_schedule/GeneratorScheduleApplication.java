package com.sourajit.generator_schedule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

import com.sourajit.generator_schedule.model.Generator;
import com.sourajit.generator_schedule.model.Schedule;
import com.sourajit.generator_schedule.repository.GeneratorRepository;
import com.sourajit.generator_schedule.repository.ScheduleRepository;

@SpringBootApplication
public class GeneratorScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeneratorScheduleApplication.class, args);
	}

	@Bean
	@Order(1)
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
			repo.save(new Generator("Unit-4", "COAL", 400)); // new gen to check ineer join and left join

		};
	}

	@Bean
	@Order(2)
	CommandLineRunner lazyDemo(GeneratorRepository repo, TransactionTemplate tx) {
		return args -> tx.executeWithoutResult(status -> {
			Generator g = repo.findById(1L).get();
			System.out.println("Loaded generator: " + g.getName());
			System.out.println("Schedules: " + g.getSchedules().size());
		});
	}

	@Bean
	@Order(3)
	CommandLineRunner eagerDemo(ScheduleRepository scheduleRepo, TransactionTemplate tx) {
		return args -> tx.executeWithoutResult(status -> {
			Schedule s = scheduleRepo.findById(1L).get();
			System.out.println("Loaded schedule date: " + s.getScheduleDate());
			System.out.println("Generator: " + s.getGenerator().getName());
		});
	}

	@Bean
	@Order(4)
	CommandLineRunner nPlusOneDemo(GeneratorRepository repo, TransactionTemplate tx) {
		return args -> tx.executeWithoutResult(status -> {
			List<Generator> all = repo.findAll();
			for (Generator g : all) {
				System.out.println(g.getName() + " has " + g.getSchedules().size() + " schedules");
			}
		});
	}

	@Bean
	@Order(5)
	CommandLineRunner joinFetchDemo(GeneratorRepository repo, TransactionTemplate tx) {
		return args -> tx.executeWithoutResult(status -> {
			List<Generator> all = repo.findAllWithSchedules();
			System.out.println("join fetch solution");
			for (Generator g : all) {
				System.out.println(g.getName() + " has " + g.getSchedules().size() + " schedules");
			}
		});
	}

	@Bean
	@Order(6)
	CommandLineRunner entityGraphDemo(GeneratorRepository repo, TransactionTemplate tx) {
		return args -> tx.executeWithoutResult(status -> {
			System.out.println("entity graph solution");
			for (Generator g : repo.findAllWithGraph()) {
				System.out.println(g.getName() + " has " + g.getSchedules().size() + " schedules");
			}
		});
	}
}
