package net.rubygrapefruit.docs.model;

public class DefaultUnknown implements Unknown {
    private final Location location;
    private final String name;

    public DefaultUnknown(String name, Location location) {
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
