package com.example.roombooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Request sent screen. Shows Pending approval and a Cancel request button. */
public class RequestSentActivity extends AppCompatActivity {

    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    private long bookingId;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_sent);

        Intent in = getIntent();
        LocalDate date = LocalDate.parse(in.getStringExtra(Extras.DATE));
        LocalTime start = LocalTime.parse(in.getStringExtra(Extras.START));
        LocalTime end = start.plusMinutes(in.getIntExtra(Extras.DURATION, 60));
        room = DemoData.find(in.getStringExtra(Extras.ROOM_ID));
        bookingId = in.getLongExtra(Extras.BOOKING_ID, -1);

        ((TextView) findViewById(R.id.tv_details)).setText(getString(R.string.sent_details,
                room.name, date.format(SHORT_DAY), start.toString(), end.toString()));

        findViewById(R.id.btn_view_bookings).setOnClickListener(v -> backToMain(true, false));
        findViewById(R.id.btn_find_another).setOnClickListener(v -> backToMain(false, false));
        findViewById(R.id.btn_cancel_request).setOnClickListener(v -> confirmCancel());
    }

    /** Cancel dialog for a request that is still pending. Keep booking is the main button. */
    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_withdraw_title)
                .setMessage(getString(R.string.dialog_withdraw_message, room.name))
                .setPositiveButton(R.string.keep_booking, null)
                .setNegativeButton(R.string.withdraw, (dialog, which) -> {
                    Booking booking = DemoData.findBooking(bookingId);
                    if (booking != null) DemoData.withdraw(booking);
                    backToMain(true, true);          // "Request removed; list updated"
                })
                .show();
    }

    /** CLEAR_TOP goes back to the open MainActivity instead of making a second one. */
    private void backToMain(boolean openBookings, boolean showWithdrawn) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(Extras.OPEN_BOOKINGS, openBookings);
        i.putExtra(Extras.SHOW_WITHDRAWN, showWithdrawn);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}
