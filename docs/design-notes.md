# Design Notes

## SOLID

**Single Responsibility:** A class should have one reason to change. `Generator` only holds data; business rules will live in a service class.

**Open/Closed:** Code should be open for extension but closed for modification. New schedule-validation rules will be added as new classes implementing a `ScheduleValidator` interface, without editing existing validators.

**Liskov Substitution:** A subclass must work anywhere its parent is expected without breaking behavior. A `HydroGenerator extends Generator` must not throw on methods like `getCapacityMw()`, or code written for `Generator` would fail.

**Interface Segregation:** No class should be forced to implement methods it doesn't use, so prefer several small interfaces over one large one. Small interfaces are also easier to mock in tests.

**Dependency Inversion:** High-level code depends on abstractions, not concrete classes. `demo` depends on the `GeneratorRepository` interface, and Spring injects the implementation at runtime, so it can be swapped or mocked.

Collections are lazy by default. The query for them runs on first access.
Accessing them after the session closes throws LazyInitializationException.
Fixes: @Transactional, JOIN FETCH or @EntityGraph, or a DTO projection. Avoid FetchType.EAGER as a blanket fix.

EAGER @ManyToOne loads the generator with a join on every schedule load, even when unused.
LAZY defers the generator query until a field is accessed.
Prefer LAZY and fetch what you need with JOIN FETCH or @EntityGraph.
