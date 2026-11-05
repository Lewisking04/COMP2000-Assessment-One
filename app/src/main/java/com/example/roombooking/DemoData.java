package com.example.roombooking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample data for the demo. Everything is kept in memory
 * and resets when the app restarts.
 */
public final class DemoData {
    private DemoData() { }

    public static final LocalTime OPEN = LocalTime.of(9, 0);          // 09:00 to 15:00
    public static final LocalTime CLOSE = LocalTime.of(15, 0);
    public static final LocalTime LAST_START = LocalTime.of(14, 30);   // latest start: a 30-minute booking ends at 15:00
    public static final int MAX_DAYS_AHEAD = 29;                        // less than 30 days ahead

    public static final List<Room> ROOMS = new ArrayList<>();
    private static final List<Block> BUSY = new ArrayList<>();
    private static final List<Booking> MY_BOOKINGS = new ArrayList<>();
    private static long nextId = 100;
    private static boolean room3Taken = false;

    /** A time when a room is booked by someone else. No date means every day. */
    private static class Block {
        final Room room; final LocalDate date; final LocalTime start; final LocalTime end;
        Block(Room room, LocalDate date, LocalTime start, LocalTime end) {
            this.room = room; this.date = date; this.start = start; this.end = end;
        }
    }

    static {
        ROOMS.add(new Room("R1", "Room 1", false, 6));
        ROOMS.add(new Room("R2", "Room 2", false, 6));
        ROOMS.add(new Room("R3", "Room 3", false, 8));
        ROOMS.add(new Room("R4", "Room 4", false, 10));
        ROOMS.add(new Room("R5", "Room 5", false, 12));
        ROOMS.add(new Room("S1", "Seminar 1", true, 40));
        ROOMS.add(new Room("S2", "Seminar 2", true, 30));

        // Other people's bookings
        BUSY.add(new Block(find("R2"), null, LocalTime.of(13, 30), LocalTime.of(15, 0)));
        BUSY.add(new Block(find("R5"), null, LocalTime.of(14, 0), LocalTime.of(15, 0)));
        BUSY.add(new Block(find("S1"), null, LocalTime.of(13, 0), LocalTime.of(15, 0)));

        // This employee's bookings
        LocalDate today = LocalDate.now();
        MY_BOOKINGS.add(new Booking(1, find("R3"), today.plusDays(1), LocalTime.of(14, 0),
                LocalTime.of(15, 0), "Team stand-up", Booking.Status.PENDING));
        MY_BOOKINGS.add(new Booking(2, find("S1"), today.plusDays(3), LocalTime.of(10, 0),
                LocalTime.of(12, 0), "Client workshop", Booking.Status.CONFIRMED));
        MY_BOOKINGS.add(new Booking(3, find("R5"), today.plusDays(4), LocalTime.of(9, 0),
                LocalTime.of(10, 0), "Planning", Booking.Status.CANCELLATION_REQUESTED));
        MY_BOOKINGS.add(new Booking(4, find("R1"), today.minusDays(2), LocalTime.of(11, 0),
                LocalTime.of(12, 0), "Review", Booking.Status.CONFIRMED));
    }

    public static Room find(String id) {
        for (Room r : ROOMS) {
            if (r.id.equals(id)) return r;
        }
        return null;
    }

    /** True if no confirmed booking overlaps the slot. */
    public static boolean isFree(Room room, LocalDate date, LocalTime start, LocalTime end) {
        for (Block b : BUSY) {
            if (b.room == room && (b.date == null || b.date.equals(date)) && overlaps(start, end, b.start, b.end)) {
                return false;
            }
        }
        for (Booking b : MY_BOOKINGS) {
            boolean blocks = b.status == Booking.Status.CONFIRMED
                    || b.status == Booking.Status.CANCELLATION_REQUESTED;
            if (blocks && b.room == room && b.date.equals(date) && overlaps(start, end, b.start, b.end)) {
                return false;
            }
        }
        return true;
    }

    private static boolean overlaps(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        return s1.isBefore(e2) && e1.isAfter(s2);
    }

    /** Rooms that are free for the whole slot. */
    public static List<Room> freeRooms(LocalDate date, LocalTime start, LocalTime end) {
        List<Room> free = new ArrayList<>();
        for (Room r : ROOMS) {
            if (isFree(r, date, start, end)) free.add(r);
        }
        return free;
    }

    /** Sends a booking request. Room 3 fails the first time so the error screen can be shown. */
    public static Booking requestBooking(Room room, LocalDate date, LocalTime start, LocalTime end,
                                         String purpose) {
        if (room.id.equals("R3") && !room3Taken) {
            room3Taken = true;
            BUSY.add(new Block(room, date, start, end));
            return null;
        }
        if (!isFree(room, date, start, end)) return null;
        Booking b = new Booking(nextId++, room, date, start, end, purpose, Booking.Status.PENDING);
        MY_BOOKINGS.add(b);
        return b;
    }

    /** Earliest start from the given time when the room is free for that length, or null. */
    public static LocalTime nextFreeStart(Room room, LocalDate date, LocalTime from, int minutes) {
        LocalTime t = from;
        while (!t.isAfter(LAST_START)) {
            LocalTime end = t.plusMinutes(minutes);
            if (end.isAfter(CLOSE)) return null;
            if (isFree(room, date, t, end)) return t;
            t = t.plusMinutes(30);
        }
        return null;
    }

    public static List<Booking> myBookings(boolean upcoming) {
        List<Booking> out = new ArrayList<>();
        for (Booking b : MY_BOOKINGS) {
            if (b.isPast() != upcoming) out.add(b);
        }
        out.sort((a, b) -> {
            int c = a.date.compareTo(b.date);
            return c != 0 ? c : a.start.compareTo(b.start);
        });
        return out;
    }

    /** The employee's booking with this id, or null if it was withdrawn. */
    public static Booking findBooking(long id) {
        for (Booking b : MY_BOOKINGS) {
            if (b.id == id) return b;
        }
        return null;
    }

    public static void withdraw(Booking b) {
        MY_BOOKINGS.remove(b);
    }

    public static void requestCancellation(Booking b) {
        b.status = Booking.Status.CANCELLATION_REQUESTED;
    }
}
