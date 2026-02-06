# EcsSystem registry

**EcsSystem** is a static global registry of **World** instances. It lets you look up a world by its ID or find which
world contains an entity when you only have the entity ID.

See also: [World](world.md), [Entities](entities.md).

## API

| Method                               | Description                                                                                                                                                                                                   |
|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **getWorld(int id)**                 | Returns the world with the given **getId()**, or `null` if not registered.                                                                                                                                    |
| **registerWorld(World world)**       | Registers a world. Worlds created via **World.create(...)** are registered automatically by the default **World.Factory**. Use this if you create a world through a custom factory that does not register it. |
| **getWorldByEntityId(int entityId)** | Searches all registered worlds for one that contains an entity with the given ID. Returns that world, or `null` if no such entity exists.                                                                     |

## Package and usage

- **Package:** `me.siebe.flux.api.ecs`
- **Usage:** Static methods only; no instance required.

```java
import me.siebe.flux.api.ecs.EcsSystem;

World world = EcsSystem.getWorld(worldId);
if (world != null) {
    Entity e = world.getEntity(entityId);
    // ...
}

// Find the world that owns this entity ID (e.g. from a saved reference)
World owner = EcsSystem.getWorldByEntityId(entityId);
```

## When to use

- You store a **world ID** or **entity ID** (e.g. in save data or in another subsystem) and need to resolve the
  **World** or **Entity** at runtime.
- You have multiple worlds and need to route an entity ID to the correct world.

If you already have an **Entity** reference, you typically already have the **World** from which you created it. Keeping
a reference to the world where you create entities often makes the registry unnecessary.
