package com.example.roombooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Request sent screen. Shows the status Pending approval. */
public class RequestSentActivity extends AppCompatActivity {

    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_sent);

        Intent in = getIntent();
        LocalDate date = LocalDate.parse(in.getStringExtra(Extras.DATE));
        LocalTime start = LocalTime.parse(in.getStringExtra(Extras.START));
        LocalTime end = start.plusMinutes(in.getIntExtra(Extras.DURATION, 60));
        Room room = DemoData.find(in.getStringExtra(Extras.ROOM_ID));

        ((TextView) findViewById(R.id.tv_details)).setText(getString(R.string.sent_details,
                room.name, date.format(SHORT_DAY), start.toString(), end.toString()));

        findViewById(R.id.btn_view_bookings).setOnClickListener(v -> backToMain(true));
        findViewById(R.id.btn_find_another).setOnClickListener(v -> backToMain(false));
    }

    private void backToMain(boolean openBookings) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(Extras.OPEN_BOOKINGS, openBookings);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}
