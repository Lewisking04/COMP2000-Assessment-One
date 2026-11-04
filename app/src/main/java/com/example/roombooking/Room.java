package com.example.roombooking;

/** A bookable space: five meeting rooms and two seminar rooms. */
public class Room {
    public final String id;
    public final String name;
    public final boolean seminar;
    public final int seats;

    public Room(String id, String name, boolean seminar, int seats) {
        this.id = id;
        this.name = name;
        this.seminar = seminar;
        this.seats = seats;
    }

    /** Text label for the room type. Type is not shown by colour alone. */
    public String typeLabel() {
        return seminar ? "Seminar room" : "Meeting room";
    }

    public String details() {
        return typeLabel() + " \u00B7 " + seats + " seats";
    }
}
