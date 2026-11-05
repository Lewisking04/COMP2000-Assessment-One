package com.example.comp2000roombookingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Manager room detail screen. Shows one room for the chosen day and its bookings, with a cancel dialog. */
public class RoomDetailActivity extends AppCompatActivity {

    private static final int REQUEST_BOOK = 100;     // identifies the answer that comes back from Book a room
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);

    private Room room;
    private LocalDate date;
    private LocalTime start;

    private TextView tvStatus;
    private TextView tvTimelineHeading;
    private TextView tvTimelineEmpty;
    private ListView lvTimeline;
    private TextView tvSelected;
    private Button btnCancel;

    private List<Booking> dayBookings = new ArrayList<>();
    private Booking selected;         // the booking whose details are shown, or null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Read what All rooms sent: which room, day and hour.
        Intent in = getIntent();
        room = DemoData.find(in.getStringExtra(Extras.ROOM_ID));
        date = LocalDate.parse(in.getStringExtra(Extras.DATE));
        start = LocalTime.parse(in.getStringExtra(Extras.START));

        ((TextView) findViewById(R.id.tv_detail_room)).setText(room.name);
        ((TextView) findViewById(R.id.tv_detail_info)).setText(room.details());
        tvStatus = findViewById(R.id.tv_detail_status);
        tvTimelineHeading = findViewById(R.id.tv_timeline_heading);
        tvTimelineEmpty = findViewById(R.id.tv_timeline_empty);
        lvTimeline = findViewById(R.id.lv_timeline);
        tvSelected = findViewById(R.id.tv_selected_booking);
        btnCancel = findViewById(R.id.btn_cancel_booking);

        // Tapping a booking in the timeline selects it.
        lvTimeline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selected = dayBookings.get(position);
                showSelected();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmCancel();
            }
        });

        // Book another time opens Book a room. startActivityForResult lets that screen tell us it made a booking,
        // so this screen can close and the manager lands back on All rooms.
        findViewById(R.id.btn_book_other_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RoomDetailActivity.this, BookRoomActivity.class);
                i.putExtra(Extras.ROOM_ID, room.id);
                i.putExtra(Extras.DATE, date.toString());
                i.putExtra(Extras.START, start.toString());
                startActivityForResult(i, REQUEST_BOOK);
            }
        });

        // Start with the booking that holds the chosen hour selected, if there is one.
        selected = DemoData.holder(room, date, start, start.plusHours(1));
        refresh();
    }

    // Redraws the status line, the timeline and the details after anything changes.
    private void refresh() {
        LocalTime end = start.plusHours(1);
        Booking holder = DemoData.holder(room, date, start, end);
        if (holder == null) {
            tvStatus.setText(getString(R.string.detail_vacant, start.toString(), end.toString()));
            tvStatus.setBackgroundResource(R.drawable.bg_chip_confirmed);
        } else {
            tvStatus.setText(getString(R.string.detail_booked, start.toString(), end.toString(), holder.booker));
            tvStatus.setBackgroundResource(R.drawable.bg_chip_cancellation);
        }

        tvTimelineHeading.setText(getString(R.string.timeline_heading, date.format(DAY)));
        dayBookings = DemoData.bookingsOn(room, date);
        List<String> lines = new ArrayList<>();
        for (Booking b : dayBookings) {
            lines.add(getString(R.string.timeline_line, b.start.toString(), b.end.toString(), b.booker));
        }
        // ArrayAdapter turns each String into a row of the ListView.
        lvTimeline.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lines));
        tvTimelineEmpty.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);

        // If the selected booking was removed, forget it.
        if (selected != null && DemoData.findBooking(selected.id) == null) selected = null;
        showSelected();
    }

    // Shows the details of the selected booking and the Cancel button, or a hint when nothing is selected.
    private void showSelected() {
        if (selected == null) {
            tvSelected.setText(R.string.no_booking_selected);
            btnCancel.setVisibility(View.GONE);      // Cancel only exists for a booked slot
        } else {
            tvSelected.setText(getString(R.string.selected_booking_format, selected.booker,
                    selected.start.toString(), selected.end.toString(), selected.purpose));
            btnCancel.setVisibility(View.VISIBLE);
        }
    }

    /** Cancel dialog. Keep booking is the main button so nothing is cancelled by accident. */
    private void confirmCancel() {
        final Booking target = selected;
        if (target == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_manager_cancel_title)
                .setMessage(getString(R.string.dialog_manager_cancel_message, target.room.name,
                        target.start.toString(), target.end.toString(), target.booker))
                .setPositiveButton(R.string.keep_booking, null)
                .setNegativeButton(R.string.cancel_booking, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DemoData.cancelBooking(target);       // the slot is free straight away
                        selected = null;
                        refresh();
                        Snackbar.make(findViewById(android.R.id.content), R.string.message_booking_cancelled,
                                Snackbar.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    // Called when Book a room closes. RESULT_OK means a booking was made, so this screen is no longer needed.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BOOK && resultCode == RESULT_OK) {
            finish();
        } else {
            refresh();
        }
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
