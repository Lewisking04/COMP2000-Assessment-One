package com.example.roombooking;

/** Keys for the data passed between screens. */
public final class Extras {
    private Extras() { }

    public static final String DATE = "extra_date";            // date, e.g. 2026-10-27
    public static final String START = "extra_start";          // HH:mm
    public static final String DURATION = "extra_duration";    // minutes
    public static final String ROOM_ID = "extra_room_id";
    public static final String OPEN_BOOKINGS = "extra_open_bookings";
    public static final String BOOKING_ID = "extra_booking_id";    // the request that was just sent
    public static final String SHOW_WITHDRAWN = "extra_show_withdrawn";   // My bookings shows "Request withdrawn"
}
