# Components

**Components** are plain Java objects (POJOs) that hold data. Each entity can have at most **one instance per component
type**. Components have no behavior in the ECS sense; your “systems” (game logic) read and write component data by
querying the world.

See also: [Entities](entities.md), [Queries and results](queries-and-results.md).

## Design guidelines

- **Data only** — Prefer small, data-focused types (position, velocity, health, name). No engine callbacks or heavy
  logic inside components.
- **One per type per entity** — The default implementation allows only one component of each class per entity. Adding a
  second component of the same type throws an error.
- **Any class** — There is no required interface or base class. Use records, simple classes, or “tag” components with no
  fields.

## Examples

### Data components

```java
public record Position(float x, float y) {}

public record Velocity(float dx, float dy) {}

public static class Health {
    public int current;
    public int max;
    public Health(int current, int max) {
        this.current = current;
        this.max = max;
    }
}

public static class Name {
    public String value;
    public Name(String value) {this.value = value;}
}
```

### Tag components

Use empty (or minimal) components to mark entities for queries:

```java
public static class PlayerTag {}

public static class EnemyTag {}
```

Then query for e.g. **findEntitiesWith(Position.class, PlayerTag.class)** to get all player entities with a position.

## Registration

You do **not** register component types upfront. When you first **add** a component of a given class to any entity, the
world’s internal **ComponentRegistry** creates a **ComponentStore** for that type. Subsequent adds and queries use that
store automatically. See [Implementation notes](implementation-notes.md).

## Adding and removing

- **entity.add(component)** — Attaches the component. If the entity already has that type, the default implementation
  throws **IllegalArgumentException**.
- **entity.removeType(SomeComponent.class)** — Removes the component of that type from the entity.

Components are stored by the **World** (in the default implementation, per world). The **Entity** object is a handle
that delegates **add**, **get**, **has**, and **removeType** to the world’s component registry.
