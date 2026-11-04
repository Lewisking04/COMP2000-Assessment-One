package com.example.roombooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Free rooms screen. Only rooms free for the chosen slot are listed. */
public class FreeRoomsActivity extends AppCompatActivity {

    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_rooms);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        LocalDate date = LocalDate.parse(getIntent().getStringExtra(Extras.DATE));
        LocalTime start = LocalTime.parse(getIntent().getStringExtra(Extras.START));
        int minutes = getIntent().getIntExtra(Extras.DURATION, 60);
        LocalTime end = start.plusMinutes(minutes);

        TextView tvSlot = findViewById(R.id.tv_slot);
        tvSlot.setText(getString(R.string.slot_format, date.format(SHORT_DAY), start.toString(), end.toString()));
        findViewById(R.id.btn_change).setOnClickListener(v -> finish());   // back to Find a room

        List<Room> free = DemoData.freeRooms(date, start, end);
        TextView tvCount = findViewById(R.id.tv_count);
        tvCount.setText(getString(R.string.rooms_free_count, free.size(), DemoData.ROOMS.size()));
        TextView tvEmpty = findViewById(R.id.tv_empty);
        tvEmpty.setVisibility(free.isEmpty() ? TextView.VISIBLE : TextView.GONE);

        RoomAdapter adapter = new RoomAdapter(room -> {
            Intent i = new Intent(this, ConfirmRequestActivity.class);
            i.putExtra(Extras.DATE, date.toString());
            i.putExtra(Extras.START, start.toString());
            i.putExtra(Extras.DURATION, minutes);
            i.putExtra(Extras.ROOM_ID, room.id);
            startActivity(i);
        }, getString(R.string.request));
        RecyclerView list = findViewById(R.id.rooms_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        adapter.submit(free);
    }
}
