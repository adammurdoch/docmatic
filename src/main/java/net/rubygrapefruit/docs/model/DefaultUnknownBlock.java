package net.rubygrapefruit.docs.model;

public class DefaultUnknownBlock implements UnknownBlock {
    private final Location location;
    private final String name;

    public DefaultUnknownBlock(String name, Location location) {
        this.location = location;
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
