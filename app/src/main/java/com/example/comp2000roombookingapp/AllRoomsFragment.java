package com.example.comp2000roombookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Manager all rooms page. Lists every room as Vacant or Booked, with buttons to change the day and hour. */
public class AllRoomsFragment extends Fragment {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);

    private LocalDate date;
    private LocalTime start = LocalTime.of(14, 0);

    private TextView tvDate;
    private TextView tvHour;
    private TextView tvCount;
    private RoomStatusAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDate = view.findViewById(R.id.tv_mgr_date);
        tvHour = view.findViewById(R.id.tv_mgr_hour);
        tvCount = view.findViewById(R.id.tv_mgr_count);

        // Start on today, or on Monday when today is a weekend (rooms are only bookable Monday to Friday).
        date = LocalDate.now();
        while (isWeekend(date)) date = date.plusDays(1);

        // Tapping a room opens Room detail with the room and slot as Intent extras
        adapter = new RoomStatusAdapter(new RoomAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(Room room) {
                Intent i = new Intent(requireContext(), RoomDetailActivity.class);
                i.putExtra(Extras.ROOM_ID, room.id);
                i.putExtra(Extras.DATE, date.toString());
                i.putExtra(Extras.START, start.toString());
                startActivity(i);
            }
        });
        RecyclerView list = view.findViewById(R.id.mgr_rooms_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        view.findViewById(R.id.btn_prev_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveDay(-1);
            }
        });
        view.findViewById(R.id.btn_next_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveDay(1);
            }
        });
        view.findViewById(R.id.btn_earlier).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveHour(-1);
            }
        });
        view.findViewById(R.id.btn_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveHour(1);
            }
        });
        // Book a room opens the booking screen at the slot shown
        view.findViewById(R.id.btn_book_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(requireContext(), BookRoomActivity.class);
                i.putExtra(Extras.DATE, date.toString());
                i.putExtra(Extras.START, start.toString());
                startActivity(i);
            }
        });
        refresh();
    }

    // Runs every time this page comes back into view, so the list is up to date
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) refresh();
    }

    private boolean isWeekend(LocalDate d) {
        return d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /** Moves to the previous or next working day (today up to 29 days ahead). */
    private void moveDay(int step) {
        LocalDate d = date.plusDays(step);
        while (isWeekend(d)) d = d.plusDays(step);
        LocalDate today = LocalDate.now();
        if (d.isBefore(today) || d.isAfter(today.plusDays(DemoData.MAX_DAYS_AHEAD))) {
            message(R.string.error_day_range);
            return;
        }
        date = d;
        refresh();
    }

    /** Moves the one-hour slot earlier or later, inside opening hours (09:00 to 17:00). */
    private void moveHour(int step) {
        LocalTime t = start.plusHours(step);
        if (t.isBefore(DemoData.OPEN) || t.plusHours(1).isAfter(DemoData.CLOSE)) {
            message(R.string.error_opening_hours);
            return;
        }
        start = t;
        refresh();
    }

    // Redraws the date, hour, count and room list
    private void refresh() {
        LocalTime end = start.plusHours(1);
        tvDate.setText(date.format(DAY));
        tvHour.setText(getString(R.string.mgr_hour_format, start.toString(), end.toString()));
        adapter.submit(DemoData.ROOMS, date, start, end);
        int free = DemoData.freeRooms(date, start, end).size();
        tvCount.setText(getString(R.string.rooms_free_count, free, DemoData.ROOMS.size()));
    }

    private void message(int resId) {
        Snackbar.make(requireView(), resId, Snackbar.LENGTH_LONG).show();
    }
}
