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

/** Room no longer free screen. Shows other free rooms and when the first choice is next free. */
public class RoomUnavailableActivity extends AppCompatActivity {

    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_unavailable);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent in = getIntent();
        LocalDate date = LocalDate.parse(in.getStringExtra(Extras.DATE));
        LocalTime start = LocalTime.parse(in.getStringExtra(Extras.START));
        int minutes = in.getIntExtra(Extras.DURATION, 60);
        LocalTime end = start.plusMinutes(minutes);
        Room room = DemoData.find(in.getStringExtra(Extras.ROOM_ID));

        ((TextView) findViewById(R.id.tv_title)).setText(getString(R.string.unavailable_title, room.name));
        ((TextView) findViewById(R.id.tv_message)).setText(getString(R.string.unavailable_message,
                date.format(SHORT_DAY), start.toString(), end.toString()));
        ((TextView) findViewById(R.id.tv_alt_heading)).setText(
                getString(R.string.still_free_heading, start.toString(), end.toString()));

        // Other rooms for the same slot (not the one that failed). This reuses RoomAdapter;
        // code as Free rooms; tapping a room goes to Confirm request with the new room.
        List<Room> alternatives = DemoData.freeRooms(date, start, end);
        alternatives.remove(room);
        RoomAdapter adapter = new RoomAdapter(new RoomAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(Room chosen) {
                Intent confirm = new Intent(RoomUnavailableActivity.this, ConfirmRequestActivity.class);
                confirm.putExtras(in);
                confirm.putExtra(Extras.ROOM_ID, chosen.id);
                startActivity(confirm);
                finish();
            }
        });
        RecyclerView list = findViewById(R.id.rooms_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        adapter.submit(alternatives);

        // Tell the user when the first-choice room is next free for the same length. The line stays hidden if never.
        TextView tvNext = findViewById(R.id.tv_next_free);
        LocalTime next = DemoData.nextFreeStart(room, date, end, minutes);
        if (next != null) {
            tvNext.setText(getString(R.string.next_free, room.name, next.toString()));
            tvNext.setVisibility(View.VISIBLE);
        }

        // "Change date or time": open the home screen again, which starts on Find a room, and close this one.
        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RoomUnavailableActivity.this, MainActivity.class));
                finish();
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
