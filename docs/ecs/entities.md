# Entities

An **Entity** is a unique identifier that can have components attached. Entities themselves hold no data; they are
containers for components. Your logic operates on entities by querying the world for component types and reading or
updating those components.

See also: [World](world.md), [Components](components.md), [Queries and results](queries-and-results.md).

## Creating entities

Create entities from a **World**. You can attach initial components in one call; nulls in the array are ignored.

```java
World world = World.create();

// Entity with no components
Entity entity = world.createEntity();

// Entity with initial components
Entity player = world.createEntity(
        new Position(0, 0),
        new Velocity(1, 0),
        new Health(100, 100)
);
```

Each entity receives a unique integer **ID** that remains constant for its lifetime (until the entity is deleted; IDs
may then be recycled). See [Implementation notes](implementation-notes.md).

## Entity API

| Method                                 | Description                                                                                                                                                                                                                                                 |
|----------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **getId()**                            | Returns the entity’s unique integer ID.                                                                                                                                                                                                                     |
| **add(Object component)**              | Adds a component to this entity. Returns `this` for chaining. Null is ignored. If the entity already has a component of the same type, behavior is implementation-dependent (SimpleEntity throws `IllegalArgumentException("Entity already has component")` |
| **removeType(Class<?> componentType)** | Removes the component of the given type. Returns `true` if a component was removed, false otherwise.                                                                                                                                                        |
| **has(Class<?> componentType)**        | Returns whether the entity has a component of that type.                                                                                                                                                                                                    |
| **get(Class\<T\> componentType)**      | Returns the component of that type, or `null` if the entity doesn’t have it.                                                                                                                                                                                |
| **delete()**                           | Deletes the entity from its world (removes all components; ID may be recycled). Returns `true` if deleted. After deletion, the entity should not be used.                                                                                                   |

### Example

```java
entity.add(new Name("Player 1"));

if(entity.has(Health .class)){
  Health h = entity.get(Health.class);
  h.current -=10;
}

entity.removeType(Velocity .class);

entity.delete();
```

## World-level entity operations

From the **World** you can:

- **getEntity(int id)** — Get the entity with the given ID, or `null` if it doesn’t exist (e.g. never created or already
  deleted).
- **deleteEntity(Entity entity)** — Remove the entity and all its components. Returns `true` if the entity was in this
  world and was deleted. Equivalent to **entity.delete()** when the entity belongs to that world.

Use **getEntity** when you only have an ID (e.g. from a saved game or another subsystem). Use **deleteEntity** when you
have the **Entity** reference and want to remove it from the world.
