package com.example.roombooking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Find a room screen. Pick a date, start time and length. Portrait opens Free rooms, landscape shows the list beside the form. */
public class FindRoomFragment extends Fragment {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);
    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.UK);

    private LocalDate date;
    private LocalTime start;
    private int durationMinutes = 60;

    private Button btnDate;
    private Button btnStart;
    private RadioGroup rgDuration;
    private TextView tvSummary;

    // These only exist in layout-land (the second pane)
    private RecyclerView roomsList;
    private TextView tvRoomsCount;
    private TextView tvRoomsEmpty;
    private RoomAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnDate = view.findViewById(R.id.btn_date);
        btnStart = view.findViewById(R.id.btn_start);
        rgDuration = view.findViewById(R.id.rg_duration);
        tvSummary = view.findViewById(R.id.tv_summary);
        Button btnShow = view.findViewById(R.id.btn_show_rooms);

        // Sensible defaults: the next working day at 14:00 for one hour
        date = nextWorkingDay(LocalDate.now().plusDays(1));
        start = LocalTime.of(14, 0);
        if (savedInstanceState != null) {
            date = LocalDate.parse(savedInstanceState.getString("date", date.toString()));
            start = LocalTime.parse(savedInstanceState.getString("start", start.toString()));
            durationMinutes = savedInstanceState.getInt("duration", 60);
        }

        btnDate.setOnClickListener(v -> pickDate());
        btnStart.setOnClickListener(v -> pickTime());

        rgDuration.check(radioIdFor(durationMinutes));
        rgDuration.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_30) durationMinutes = 30;
            else if (checkedId == R.id.rb_60) durationMinutes = 60;
            else if (checkedId == R.id.rb_120) durationMinutes = 120;
            else if (checkedId == R.id.rb_180) durationMinutes = 180;
            updateSummary();
        });

        roomsList = view.findViewById(R.id.rooms_list);          // null in portrait
        tvRoomsCount = view.findViewById(R.id.tv_rooms_count);
        tvRoomsEmpty = view.findViewById(R.id.tv_rooms_empty);
        if (roomsList != null) {
            adapter = new RoomAdapter(room -> openConfirm(room));
            roomsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            roomsList.setAdapter(adapter);
        }

        btnShow.setOnClickListener(v -> showFreeRooms());
        updateSummary();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("date", date.toString());
        outState.putString("start", start.toString());
        outState.putInt("duration", durationMinutes);
    }

    private int radioIdFor(int minutes) {
        switch (minutes) {
            case 30: return R.id.rb_30;
            case 120: return R.id.rb_120;
            case 180: return R.id.rb_180;
            default: return R.id.rb_60;
        }
    }

    private static LocalDate nextWorkingDay(LocalDate d) {
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    /** Date picker limited to today and the next 29 days, Monday to Friday. */
    private void pickDate() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (DatePicker picker, int year, int month, int dayOfMonth) -> {
                    LocalDate chosen = LocalDate.of(year, month + 1, dayOfMonth);
                    if (chosen.getDayOfWeek() == DayOfWeek.SATURDAY
                            || chosen.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        message(R.string.error_weekend);
                        return;
                    }
                    date = chosen;
                    updateSummary();
                }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();
        max.add(Calendar.DAY_OF_YEAR, DemoData.MAX_DAYS_AHEAD);
        dialog.getDatePicker().setMinDate(min.getTimeInMillis() - 1000);
        dialog.getDatePicker().setMaxDate(max.getTimeInMillis());
        dialog.show();
    }

    private void pickTime() {
        TimePickerDialog dialog = new TimePickerDialog(requireContext(),
                (TimePicker picker, int hour, int minute) -> {
                    if (minute != 0 && minute != 30) {
                        message(R.string.error_half_hour);          // say what is wrong and how to fix it
                        return;
                    }
                    LocalTime chosen = LocalTime.of(hour, minute);
                    if (chosen.isBefore(DemoData.OPEN) || chosen.isAfter(DemoData.LAST_START)) {
                        message(R.string.error_opening_hours);
                        return;
                    }
                    start = chosen;
                    updateSummary();
                }, start.getHour(), start.getMinute(), true);
        dialog.show();
    }

    private void updateSummary() {
        btnDate.setText(date.format(DAY));
        btnStart.setText(start.format(TIME));
        LocalTime end = start.plusMinutes(durationMinutes);
        tvSummary.setText(getString(R.string.summary_format, date.format(SHORT_DAY),
                start.format(TIME), end.format(TIME)));
    }

    private void showFreeRooms() {
        LocalTime end = start.plusMinutes(durationMinutes);
        if (end.isAfter(DemoData.CLOSE)) {
            message(R.string.error_too_late);
            return;
        }
        if (roomsList == null) {
            // Portrait: the list is its own screen. The values go in the Intent as extras.
            Intent i = new Intent(requireContext(), FreeRoomsActivity.class);
            i.putExtra(Extras.DATE, date.toString());
            i.putExtra(Extras.START, start.toString());
            i.putExtra(Extras.DURATION, durationMinutes);
            startActivity(i);
        } else {
            List<Room> free = DemoData.freeRooms(date, start, end);
            adapter.submit(free);
            tvRoomsCount.setText(getString(R.string.rooms_free_count, free.size(), DemoData.ROOMS.size()));
            tvRoomsEmpty.setVisibility(free.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void openConfirm(Room room) {
        Intent i = new Intent(requireContext(), ConfirmRequestActivity.class);
        i.putExtra(Extras.DATE, date.toString());
        i.putExtra(Extras.START, start.toString());
        i.putExtra(Extras.DURATION, durationMinutes);
        i.putExtra(Extras.ROOM_ID, room.id);
        startActivity(i);
    }

    /** Snackbar message. */
    private void message(int resId) {
        Snackbar.make(requireView(), resId, Snackbar.LENGTH_LONG).show();
    }
}
