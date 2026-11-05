package com.example.comp2000roombookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Free rooms screen. Lists the rooms free for the date and time picked on the last page. */
public class FreeRoomsActivity extends AppCompatActivity {

    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_rooms);
        // Shows the Up arrow in the top bar. The click itself is handled in onOptionsItemSelected at the bottom.
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Read the values sent by Find a room. Dates and times travel as text and are parsed back.
        final LocalDate date = LocalDate.parse(getIntent().getStringExtra(Extras.DATE));
        final LocalTime start = LocalTime.parse(getIntent().getStringExtra(Extras.START));
        final int minutes = getIntent().getIntExtra(Extras.DURATION, 60);
        LocalTime end = start.plusMinutes(minutes);

        TextView tvSlot = findViewById(R.id.tv_slot);
        tvSlot.setText(getString(R.string.slot_format, date.format(SHORT_DAY), start.toString(), end.toString()));
        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // back to Find a room
            }
        });

        // Ask the sample data for the rooms free for the whole slot, then show "4 of 7 rooms free".
        List<Room> free = DemoData.freeRooms(date, start, end);
        TextView tvCount = findViewById(R.id.tv_count);
        tvCount.setText(getString(R.string.rooms_free_count, free.size(), DemoData.ROOMS.size()));
        TextView tvEmpty = findViewById(R.id.tv_empty);
        tvEmpty.setVisibility(free.isEmpty() ? View.VISIBLE : View.GONE);

        // What happens when a room's Request button is tapped: open the Confirm screen for that room.
        RoomAdapter adapter = new RoomAdapter(new RoomAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(Room room) {
                Intent i = new Intent(FreeRoomsActivity.this, ConfirmRequestActivity.class);
                i.putExtra(Extras.DATE, date.toString());
                i.putExtra(Extras.START, start.toString());
                i.putExtra(Extras.DURATION, minutes);
                i.putExtra(Extras.ROOM_ID, room.id);
                startActivity(i);
            }
        });
        // RecyclerView needs a LayoutManager, an Adapter and the data.
        RecyclerView list = findViewById(R.id.rooms_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        adapter.submit(free);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {   // the Up arrow in the action bar
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
