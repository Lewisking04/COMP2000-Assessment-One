package com.example.roombooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Find a room screen. Pick a date, start time and length, then see the free rooms. */
public class FindRoomFragment extends Fragment {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);
    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.UK);

    private LocalDate date;
    private LocalTime start;
    private int durationMinutes = 60;

    private TextInputEditText etDate;
    private TextInputEditText etStart;
    private TextView tvSummary;

    // Only in the wide (two-pane) layout
    private RecyclerView roomsList;
    private TextView tvRoomsCount;
    private TextView tvRoomsEmpty;
    private RoomAdapter adapter;

    public FindRoomFragment() {
        super(R.layout.fragment_find_room);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDate = view.findViewById(R.id.et_date);
        etStart = view.findViewById(R.id.et_start);
        tvSummary = view.findViewById(R.id.tv_summary);
        TextInputLayout dateLayout = view.findViewById(R.id.date_layout);
        TextInputLayout startLayout = view.findViewById(R.id.start_layout);
        ChipGroup chips = view.findViewById(R.id.chip_duration);
        MaterialButton btnShow = view.findViewById(R.id.btn_show_rooms);

        // Sensible defaults: the next working day at 14:00 for one hour
        date = nextWorkingDay(LocalDate.now().plusDays(1));
        start = LocalTime.of(14, 0);
        if (savedInstanceState != null) {
            date = LocalDate.parse(savedInstanceState.getString("date", date.toString()));
            start = LocalTime.parse(savedInstanceState.getString("start", start.toString()));
            durationMinutes = savedInstanceState.getInt("duration", 60);
        }

        etDate.setOnClickListener(v -> pickDate());
        dateLayout.setEndIconOnClickListener(v -> pickDate());
        etStart.setOnClickListener(v -> pickTime());
        startLayout.setEndIconOnClickListener(v -> pickTime());

        chips.check(chipIdFor(durationMinutes));
        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_30) durationMinutes = 30;
            else if (id == R.id.chip_60) durationMinutes = 60;
            else if (id == R.id.chip_120) durationMinutes = 120;
            else if (id == R.id.chip_180) durationMinutes = 180;
            updateSummary();
        });

        // Two-pane views only exist in layout-w600dp
        roomsList = view.findViewById(R.id.rooms_list);
        tvRoomsCount = view.findViewById(R.id.tv_rooms_count);
        tvRoomsEmpty = view.findViewById(R.id.tv_rooms_empty);
        if (roomsList != null) {
            adapter = new RoomAdapter(room -> openConfirm(room), getString(R.string.request));
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

    private int chipIdFor(int minutes) {
        switch (minutes) {
            case 30: return R.id.chip_30;
            case 120: return R.id.chip_120;
            case 180: return R.id.chip_180;
            default: return R.id.chip_60;
        }
    }

    private static LocalDate nextWorkingDay(LocalDate d) {
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    private void pickDate() {
        long todayUtc = MaterialDatePicker.todayInUtcMilliseconds();
        // Bookings can be made from today up to 30 days ahead
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(todayUtc)
                .setEnd(todayUtc + 30L * 24 * 60 * 60 * 1000)
                .setValidator(DateValidatorPointForward.from(todayUtc))
                .build();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pick_date)
                .setSelection(date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                .setCalendarConstraints(constraints)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            LocalDate chosen = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
            if (chosen.getDayOfWeek() == DayOfWeek.SATURDAY || chosen.getDayOfWeek() == DayOfWeek.SUNDAY) {
                message(R.string.error_weekend);
                return;
            }
            date = chosen;
            updateSummary();
        });
        picker.show(getParentFragmentManager(), "date_picker");
    }

    private void pickTime() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(start.getHour())
                .setMinute(start.getMinute())
                .setTitleText(R.string.pick_time)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            int minute = picker.getMinute();
            if (minute != 0 && minute != 30) {
                message(R.string.error_half_hour);
                return;
            }
            LocalTime chosen = LocalTime.of(picker.getHour(), minute);
            if (chosen.isBefore(DemoData.OPEN) || chosen.isAfter(DemoData.LAST_START)) {
                message(R.string.error_opening_hours);
                return;
            }
            start = chosen;
            updateSummary();
        });
        picker.show(getParentFragmentManager(), "time_picker");
    }

    private void updateSummary() {
        etDate.setText(date.format(DAY));
        etStart.setText(start.format(TIME));
        LocalTime end = start.plusMinutes(durationMinutes);
        tvSummary.setText(getString(R.string.summary_format, date.format(SHORT_DAY),
                start.format(TIME), end.format(TIME)));
    }

    private void showFreeRooms() {
        LocalTime end = start.plusMinutes(durationMinutes);
        if (end.isAfter(DemoData.CLOSE)) {
            message(R.string.error_too_late);   // say what is wrong and how to fix it
            return;
        }
        if (roomsList == null) {
            // Phone portrait: the list opens as its own screen
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

    private void message(int resId) {
        Snackbar.make(requireView(), resId, Snackbar.LENGTH_LONG).show();
    }
}
