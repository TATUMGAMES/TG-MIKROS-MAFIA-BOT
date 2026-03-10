# BEST CODING PRACTICES – TG-MIKROS-BOT

This document defines the **best practices and coding standards** to be followed when contributing to the
`TG-MIKROS-BOT` Discord bot. All code should be **clean, modular, testable, and maintainable**. These rules are intended
to guide both human and AI contributors (such as Cursor AI) to write production-grade code.

---

## Project Architecture Guidelines

### 1. Clean Architecture (Layered / Hexagonal Approach)

Use a modular, layered structure:

- `config/` – App configuration and constants.
- `bot/` – Discord bot entry point and setup.
- `commands/` – All bot commands and interactions.
- `services/` – Business logic and use cases.
- `models/` – POJOs, enums, and data classes.
- `data/` – API calls, data access, storage (if any).
- `utils/` – Utility classes/helpers (date, string, etc.).
- `tests/` – Unit and integration tests.

**Do not put logic in the bot event handlers.** Delegate to services.

---

## Core OOP Practices

### Encapsulation

- Use private fields and public getters/setters as needed.
- No public mutable fields.

### Inheritance (when appropriate)

- Favor **composition** over inheritance when possible.
- Use abstract classes or interfaces for shared behaviors.

### Interfaces

- Use interfaces to define behaviors (e.g., `CommandHandler`, `GameService`, etc.).
- Keep interface names descriptive (`ICommand`, `IDataFetcher` are discouraged – use real names).

---

## Code Style & Formatting

### Naming Conventions

| Element    | Convention           | Example                    |
|------------|----------------------|----------------------------|
| Classes    | PascalCase           | `GameService`, `UserStats` |
| Interfaces | PascalCase           | `CommandHandler`           |
| Methods    | camelCase            | `handleInteraction()`      |
| Variables  | camelCase            | `userName`, `gameList`     |
| Constants  | UPPER_SNAKE_CASE     | `MAX_RETRY_ATTEMPTS`       |
| Packages   | lower.case.with.dots | `com.mikros.bot.service`   |

### Indentation & Braces

- Use **K&R style** (Kernighan and Ritchie):

```java
if (condition) {
    doSomething();
} else {
    doSomethingElse();
}
```

- 4 spaces for indentation.
- No tabs.
- **Formatting:** Run `./gradlew spotlessApply` before commit to apply consistent Java formatting (Spotless with
  google-java-format). Run `./gradlew spotlessCheck` in CI to enforce.

### 📄 Documentation Standards

- All public classes and methods must have Javadoc comments.
- Use `@param`, `@return`, and `@throws` tags as needed.
- Write clear, concise, and actionable comments.
- Avoid redundant or obvious comments.

### 🧪 Unit Testing

- All logic-heavy services should be unit tested.
- Use JUnit 5.
- Use Mockito for mocking dependencies.
- Place tests in `src/test/java/...` matching the source structure.
- Name test classes like `XyzServiceTest.java`.

Example:

```java
@Test
void shouldReturnTopGamesByGenre() {
    when(api.getGames()).thenReturn(mockedList);
    List<Game> result = service.getTopGames("rpg");
    assertEquals(3, result.size());
}
```

### Error Handling

- Do not swallow exceptions.
- Catch and log errors with context.
- Use custom exceptions where it improves clarity.

Example:

```
throw new GameDataNotFoundException("Game ID " + gameId + " not found");
```

### Configuration Best Practices

- Use `.env` or a `config.properties` file for:
  - Bot token
  - API URLs
  - Secrets and keys

* Do not hardcode values in classes.
* Provide a ConfigLoader class to read and validate config at startup.

### Dependency Management

- Use Gradle with well-defined dependencies.
- Keep dependencies minimal and necessary.
- Use dependency versions via variables (avoid hardcoding multiple times).

### Enums over Constants

- Use enums for any fixed set of values like game genres, roles, command types, etc.

```java
public enum GameGenre {
    RPG, FPS, PUZZLE, STRATEGY, INDIE
}
```

### Clean Code Principles

- **DRY** – Don’t repeat yourself.
- **KISS** – Keep it simple and stupid.
- **YAGNI** – You aren’t gonna need it.
- **SRP** – Single Responsibility Principle.
- Favor readability over cleverness.