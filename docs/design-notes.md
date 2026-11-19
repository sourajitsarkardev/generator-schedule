# Design Notes

## SOLID

**Single Responsibility:** A class should have one reason to change. `Generator` only holds data; business rules live in a service class.

**Open/Closed:** Code should be open for extension but closed for modification. New schedule-validation rules are added as new classes implementing `ScheduleValidator`, without editing existing validators.

**Liskov Substitution:** A subclass must work anywhere its parent is expected without breaking behavior. A `HydroGenerator extends Generator` must not throw on methods like `getCapacityMw()`, or code written for `Generator` would fail.

**Interface Segregation:** No class should be forced to implement methods it doesn't use, so prefer several small interfaces over one large one. Small interfaces are also easier to mock in tests.

**Dependency Inversion:** High-level code depends on abstractions, not concrete classes. `demo` depends on the `GeneratorRepository` interface, and Spring injects the implementation at runtime, so it can be swapped or mocked.

## JPA Fetching

### Default fetch types

| Association                 | Default |
| --------------------------- | ------- |
| `@OneToMany`, `@ManyToMany` | LAZY    |
| `@ManyToOne`, `@OneToOne`   | EAGER   |

`@ManyToOne` is set to LAZY explicitly in `Schedule` to avoid hidden queries.

### Lazy loading

- Collections are loaded on first access, which needs an open session.
- Accessing a lazy association after the session closes throws `LazyInitializationException`.
- Fixes: `@Transactional` / `TransactionTemplate`, `JOIN FETCH` or `@EntityGraph`, or a DTO projection.
- Avoid `FetchType.EAGER` as a blanket fix.

### Lazy vs eager (observed in the SQL log)

- EAGER `@ManyToOne`: `findById` on `Schedule` ran one query with a `join generator`, even when the generator was unused.
- LAZY: the generator query is deferred until a field is accessed, and accessing it outside a session threw `LazyInitializationException`.

### `mappedBy`

`mappedBy = "generator"` on `Generator.schedules` says the `generator` field in `Schedule` owns the relationship (the foreign key `generator_id` lives in the `schedule` table). Without it, Hibernate creates an extra join table.

## N+1 Problem

- **Cause:** `findAll()` plus lazy access inside a loop. 1 query for generators + 1 per generator for schedules.
- **Observed:** 3 generators gave 1 + 3 = 4 queries.
- **Fix 1, `JOIN FETCH`:** 1 query. It is an inner join, so parents with no children disappear. Use `left join fetch` to keep them.
- **Fix 2, `@EntityGraph`:** 1 query. It uses a left join, so parents are kept (Unit-4 showed `0 schedules`).
- **Difference:** `JOIN FETCH` is written inside the query. `@EntityGraph` keeps the query clean and can be added to derived methods like `findByFuelType`.
- **Pagination caveat:** fetching a collection with a join plus `Pageable` makes Hibernate paginate in memory (warning `HHH90003004`). For paged queries use `@BatchSize` or a two-step query.
- **Hibernate 6** de-duplicates parent entities from a join fetch automatically. Older versions needed `select distinct`.

## Design Patterns

### Strategy

Each validation rule is its own class behind the `ScheduleValidator` interface, and `ScheduleService` loops over all of them. Adding a rule means adding one `@Component`, with no edit to the service. This is Open/Closed in code: Spring injects new validator beans into the list automatically.

**Mistake to remember:** both validators were first written with the condition for the invalid case. Say the rule as "valid when ..." before writing the comparison.

### Factory

`Generator.of(name, fuelType, capacityMw)` is a static factory method. The constructor is private (JPA uses a protected no-args constructor), so `of` is the only way to create a `Generator` from outside the `model` package.

**How it works**

1. General checks run first: name not blank, fuel type not null, capacity positive.
2. Fuel-specific checks run next, in a `switch` on `FuelType`, and only after the general checks pass. The null check must come first, because switching on a null enum throws `NullPointerException`.
3. If every check passes, the private constructor runs and the new object is returned. Otherwise an `IllegalArgumentException` with a clear message is thrown, and no object is created.

**Fuel-type validations (example limits, not real engineering rules)**

| Fuel type          | Rule                                    |
| ------------------ | --------------------------------------- |
| HYDRO              | capacity must not exceed 500 MW         |
| SOLAR              | capacity must not exceed 1000 MW        |
| COAL, WIND, BIOGAS | no extra rule beyond the general checks |

**Why `static` and `Generator.of(...)`**

A static method belongs to the class, so it can be called before any object exists. An instance method such as `g.getName()` needs an existing object. It is written inside `Generator`, so it can call the private constructor.

**When is a factory better than `new`?**

- Several creation paths with the same parameter types, since constructors cannot be told apart but factories can be named (`of`, `hydro`, `fromCsvRow`).
- Creation that may return a cached instance or a subclass, which a constructor cannot do.
- A private constructor forces every object through one door.

A validated public constructor gives the same safety with less ceremony. Choose the factory when there are multiple creation paths or different return types, and the constructor when creation is simple.

**Enum as the fuel type**

- An enum makes invalid fuel types unrepresentable, so no string comparison is needed.
- Store it with `@Enumerated(EnumType.STRING)`, never `ORDINAL`. Ordinal stores positions, so reordering or inserting a constant silently corrupts existing data.

**Factory vs Strategy**

- Factory decides how an object is created.
- Strategy decides how behavior is chosen and run.
- When fuel-specific rules multiply, move them out of the `switch` into Strategy classes (one rule per class), so adding a rule means adding a class, not editing the factory. Static methods cannot receive Spring beans, so that version lives in a service.
