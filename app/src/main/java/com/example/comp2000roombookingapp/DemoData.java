package com.example.comp2000roombookingapp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Sample data for the demo. Kept in memory, so it resets when the app restarts. */
public final class DemoData {
    // A private constructor stops anyone writing new DemoData(). Everything is static.
    private DemoData() { }

    // Opening rules. Change these lines to change the booking window everywhere.
    public static final LocalTime OPEN = LocalTime.of(9, 0);          // rooms can be booked from 09:00 to 17:00
    public static final LocalTime CLOSE = LocalTime.of(17, 0);
    public static final LocalTime LAST_START = LocalTime.of(16, 30);   // latest start: a 30-minute booking ends at 17:00
    public static final int MAX_DAYS_AHEAD = 29;                        // less than 30 days ahead

    // The name used for the signed-in employee
    public static final String EMPLOYEE_NAME = "Demo Employee";

    // The name used for bookings a manager makes for themselves.
    public static final String MANAGER_NAME = "Manager";

    // The people a manager can book a room for
    public static final String[] EMPLOYEE_NAMES = {
            "Demo Employee", "Employee A", "Employee B", "Employee C", "Employee D"
    };

    // The sample data lists. ROOMS is public so screens can use ROOMS.size().
    public static final List<Room> ROOMS = new ArrayList<>();
    private static final List<Booking> BOOKINGS = new ArrayList<>();
    private static long nextId = 100;
    private static boolean room3Taken = false;

    // A static block runs once when the app first uses this class. It fills the lists with the sample data.
    static {
        ROOMS.add(new Room("R1", "Room 1", false, 6));
        ROOMS.add(new Room("R2", "Room 2", false, 6));
        ROOMS.add(new Room("R3", "Room 3", false, 8));
        ROOMS.add(new Room("R4", "Room 4", false, 10));
        ROOMS.add(new Room("R5", "Room 5", false, 12));
        ROOMS.add(new Room("S1", "Seminar 1", true, 40));
        ROOMS.add(new Room("S2", "Seminar 2", true, 30));

        LocalDate today = LocalDate.now();

        // Other people's bookings, repeated on every working day for the next 30 days
        for (int day = 0; day <= MAX_DAYS_AHEAD; day++) {
            LocalDate d = today.plusDays(day);
            if (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            addOther("R2", d, LocalTime.of(13, 30), LocalTime.of(15, 0), "Employee A", "Project review");
            addOther("R5", d, LocalTime.of(14, 0), LocalTime.of(15, 0), "Employee B", "Interviews");
            addOther("S1", d, LocalTime.of(13, 0), LocalTime.of(15, 0), "Employee C", "Training");
        }

        // This employee's bookings. Dates are worked out from today so the data never goes out of date.
        BOOKINGS.add(new Booking(1, find("R3"), workday(today, 1), LocalTime.of(14, 0),
                LocalTime.of(15, 0), EMPLOYEE_NAME, "Team stand-up", Booking.Status.PENDING));
        BOOKINGS.add(new Booking(2, find("S1"), workday(today, 2), LocalTime.of(10, 0),
                LocalTime.of(12, 0), EMPLOYEE_NAME, "Client workshop", Booking.Status.CONFIRMED));
        BOOKINGS.add(new Booking(3, find("R5"), workday(today, 3), LocalTime.of(9, 0),
                LocalTime.of(10, 0), EMPLOYEE_NAME, "Planning", Booking.Status.CANCELLATION_REQUESTED));
        BOOKINGS.add(new Booking(4, find("R1"), today.minusDays(2), LocalTime.of(11, 0),
                LocalTime.of(12, 0), EMPLOYEE_NAME, "Review", Booking.Status.CONFIRMED));
    }

    // Moves forward by n working days (Monday to Friday), so the sample bookings never fall on a weekend.
    private static LocalDate workday(LocalDate from, int n) {
        LocalDate d = from;
        int counted = 0;
        while (counted < n) {
            d = d.plusDays(1);
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) counted++;
        }
        return d;
    }

    // Adds one confirmed booking made by somebody else (used only for the sample data above).
    private static void addOther(String roomId, LocalDate d, LocalTime s, LocalTime e, String who, String why) {
        BOOKINGS.add(new Booking(nextId++, find(roomId), d, s, e, who, why, Booking.Status.CONFIRMED));
    }

    /** Finds a room by its id (e.g. "R3"), because Intents only carry the id. */
    public static Room find(String id) {
        for (Room r : ROOMS) {
            if (r.id.equals(id)) return r;
        }
        return null;
    }

    // Availability (used by both roles)

    /** The booking holding this room in this slot, or null if it is free. Pending requests don't count. */
    public static Booking holder(Room room, LocalDate date, LocalTime start, LocalTime end) {
        for (Booking b : BOOKINGS) {
            if (b.blocksRoom() && b.room == room && b.date.equals(date) && overlaps(start, end, b.start, b.end)) {
                return b;
            }
        }
        return null;
    }

