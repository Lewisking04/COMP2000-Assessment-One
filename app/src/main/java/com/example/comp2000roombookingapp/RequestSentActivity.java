package com.example.comp2000roombookingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    // Kept as fields because the cancel dialog (a separate method) needs them after onCreate has finished.
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
        bookingId = in.getLongExtra(Extras.BOOKING_ID, -1);     // -1 is the fallback if no id was sent

        ((TextView) findViewById(R.id.tv_details)).setText(getString(R.string.sent_details,
                room.name, date.format(SHORT_DAY), start.toString(), end.toString()));

        // The first two buttons both go home; the booleans say which page to show and whether to show the message.
        findViewById(R.id.btn_view_bookings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMain(true, false);
            }
        });
        findViewById(R.id.btn_find_another).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMain(false, false);
            }
        });
        findViewById(R.id.btn_cancel_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmCancel();
            }
        });
    }

    /** Cancel dialog for a request that is still pending. Keep booking is the main button. */
    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_withdraw_title)
                .setMessage(getString(R.string.dialog_withdraw_message, room.name))
                .setPositiveButton(R.string.keep_booking, null)
                // The negative button is the destructive one. Its code runs only if the user confirms.
                .setNegativeButton(R.string.withdraw, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Booking booking = DemoData.findBooking(bookingId);
                        if (booking != null) DemoData.withdraw(booking);
                        backToMain(true, true);          // "Request removed; list updated"
                    }
                })
                .show();
    }

    /** Opens the employee home on the chosen page and closes this screen. */
    private void backToMain(boolean openBookings, boolean showWithdrawn) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(Extras.OPEN_BOOKINGS, openBookings);
        i.putExtra(Extras.SHOW_WITHDRAWN, showWithdrawn);
        startActivity(i);
        finish();
    }
}
