# Entity Component System (ECS)

The Flux engine provides an **Entity Component System (ECS)** for data-oriented game logic. The ECS pattern separates 
**data** (components) from **behavior** (your systems), promoting composition over inheritance and cache-friendly
iteration.

## Overview

| Concept       | Description                                                                                                                                                                 |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Entity**    | A unique ID that can have components attached. Entities hold no data themselves; they are containers for components.                                                        |
| **Component** | A plain Java object (POJO) holding data. One instance per component type per entity.                                                                                        |
| **World**     | The container for all entities and their components. You create entities in a world and query by component types.                                                           |
| **System**    | Your own logic (e.g. in `gameUpdate()`) that queries the world and updates components. The Flux API does not define a formal “System” type; you iterate over query results. |

## Packages

- **API:** `me.siebe.flux.api.ecs` — `Entity`, `World`, `Results`, `EcsSystem`
- **Implementation:** `me.siebe.flux.ecs` (flux-core) — `SimpleWorld`, `SimpleEntity`, plus internal
  `ComponentRegistry`, `ComponentStore`, `WorldQuery`

## Documentation

- [World](world.md) — Creating and configuring a world, identity, factory
- [Entities](entities.md) — Creating entities, entity API (add, get, has, remove, delete)
- [Components](components.md) — What components are, design guidelines, examples
- [Queries and results](queries-and-results.md) — Finding entities by component types, `Results`, iteration and streams
- [EcsSystem registry](ecs-system-registry.md) — Global world registry, looking up worlds by ID or entity ID
- [Implementation notes](implementation-notes.md) — SimpleWorld, ID recycling, component storage, SPI

## Quick start

```java
import me.siebe.flux.api.ecs.World;
import me.siebe.flux.api.ecs.Entity;
import me.siebe.flux.api.ecs.Results;

// In initGameSystems(): create world
World world = World.create("game-world");

// Spawn an entity with components
Entity player = world.createEntity(new Position(0, 0), new Velocity(1, 0));

// In gameUpdate(): run a “system” by querying and updating
for(var row :world.findEntitiesWith(Position .class, Velocity .class)){
  row.comp1().x += row.comp2().dx * dt;
  row.comp1().y += row.comp2().dy * dt;
}
```

See [Entities](entities.md) and [Queries and results](queries-and-results.md) for full detail.