    /** True if no confirmed booking overlaps the slot. */
    public static boolean isFree(Room room, LocalDate date, LocalTime start, LocalTime end) {
        return holder(room, date, start, end) == null;
    }

    // Two time ranges overlap when each one starts before the other one ends.
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

    // Employee side

    /** Sends a booking request. Room 3 fails the first time so the error screen can be shown. */
    public static Booking requestBooking(Room room, LocalDate date, LocalTime start, LocalTime end,
                                         String purpose) {
        if (room.id.equals("R3") && !room3Taken) {
            room3Taken = true;
            BOOKINGS.add(new Booking(nextId++, room, date, start, end, "Employee D", "Meeting",
                    Booking.Status.CONFIRMED));
            return null;
        }
        if (!isFree(room, date, start, end)) return null;
        Booking b = new Booking(nextId++, room, date, start, end, EMPLOYEE_NAME, purpose, Booking.Status.PENDING);
        BOOKINGS.add(b);
        return b;
    }

    /** Returns this employee's bookings: upcoming ones if true, past ones if false, sorted by date then start time. */
    public static List<Booking> myBookings(boolean upcoming) {
        List<Booking> out = new ArrayList<>();
        for (Booking b : BOOKINGS) {
            if (b.booker.equals(EMPLOYEE_NAME) && b.isPast() != upcoming) out.add(b);
        }
        sortByTime(out);
        return out;
    }

    // Sorts bookings by date, then by start time. A Comparator tells Java how to put two bookings in order.
    private static void sortByTime(List<Booking> list) {
        Collections.sort(list, new Comparator<Booking>() {
            @Override
            public int compare(Booking a, Booking b) {
                int c = a.date.compareTo(b.date);
                return c != 0 ? c : a.start.compareTo(b.start);
            }
        });
    }

    /** The booking with this id, or null if it has been removed. */
    public static Booking findBooking(long id) {
        for (Booking b : BOOKINGS) {
            if (b.id == id) return b;
        }
        return null;
    }

    /** Withdraw = delete a request that is still pending. */
    public static void withdraw(Booking b) {
        BOOKINGS.remove(b);
    }

    /** Cancel request = ask the manager to cancel a confirmed booking. The booking stays, with a new status. */
    public static void requestCancellation(Booking b) {
        b.status = Booking.Status.CANCELLATION_REQUESTED;
    }

    // Manager side. Only the manager screens call these.

    /** Every booking that holds this room on this day, earliest first. */
    public static List<Booking> bookingsOn(Room room, LocalDate date) {
        List<Booking> out = new ArrayList<>();
        for (Booking b : BOOKINGS) {
            if (b.blocksRoom() && b.room == room && b.date.equals(date)) out.add(b);
        }
        sortByTime(out);
        return out;
    }

    /** A manager books a room. It is confirmed straight away. Returns null if the room is not free. */
    public static Booking managerBook(Room room, LocalDate date, LocalTime start, LocalTime end, String who) {
        if (!isFree(room, date, start, end)) return null;
        Booking b = new Booking(nextId++, room, date, start, end, who, "Booked by a manager",
                Booking.Status.CONFIRMED);
        BOOKINGS.add(b);
        return b;
    }

    /** A manager cancels any booking. The slot is free straight away. */
    public static void cancelBooking(Booking b) {
        BOOKINGS.remove(b);
    }

    /** Upcoming requests waiting for a manager. false = booking requests, true = cancellation requests. */
    public static List<Booking> requests(boolean cancellations) {
        Booking.Status wanted = cancellations ? Booking.Status.CANCELLATION_REQUESTED : Booking.Status.PENDING;
        List<Booking> out = new ArrayList<>();
        for (Booking b : BOOKINGS) {
            if (b.status == wanted && !b.isPast()) out.add(b);
        }
        sortByTime(out);
        return out;
    }

    /** All upcoming requests of both kinds: the number shown on the Requests button. */
    public static int requestCount() {
        return requests(false).size() + requests(true).size();
    }

    /** For a booking request, the booking that already holds the room, or null if there is no clash. */
    public static Booking clashFor(Booking request) {
        if (request.status != Booking.Status.PENDING) return null;
        return holder(request.room, request.date, request.start, request.end);
    }

    /** Approve a request. Returns false if it would clash with another booking. */
    public static boolean approve(Booking b) {
        if (b.status == Booking.Status.PENDING) {
            if (clashFor(b) != null) return false;
            b.status = Booking.Status.CONFIRMED;
            return true;
        }
        if (b.status == Booking.Status.CANCELLATION_REQUESTED) {
            BOOKINGS.remove(b);
        }
        return true;
    }

    /** Reject a request. A rejected booking request is removed; a rejected cancellation leaves the booking Confirmed. */
    public static void reject(Booking b) {
        if (b.status == Booking.Status.PENDING) {
            BOOKINGS.remove(b);
        } else if (b.status == Booking.Status.CANCELLATION_REQUESTED) {
            b.status = Booking.Status.CONFIRMED;
        }
    }
}
