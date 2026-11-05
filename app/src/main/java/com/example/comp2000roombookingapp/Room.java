package com.example.comp2000roombookingapp;

/** A room. Stores the details of one room. */
public class Room {
    // "final" means the value is set once in the constructor and can never change afterwards.
    public final String id;
    public final String name;
    public final boolean seminar;
    public final int seats;

    // The constructor copies the values into the fields.
    public Room(String id, String name, boolean seminar, int seats) {
        this.id = id;
        this.name = name;
        this.seminar = seminar;
        this.seats = seats;
    }

    /** Text label for the room type. */
    public String typeLabel() {
        return seminar ? "Seminar room" : "Meeting room";
    }

    /** Short line shown under the room name, e.g. Meeting room, 6 seats. */
    public String details() {
        return typeLabel() + " \u00B7 " + seats + " seats";
    }
}
