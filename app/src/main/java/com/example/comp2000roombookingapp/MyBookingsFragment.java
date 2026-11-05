package com.example.comp2000roombookingapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/** My bookings screen with the cancel dialog. The Upcoming and Past buttons change the list. */
public class MyBookingsFragment extends Fragment {

    private BookingAdapter adapter;
    private TextView tvEmpty;
    private Button btnUpcoming;
    private Button btnPast;
    private boolean showUpcoming = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnUpcoming = view.findViewById(R.id.btn_upcoming);
        btnPast = view.findViewById(R.id.btn_past);
        tvEmpty = view.findViewById(R.id.tv_empty);
        RecyclerView list = view.findViewById(R.id.bookings_list);

        // When a booking's button is tapped, ask "are you sure?" first (confirmAction below).
        adapter = new BookingAdapter(new BookingAdapter.OnActionListener() {
            @Override
            public void onAction(Booking booking) {
                confirmAction(booking);
            }
        });
        // A LinearLayoutManager shows the rows in one vertical column.
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        // Tapping Upcoming or Past changes the flag and redraws the list.
        btnUpcoming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpcoming = true;
                refresh();
            }
        });
        btnPast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpcoming = false;
                refresh();
            }
        });
        refresh();

        // Came from Request sent after Cancel request: show that the request was removed
        if (savedInstanceState == null && getArguments() != null
                && getArguments().getBoolean(Extras.SHOW_WITHDRAWN, false)) {
            message(R.string.message_withdrawn);
        }
    }

    // Updates the highlighted button, the count, the list and the empty text.
    private void refresh() {
        btnUpcoming.setSelected(showUpcoming);
        btnUpcoming.setText(getString(R.string.tab_upcoming_count, DemoData.myBookings(true).size()));   // "Upcoming (3)"
        btnPast.setSelected(!showUpcoming);
        List<Booking> data = DemoData.myBookings(showUpcoming);
        adapter.submit(data);
        tvEmpty.setText(showUpcoming ? R.string.empty_upcoming : R.string.empty_past);
        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** The dialog says what will happen. Keep booking is the main button so a slip does not lose the booking. */
    private void confirmAction(final Booking booking) {
        final boolean pending = booking.status == Booking.Status.PENDING;
        new AlertDialog.Builder(requireContext())
                .setTitle(pending ? R.string.dialog_withdraw_title : R.string.dialog_cancel_title)
                .setMessage(getString(pending ? R.string.dialog_withdraw_message : R.string.dialog_cancel_message,
                        booking.room.name))
                .setPositiveButton(R.string.keep_booking, null)
                // Runs only if the user confirms. Pending -> delete the request. Confirmed -> ask the manager to cancel.
                .setNegativeButton(pending ? R.string.withdraw : R.string.ask_to_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (pending) {
                                    DemoData.withdraw(booking);
                                    message(R.string.message_withdrawn);
                                } else {
                                    DemoData.requestCancellation(booking);
                                    message(R.string.message_cancellation_requested);
                                }
                                refresh();
                            }
                        })
                .show();
    }

    // A Snackbar is the short message that slides up from the bottom and fades by itself.
    private void message(int resId) {
        Snackbar.make(requireView(), resId, Snackbar.LENGTH_LONG).show();
    }
}
