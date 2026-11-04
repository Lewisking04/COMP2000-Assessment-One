package com.example.roombooking;

import java.time.LocalDate;
import java.time.LocalTime;

/** One booking or booking request. */
public class Booking {

    /** The status shown on each booking. */
    public enum Status { PENDING, CONFIRMED, CANCELLATION_REQUESTED }

    public final long id;
    public final Room room;
    public final LocalDate date;
    public final LocalTime start;
    public final LocalTime end;
    public final String purpose;
    public Status status;

    public Booking(long id, Room room, LocalDate date, LocalTime start, LocalTime end,
                   String purpose, Status status) {
        this.id = id;
        this.room = room;
        this.date = date;
        this.start = start;
        this.end = end;
        this.purpose = purpose;
        this.status = status;
    }

    public boolean isPast() {
        return date.isBefore(LocalDate.now());
    }
}
