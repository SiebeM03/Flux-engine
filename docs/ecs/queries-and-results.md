# Queries and results

You query a **World** for entities that have a given set of component types. The result is a **Results** object that you
can iterate or stream over. Queries support 1, 2, 3, or 4 component types but this can easily be extended.

See also: [World](world.md), [Entities](entities.md), [Components](components.md).

## Finding entities

Use **findEntitiesWith** with the component classes. The world returns all entities that have **all** the requested
types.

```java
import me.siebe.flux.api.ecs.Results;

// All entities with Position
Results<Results.With1<Position>> withPos = world.findEntitiesWith(Position.class);

// All entities with both Position and Velocity
Results<Results.With2<Position, Velocity>> moving = world.findEntitiesWith(Position.class, Velocity.class);

// Three component types
Results<Results.With3<Position, Velocity, Health>> withHealth = world.findEntitiesWith(Position.class, Velocity.class, Health.class);

// Four component types
Results<Results.With4<Position, Velocity, Health, Name>> withName = world.findEntitiesWith(Position.class, Velocity.class, Health.class, Name.class);
```

If no entity has the requested combination, the query returns an **empty** result (no exception). If a component type
has never been used in the world, there is no store for it, so the result is also empty.

## Result types

**Results\<T\>** is an **Iterable\<T\>** and also provides **stream()**. The type **T** is one of the following records:

| Type                        | Fields                                       | Use when                     |
|-----------------------------|----------------------------------------------|------------------------------|
| **With1\<T\>**              | `comp`, `entity`                             | Query had one component type |
| **With2\<T1, T2\>**         | `comp1`, `comp2`, `entity`                   | Query had two types          |
| **With3\<T1, T2, T3\>**     | `comp1`, `comp2`, `comp3`, `entity`          | Query had three types        |
| **With4\<T1, T2, T3, T4\>** | `comp1`, `comp2`, `comp3`, `comp4`, `entity` | Query had four types         |

Each record gives you the **components** (only the ones you queried) for that entity and the **entity** reference. 
Components are in the same order as the class arguments to **findEntitiesWith**.

## Iteration

```java
for (Results.With2<Position, Velocity> row : world.findEntitiesWith(Position .class, Velocity .class)) {
    Position pos = row.comp1();
    Velocity vel = row.comp2();
    Entity e = row.entity();
    pos.x += vel.dx;
    pos.y += vel.dy;
}
```

## Streams

**Results** implements **stream()** so you can use **Stream** operations:

```java
world.findEntitiesWith(Position .class, Health .class)
    .stream()
    .filter(r -> r.comp2().current > 0)
    .forEach(r -> {
        r.comp1().x += 1;
    });
```

## Lazy evaluation

Queries are **lazy**: the matching work is done when you iterate or consume the stream. Creating a **Results** object
does not scan all entities immediately.

## Using queries as “systems”

In ECS, a “system” is logic that runs over entities with a specific component set. In Flux you implement that by calling
**findEntitiesWith** in your game loop and updating the components:

```java

@Override
protected void gameUpdate(AppContext ctx) {
    float dt = (float) ctx.getTimer().getDelta();
    for (var row : world.findEntitiesWith(Position.class, Velocity.class)) {
        row.comp1().x += row.comp2().dx * dt;
        row.comp1().y += row.comp2().dy * dt;
    }
}
```

You can run multiple such “systems” (multiple queries and updates) in **gameUpdate** in whatever order fits your game.
