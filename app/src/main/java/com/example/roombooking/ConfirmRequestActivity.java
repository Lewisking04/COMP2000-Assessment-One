package com.example.roombooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Confirm request screen. Shows a summary, then sends the request. */
public class ConfirmRequestActivity extends AppCompatActivity {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_request);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent in = getIntent();
        LocalDate date = LocalDate.parse(in.getStringExtra(Extras.DATE));
        LocalTime start = LocalTime.parse(in.getStringExtra(Extras.START));
        int minutes = in.getIntExtra(Extras.DURATION, 60);
        LocalTime end = start.plusMinutes(minutes);
        Room room = DemoData.find(in.getStringExtra(Extras.ROOM_ID));

        ((TextView) findViewById(R.id.tv_room_name)).setText(room.name);
        ((TextView) findViewById(R.id.tv_room_details)).setText(room.details());
        ((TextView) findViewById(R.id.tv_when)).setText(date.format(DAY));
        ((TextView) findViewById(R.id.tv_time)).setText(
                getString(R.string.time_range_duration, start.toString(), end.toString(), minutes / 60.0));
        TextInputEditText etPurpose = findViewById(R.id.et_purpose);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_send).setOnClickListener(v -> {
            String purpose = etPurpose.getText() == null ? "" : etPurpose.getText().toString().trim();
            Booking booking = DemoData.requestBooking(room, date, start, end, purpose);
            Intent next = new Intent(this,
                    booking == null ? RoomUnavailableActivity.class : RequestSentActivity.class);
            next.putExtras(in);            // pass the slot and room along
            startActivity(next);
            if (booking != null) finish();
        });
    }
}
