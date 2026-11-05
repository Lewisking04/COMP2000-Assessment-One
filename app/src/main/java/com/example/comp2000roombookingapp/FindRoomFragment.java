package com.example.comp2000roombookingapp;

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

    // Formatters turn a date or time into text for the buttons, e.g. "Wed 4 Nov 2026" and "14:00".
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);
    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.UK);

    // The three choices the user makes. Everything else on this page is built from these.
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

    // Step 1: turn the XML layout into real views
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

        // Sensible defaults: the next working day at 14:00 for one hour, so the user can often just press the button.
        date = nextWorkingDay(LocalDate.now().plusDays(1));
        start = LocalTime.of(14, 0);

        // Tapping a button opens the matching picker pop-up.
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime();
            }
        });

        // A RadioGroup allows exactly one choice. Here the choices are 30 minutes, 1 hour, 2 hours and 3 hours.
        rgDuration.check(radioIdFor(durationMinutes));
        // Runs each time a different radio button is chosen; it stores the length in minutes.
        rgDuration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_30) durationMinutes = 30;
                else if (checkedId == R.id.rb_60) durationMinutes = 60;
                else if (checkedId == R.id.rb_120) durationMinutes = 120;
                else if (checkedId == R.id.rb_180) durationMinutes = 180;
                updateSummary();
            }
        });

        // These views exist only in the landscape layout. In portrait findViewById returns null, so we check for null.
        roomsList = view.findViewById(R.id.rooms_list);          // null in portrait
        tvRoomsCount = view.findViewById(R.id.tv_rooms_count);
        tvRoomsEmpty = view.findViewById(R.id.tv_rooms_empty);
        if (roomsList != null) {
            adapter = new RoomAdapter(new RoomAdapter.OnRoomClickListener() {
                @Override
                public void onRoomClick(Room room) {
                    openConfirm(room);
                }
            });
            roomsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            roomsList.setAdapter(adapter);
        }

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFreeRooms();
            }
        });
        updateSummary();
    }

    // Converts minutes into the matching radio button id (the reverse of the listener above).
    private int radioIdFor(int minutes) {
        switch (minutes) {
            case 30: return R.id.rb_30;
            case 120: return R.id.rb_120;
            case 180: return R.id.rb_180;
            default: return R.id.rb_60;
        }
    }

    // Skips Saturday and Sunday: rooms are only bookable Monday to Friday.
    private static LocalDate nextWorkingDay(LocalDate d) {
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    /** Date picker limited to today and the next 29 days, Monday to Friday. */
    private void pickDate() {
        // The listener runs when the user presses OK in the calendar. (Android counts months from 0, hence +1.)
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker picker, int year, int month, int dayOfMonth) {
                        LocalDate chosen = LocalDate.of(year, month + 1, dayOfMonth);
                        if (chosen.getDayOfWeek() == DayOfWeek.SATURDAY
                                || chosen.getDayOfWeek() == DayOfWeek.SUNDAY) {
                            message(R.string.error_weekend);
                            return;
                        }
                        date = chosen;
                        updateSummary();
                    }
                }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        // Limit the calendar to today up to 29 days ahead, so earlier or later days cannot be selected.
        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();
        max.add(Calendar.DAY_OF_YEAR, DemoData.MAX_DAYS_AHEAD);
        dialog.getDatePicker().setMinDate(min.getTimeInMillis() - 1000);
        dialog.getDatePicker().setMaxDate(max.getTimeInMillis());
        dialog.show();
    }

    /** Time picker. Only 30-minute steps inside opening hours are accepted; otherwise a Snackbar says why. */
    private void pickTime() {
        // The listener runs when the user presses OK in the clock. "true" at the end means a 24-hour clock.
        TimePickerDialog dialog = new TimePickerDialog(requireContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker picker, int hour, int minute) {
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
                    }
                }, start.getHour(), start.getMinute(), true);
        dialog.show();
    }

    // Refreshes the two buttons and the line "Wed 4 Nov, 14:00 to 15:00" whenever a choice changes.
    private void updateSummary() {
        btnDate.setText(date.format(DAY));
        btnStart.setText(start.format(TIME));
        LocalTime end = start.plusMinutes(durationMinutes);
        tvSummary.setText(getString(R.string.summary_format, date.format(SHORT_DAY),
                start.format(TIME), end.format(TIME)));
    }

    // Runs when "Show free rooms" is pressed.
    private void showFreeRooms() {
        LocalTime end = start.plusMinutes(durationMinutes);
        if (end.isAfter(DemoData.CLOSE)) {
            message(R.string.error_too_late);
            return;
        }
        if (roomsList == null) {
            // Portrait: the list is its own screen. An explicit Intent names the screen to open,
            // and the chosen values go in as extras.
            Intent i = new Intent(requireContext(), FreeRoomsActivity.class);
            i.putExtra(Extras.DATE, date.toString());
            i.putExtra(Extras.START, start.toString());
            i.putExtra(Extras.DURATION, durationMinutes);
            startActivity(i);
        } else {
            // Landscape: no new screen. The list appears in the right-hand pane of this same page.
            List<Room> free = DemoData.freeRooms(date, start, end);
            adapter.submit(free);
            tvRoomsCount.setText(getString(R.string.rooms_free_count, free.size(), DemoData.ROOMS.size()));
            tvRoomsEmpty.setVisibility(free.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    // Landscape only: tapping Request on a room goes to the Confirm screen, carrying the slot and room as extras.
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
