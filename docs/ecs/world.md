# World

A **World** is the central container for all entities and their components in the ECS. You create entities in a world
and run queries against it.

See also: [Entities](entities.md), [Queries and results](queries-and-results.md),
[EcsSystem registry](ecs-system-registry.md).

## Creating a world

The **World** interface provides static factory methods. The default implementation is **SimpleWorld** (from flux-core),
provided via the **World.Factory** SPI.

```java
import me.siebe.flux.api.ecs.World;

// Default: max 1,000,000 entities, auto-generated name "simple-world-<N>"
World world = World.create();

// Custom max entities
World world = World.create(10_000);

// Custom name
World world = World.create("game-world");

// Name and max entities
World world = World.create("game-world", 10_000);
```

Worlds created this way are automatically **registered** with **EcsSystem** so you can look them up by ID later.
See [EcsSystem registry](ecs-system-registry.md).

### Factory options

| Method                              | Result                                             |
|-------------------------------------|----------------------------------------------------|
| **World.create()**                  | Max entities: 1,000,000; name: `simple-world-<id>` |
| **World.create(maxEntities)**       | Custom max; name: `simple-world-<id>`              |
| **World.create(name)**              | Max: 1,000,000; custom name                        |
| **World.create(name, maxEntities)** | Custom name and max                                |

### Custom factory

To use a different world implementation or configuration:

```java
World.Factory factory = World.factory();           // default max entities
World.Factory factory = World.factory(50_000);    // custom max

World w1 = factory.create();                       // auto-generated name
World w2 = factory.create("my-world");            // custom name
```

The factory is resolved via **SystemProvider**; the default is **SimpleWorld.Factory** (registered in
`META-INF/services/me.siebe.flux.api.ecs.World$Factory`).

## World identity

- **getId()** — Unique integer ID for this world. Assigned when the world is created.
- **getName()** — Human-readable name. Either the name you passed to **create(name)** or an auto-generated name like
  `simple-world-1`.

## Entity management (summary)

- **createEntity(Object... components)** — Creates a new entity, optionally with initial components. Nulls in the array
  are ignored. See [Entities](entities.md).
- **getEntity(int id)** — Returns the entity with the given ID, or `null`.
- **deleteEntity(Entity entity)** — Removes the entity and all its components from the world. The entity’s ID may be
  recycled for future entities.

Details are in [Entities](entities.md).

## Query methods (summary)

- **findEntitiesWith(Class\<T\>)** — All entities with that component type.
- **findEntitiesWith(Class\<T1\>, Class\<T2\>)** — All entities with both types.
- **findEntitiesWith(Class\<T1\>, Class\<T2\>, Class\<T3\>)** — All entities with all three.
- **findEntitiesWith(Class\<T1\>, Class\<T2\>, Class\<T3\>, Class\<T4\>)** — All entities with all four.

Each returns a **Results** object. See [Queries and results](queries-and-results.md).
