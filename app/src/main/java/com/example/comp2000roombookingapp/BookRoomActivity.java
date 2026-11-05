package com.example.comp2000roombookingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

/** Manager book a room screen. Pick the room, date, time and who it is for. Says if the room is free. */
public class BookRoomActivity extends AppCompatActivity {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);
    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    // The manager's choices. The message is redrawn whenever one changes.
    private Room room = DemoData.ROOMS.get(0);
    private LocalDate date;
    private LocalTime start = LocalTime.of(14, 0);
    private int durationMinutes = 60;

    private Spinner spRoom;
    private Button btnDate;
    private Button btnStart;
    private RadioGroup rgDuration;
    private RadioGroup rgWho;
    private View llEmployee;
    private AutoCompleteTextView actvEmployee;
    private TextView tvClash;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_room);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spRoom = findViewById(R.id.sp_room);
        btnDate = findViewById(R.id.btn_book_date);
        btnStart = findViewById(R.id.btn_book_start);
        rgDuration = findViewById(R.id.rg_book_duration);
        rgWho = findViewById(R.id.rg_who);
        llEmployee = findViewById(R.id.ll_employee);
        actvEmployee = findViewById(R.id.actv_employee);
        tvClash = findViewById(R.id.tv_clash);
        btnConfirm = findViewById(R.id.btn_confirm_booking);

        // Starting values: what the last screen sent, otherwise today (or Monday on a weekend) at 14:00
        Intent in = getIntent();
        date = LocalDate.now();
        while (isWeekend(date)) date = date.plusDays(1);
        if (in.getStringExtra(Extras.DATE) != null) date = LocalDate.parse(in.getStringExtra(Extras.DATE));
        if (in.getStringExtra(Extras.START) != null) start = LocalTime.parse(in.getStringExtra(Extras.START));
        Room sent = DemoData.find(in.getStringExtra(Extras.ROOM_ID));
        if (sent != null) room = sent;

        // Room selector: a Spinner (drop-down) filled with the room names
        String[] names = new String[DemoData.ROOMS.size()];
        for (int i = 0; i < names.length; i++) names[i] = DemoData.ROOMS.get(i).name;
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRoom.setAdapter(roomAdapter);
        spRoom.setSelection(DemoData.ROOMS.indexOf(room));
        spRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                room = DemoData.ROOMS.get(position);
                updateStatus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

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

        // Duration: same choices as the employee screen
        rgDuration.check(R.id.rb_book_60);
        rgDuration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_book_30) durationMinutes = 30;
                else if (checkedId == R.id.rb_book_60) durationMinutes = 60;
                else if (checkedId == R.id.rb_book_120) durationMinutes = 120;
                else if (checkedId == R.id.rb_book_180) durationMinutes = 180;
                updateStatus();
            }
        });

        // Employee search: suggests names as the manager types
        actvEmployee.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                DemoData.EMPLOYEE_NAMES));
        actvEmployee.setThreshold(0);      // suggest from the first moment, even with nothing typed

        // Book for: Me or Another user
        rgWho.check(R.id.rb_me);
        rgWho.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_other) {
                    showEmployeeSearch();
                } else {
                    llEmployee.setVisibility(View.GONE);
                    actvEmployee.setError(null);
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBooking();
            }
        });

        updateStatus();
    }

    /** Opens and highlights the employee search as soon as Another user is chosen. */
    private void showEmployeeSearch() {
        llEmployee.setVisibility(View.VISIBLE);
        llEmployee.setBackgroundResource(R.drawable.bg_highlight);     // highlighted background and outline
        actvEmployee.requestFocus();                                   // the cursor is already in the box
        actvEmployee.showDropDown();                                   // and the list of names is already open
    }

    private boolean isWeekend(LocalDate d) {
        return d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /** Date picker limited to today and the next 29 days, Monday to Friday. */
    private void pickDate() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker picker, int year, int month, int dayOfMonth) {
                        LocalDate chosen = LocalDate.of(year, month + 1, dayOfMonth);
                        if (isWeekend(chosen)) {
                            tvClash.setText(R.string.error_weekend);
                            return;
                        }
                        date = chosen;
                        updateStatus();
                    }
                }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();
        max.add(Calendar.DAY_OF_YEAR, DemoData.MAX_DAYS_AHEAD);
        dialog.getDatePicker().setMinDate(min.getTimeInMillis() - 1000);
        dialog.getDatePicker().setMaxDate(max.getTimeInMillis());
        dialog.show();
    }

    /** Time picker. Only 30-minute steps inside opening hours are accepted. */
    private void pickTime() {
        TimePickerDialog dialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker picker, int hour, int minute) {
                        if (minute != 0 && minute != 30) {
                            tvClash.setText(R.string.error_half_hour);
                            return;
                        }
                        LocalTime chosen = LocalTime.of(hour, minute);
                        if (chosen.isBefore(DemoData.OPEN) || chosen.isAfter(DemoData.LAST_START)) {
                            tvClash.setText(R.string.error_opening_hours);
                            return;
                        }
                        start = chosen;
                        updateStatus();
                    }
                }, start.getHour(), start.getMinute(), true);
        dialog.show();
    }

    /** Updates the buttons and says if the room is free. Confirm is switched off if it clashes. */
    private void updateStatus() {
        LocalTime end = start.plusMinutes(durationMinutes);
        btnDate.setText(date.format(DAY));
        btnStart.setText(start.toString());

        if (end.isAfter(DemoData.CLOSE)) {
            tvClash.setText(R.string.error_too_late);
            btnConfirm.setEnabled(false);
            return;
        }
        Booking clash = DemoData.holder(room, date, start, end);
        if (clash == null) {
            tvClash.setText(getString(R.string.book_free, room.name, date.format(SHORT_DAY),
                    start.toString(), end.toString()));
            btnConfirm.setEnabled(true);
        } else {
            tvClash.setText(getString(R.string.book_clash, room.name, clash.booker,
                    clash.start.toString(), clash.end.toString()));
            btnConfirm.setEnabled(false);
        }
    }

    // Runs when Confirm booking is tapped. Checks who it is for, then books and closes.
    private void confirmBooking() {
        String who = DemoData.MANAGER_NAME;
        if (rgWho.getCheckedRadioButtonId() == R.id.rb_other) {
            who = matchEmployee(actvEmployee.getText().toString().trim());
            if (who == null) {
                actvEmployee.setError(getString(R.string.error_employee_unknown));    // say what is wrong
                return;
            }
        }
        LocalTime end = start.plusMinutes(durationMinutes);
        Booking booked = DemoData.managerBook(room, date, start, end, who);
        if (booked == null) {          // someone else took it a moment ago
            updateStatus();
            return;
        }
        Toast.makeText(this, R.string.message_booking_confirmed, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);          // tells Room detail (if it opened this screen) that a booking was made
        finish();
    }

    // Returns the listed employee name that matches what was typed (ignoring capitals), or null if there is none.
    private String matchEmployee(String typed) {
        for (String name : DemoData.EMPLOYEE_NAMES) {
            if (name.equalsIgnoreCase(typed)) return name;
        }
        return null;
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
