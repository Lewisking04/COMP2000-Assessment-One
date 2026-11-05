package com.example.comp2000roombookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Confirm request screen. Sends the request, or goes to Room no longer free if there is a clash. */
public class ConfirmRequestActivity extends AppCompatActivity {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_request);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // getIntent() is the Intent that started this screen; its extras hold the chosen slot and room.
        final Intent in = getIntent();
        final LocalDate date = LocalDate.parse(in.getStringExtra(Extras.DATE));
        final LocalTime start = LocalTime.parse(in.getStringExtra(Extras.START));
        int minutes = in.getIntExtra(Extras.DURATION, 60);
        final LocalTime end = start.plusMinutes(minutes);
        final Room room = DemoData.find(in.getStringExtra(Extras.ROOM_ID));

        ((TextView) findViewById(R.id.tv_room_name)).setText(room.name);
        ((TextView) findViewById(R.id.tv_room_details)).setText(room.details());
        ((TextView) findViewById(R.id.tv_when)).setText(date.format(DAY));
        ((TextView) findViewById(R.id.tv_time)).setText(
                getString(R.string.time_range_duration, start.toString(), end.toString(), minutes / 60.0));
        final EditText etPurpose = findViewById(R.id.et_purpose);

        // finish() closes this screen and returns to the one before it.
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String purpose = etPurpose.getText().toString().trim();
                // requestBooking returns the new Booking, or null if the room was taken.
                Booking booking = DemoData.requestBooking(room, date, start, end, purpose);
                // Success goes to Request sent, a clash goes to Room no longer free
                Intent next = new Intent(ConfirmRequestActivity.this,
                        booking == null ? RoomUnavailableActivity.class : RequestSentActivity.class);
                next.putExtras(in);            // pass every extra we received on to the next screen
                if (booking != null) next.putExtra(Extras.BOOKING_ID, booking.id);   // so Request sent can cancel it
                startActivity(next);
                if (booking != null) finish();   // success: remove this screen so Back does not resend the request
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
