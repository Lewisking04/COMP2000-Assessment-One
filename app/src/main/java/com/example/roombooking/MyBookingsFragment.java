package com.example.roombooking;

import android.content.res.Configuration;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/** My bookings screen with the cancel dialog. The Upcoming and Past buttons change the list. */
public class MyBookingsFragment extends Fragment {

    private static final String KEY_UPCOMING = "key_upcoming";

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

        if (savedInstanceState != null) showUpcoming = savedInstanceState.getBoolean(KEY_UPCOMING, true);

        btnUpcoming = view.findViewById(R.id.btn_upcoming);
        btnPast = view.findViewById(R.id.btn_past);
        tvEmpty = view.findViewById(R.id.tv_empty);
        RecyclerView list = view.findViewById(R.id.bookings_list);

        adapter = new BookingAdapter(booking -> confirmAction(booking));
        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        list.setLayoutManager(new GridLayoutManager(requireContext(), landscape ? 2 : 1));
        list.setAdapter(adapter);

        btnUpcoming.setOnClickListener(v -> { showUpcoming = true; refresh(); });
        btnPast.setOnClickListener(v -> { showUpcoming = false; refresh(); });
        refresh();

        // Came from Request sent after Cancel request: show that the request was removed
        if (savedInstanceState == null && getArguments() != null
                && getArguments().getBoolean(Extras.SHOW_WITHDRAWN, false)) {
            message(R.string.message_withdrawn);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_UPCOMING, showUpcoming);
    }

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
    private void confirmAction(Booking booking) {
        final boolean pending = booking.status == Booking.Status.PENDING;
        new AlertDialog.Builder(requireContext())
                .setTitle(pending ? R.string.dialog_withdraw_title : R.string.dialog_cancel_title)
                .setMessage(getString(pending ? R.string.dialog_withdraw_message : R.string.dialog_cancel_message,
                        booking.room.name))
                .setPositiveButton(R.string.keep_booking, null)
                .setNegativeButton(pending ? R.string.withdraw : R.string.ask_to_cancel,
                        (dialog, which) -> {
                            if (pending) {
                                DemoData.withdraw(booking);
                                message(R.string.message_withdrawn);
                            } else {
                                DemoData.requestCancellation(booking);
                                message(R.string.message_cancellation_requested);
                            }
                            refresh();
                        })
                .show();
    }

    private void message(int resId) {
        Snackbar.make(requireView(), resId, Snackbar.LENGTH_LONG).show();   // feedback
    }
}
