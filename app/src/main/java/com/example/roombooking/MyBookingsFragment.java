package com.example.roombooking;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

/** My bookings screen with the cancel dialog. Upcoming and Past tabs. */
public class MyBookingsFragment extends Fragment {

    private BookingAdapter adapter;
    private TextView tvEmpty;
    private TabLayout tabs;

    public MyBookingsFragment() {
        super(R.layout.fragment_my_bookings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabs = view.findViewById(R.id.tabs);
        tvEmpty = view.findViewById(R.id.tv_empty);
        RecyclerView list = view.findViewById(R.id.bookings_list);

        tabs.addTab(tabs.newTab().setText(R.string.tab_upcoming));
        tabs.addTab(tabs.newTab().setText(R.string.tab_past));

        adapter = new BookingAdapter(this::confirmAction);
        list.setLayoutManager(new GridLayoutManager(requireContext(),
                getResources().getInteger(R.integer.booking_columns)));
        list.setAdapter(adapter);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { refresh(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
        refresh();
    }

    private void refresh() {
        boolean upcoming = tabs.getSelectedTabPosition() <= 0;
        java.util.List<Booking> data = DemoData.myBookings(upcoming);
        adapter.submit(data);
        tvEmpty.setText(upcoming ? R.string.empty_upcoming : R.string.empty_past);
        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** The dialog says what will happen. Keep booking is the main button so a slip does not lose the booking. */
    private void confirmAction(Booking booking) {
        boolean pending = booking.status == Booking.Status.PENDING;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(pending ? R.string.dialog_withdraw_title : R.string.dialog_cancel_title)
                .setMessage(getString(pending ? R.string.dialog_withdraw_message : R.string.dialog_cancel_message,
                        booking.room.name))
                .setPositiveButton(R.string.keep_booking, null)
                .setNegativeButton(pending ? R.string.withdraw : R.string.ask_to_cancel, (d, which) -> {
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
