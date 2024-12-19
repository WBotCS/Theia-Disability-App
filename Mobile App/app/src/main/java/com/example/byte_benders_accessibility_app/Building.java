// Building.java
package com.example.byte_benders_accessibility_app;

public class Building {
    private String name;
    private String distance;
    private String walkingTime;
    private String shorterRoute;
    private String longerRoute;

    public Building(String name, String distance, String walkingTime) {
        this(name, distance, walkingTime, null, null);
    }

    public Building(String name, String distance, String walkingTime, String shorterRoute, String longerRoute) {
        this.name = name;
        this.distance = distance;
        this.walkingTime = walkingTime;
        this.shorterRoute = shorterRoute;
        this.longerRoute = longerRoute;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public String getWalkingTime() {
        return walkingTime;
    }

    public String getShorterRoute() {
        return shorterRoute;
    }

    public String getLongerRoute() {
        return longerRoute;
    }
}