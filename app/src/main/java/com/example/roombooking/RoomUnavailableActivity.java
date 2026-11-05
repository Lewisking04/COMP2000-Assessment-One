package com.example.roombooking;

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

/** Room no longer free screen. Explains what happened and offers other rooms or a new time. */
public class RoomUnavailableActivity extends AppCompatActivity {

    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_unavailable);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent in = getIntent();
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

        // Other rooms for the same slot (not the one that failed)
        List<Room> alternatives = DemoData.freeRooms(date, start, end);
        alternatives.remove(room);
        RoomAdapter adapter = new RoomAdapter(chosen -> {
            Intent next = new Intent(RoomUnavailableActivity.this, ConfirmRequestActivity.class);
            next.putExtras(in);
            next.putExtra(Extras.ROOM_ID, chosen.id);
            startActivity(next);
            finish();
        });
        RecyclerView list = findViewById(R.id.rooms_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        adapter.submit(alternatives);

        // Say when the first-choice room is free again
        TextView tvNext = findViewById(R.id.tv_next_free);
        LocalTime next = DemoData.nextFreeStart(room, date, end, minutes);
        if (next != null) {
            tvNext.setText(getString(R.string.next_free, room.name, next.toString()));
            tvNext.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.btn_change).setOnClickListener(v -> {
            Intent i = new Intent(RoomUnavailableActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
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
