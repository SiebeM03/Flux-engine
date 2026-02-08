package me.siebe.flux.api.event;

import java.util.UUID;

public abstract class Event {
    public final UUID uuid = UUID.randomUUID();
}
