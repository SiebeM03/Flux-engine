# Implementation notes

This page describes implementation details of the default ECS: **SimpleWorld**, **SimpleEntity**, **ComponentRegistry**,
**ComponentStore**, and **WorldQuery**. You can use the ECS without reading this; it is useful if you want to understand
performance, extend the engine, or provide a custom **World** implementation.

See
also: [World](world.md), [Entities](entities.md), [Components](components.md), [Queries and results](queries-and-results.md).

## Packages and types

- **flux-api:** `Entity`, `World`, `Results`, `EcsSystem` (interfaces and public API).
- **flux-core:** `me.siebe.flux.ecs` — `SimpleWorld`, `SimpleEntity`, `ComponentRegistry`, `ComponentStore`,
  `WorldQuery`, `Results.ResultSet` and query iterators. The registry, store, and query classes are package-private.

## SimpleWorld

- **Entity IDs** are assigned sequentially from an atomic counter, or **reused** from a stack of IDs that were freed
  when entities were deleted. This keeps ID values bounded and avoids unbounded growth of internal arrays keyed by
  entity ID.
- **Entity map:** A `Map<Integer, Entity>` stores the current **SimpleEntity** for each active ID. **getEntity(id)** is
  a map lookup; **deleteEntity** removes the entity from the map and pushes the ID onto the recycle stack.
- **Component storage** is delegated to a single **ComponentRegistry** created with the world’s max entity count.

## ComponentRegistry

- Holds a **Map<Class<?>, ComponentStore<?>>**: one **ComponentStore** per component type.
- **addComponent(entityId, component):** Resolves the component’s class; if no store exists for that class, creates a
  new **ComponentStore(maxEntities)** and registers it. Then adds the component to that store for the given entity ID.
- **removeComponents(entityId):** Collects all components for that entity (by asking each store) and removes each from
  its store. Used when an entity is deleted.
- **getComponentStore(type):** Returns the store for that type, or `null`. Used by **SimpleEntity** (get, has,
  removeType) and by **WorldQuery**.

## ComponentStore\<T\>

- **Dense + sparse layout:** Components are stored in a dense array **components[]** for cache-friendly iteration. A
  **entityToIndex[]** (size maxEntities) maps entity ID → index in that array; **indexToEntity[]** maps index → entity
  ID for iteration.
- **Rules:** At most one component per entity in this store. **add(entity, component)** throws if the entity already has
  a component of this type. **remove(entity)** uses swap-and-pop: the last component is moved into the removed slot so
  the array stays dense.
- **Capacity:** The dense arrays start at a small initial capacity and double when full. **entityToIndex** is allocated
  once with length **maxEntities** (so entity IDs must be in range **[0, maxEntities)**).

## WorldQuery

- **findEntitiesWith** (1–4 types) is implemented by getting the **ComponentStore** for each requested type. If any
  store is missing, returns an empty **ResultSet**.
- For one type: iterates over that store’s dense array and yields **With1(component, entity)** for each entry.
- For 2–4 types: iterates over the first store and, for each entity ID, checks the other store(s) with
  **has(entityId)**. Only yields a result if all stores have that entity. This avoids building temporary sets; the first
  store drives the iteration order.
- **ResultSet** is lazy: it holds an iterator factory and creates a new iterator on each **iterator()** or **stream()**
  call. Each iterator walks the relevant store(s) and produces **With1** … **With4** records with **SimpleEntity**
  instances created on demand.

## World.Factory and SPI

- **World.create()** and **World.create(name)** etc. call **World.factory()** (or **World.factory(maxEntities)**), which
  uses **SystemProvider.provide(World.Factory.class, ...)** to get the factory.
- The default factory is **SimpleWorld.Factory**, registered via Java SPI:
  **META-INF/services/me.siebe.flux.api.ecs.World\$Factory** with contents **me.siebe.flux.ecs.SimpleWorld$Factory**.
- **SimpleWorld.Factory** keeps a static counter for world IDs and names. **create()** / **create(name)** build a
  **SimpleWorld**, then call **EcsSystem.registerWorld(world)** before returning.

## Dependencies

- **flux-api** — ECS interfaces and **EcsSystem**.
- **flux-core** — **SimpleWorld**, **SimpleEntity**, **ComponentRegistry**, **ComponentStore**, **WorldQuery**. Depends
  on **flux-api** and **flux-util** (for **SystemProvider** / **ProvidableSystem**).

Adding **flux-core** to your project gives you the default ECS implementation with no extra configuration.
