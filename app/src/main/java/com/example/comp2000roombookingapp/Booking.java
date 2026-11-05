package com.example.comp2000roombookingapp;

import java.time.LocalDate;
import java.time.LocalTime;

/** One booking or request: room, day, times, who booked it and its status. */
public class Booking {

    // An enum is a fixed list of allowed values, so a typo is impossible.
    /** The status shown on each booking. */
    public enum Status { PENDING, CONFIRMED, CANCELLATION_REQUESTED }

    public final long id;
    public final Room room;
    public final LocalDate date;
    public final LocalTime start;
    public final LocalTime end;
    public final String booker;      // who the booking is for, e.g. "Demo Employee"
    public final String purpose;
    public Status status;      // not "final" because the status changes (e.g. Confirmed -> Cancellation requested)

    public Booking(long id, Room room, LocalDate date, LocalTime start, LocalTime end,
                   String booker, String purpose, Status status) {
        this.id = id;
        this.room = room;
        this.date = date;
        this.start = start;
        this.end = end;
        this.booker = booker;
        this.purpose = purpose;
        this.status = status;
    }

    /** True when the booking day is before today. My bookings uses this to split Upcoming from Past. */
    public boolean isPast() {
        return date.isBefore(LocalDate.now());
    }

    /** True when this booking takes the room (a pending request does not). */
    public boolean blocksRoom() {
        return status == Status.CONFIRMED || status == Status.CANCELLATION_REQUESTED;
    }
}
