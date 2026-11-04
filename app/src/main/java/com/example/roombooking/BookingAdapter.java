package com.example.roombooking;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adapter for the My bookings list. Each row shows the status and a button that depends on it. */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.VH> {

    public interface OnAction {
        void onAction(Booking booking);
    }

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    private final List<Booking> items = new ArrayList<>();
    private final OnAction listener;

    public BookingAdapter(OnAction listener) {
        this.listener = listener;
    }

    public void submit(List<Booking> bookings) {
        items.clear();
        items.addAll(bookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Booking b = items.get(position);
        Context c = h.itemView.getContext();
        h.name.setText(b.room.name);
        h.when.setText(c.getString(R.string.booking_when, b.date.format(DAY), b.start.toString(), b.end.toString()));

        int label;
        int bg;
        switch (b.status) {
            case PENDING:
                label = R.string.status_pending;
                bg = R.color.status_pending;
                break;
            case CONFIRMED:
                label = R.string.status_confirmed;
                bg = R.color.status_confirmed;
                break;
            default:
                label = R.string.status_cancellation_requested;
                bg = R.color.status_cancellation;
                break;
        }
        h.status.setText(label);
        h.status.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(c, bg)));

        int action = 0;
        if (!b.isPast()) {
            if (b.status == Booking.Status.PENDING) action = R.string.withdraw;
            else if (b.status == Booking.Status.CONFIRMED) action = R.string.ask_to_cancel;
        }
        if (action == 0) {
            h.action.setVisibility(View.GONE);
        } else {
            h.action.setVisibility(View.VISIBLE);
            h.action.setText(action);
            h.action.setContentDescription(c.getString(action) + " " + b.room.name);
            h.action.setOnClickListener(v -> listener.onAction(b));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView when;
        final Chip status;
        final MaterialButton action;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_booking_room);
            when = itemView.findViewById(R.id.tv_booking_when);
            status = itemView.findViewById(R.id.chip_status);
            action = itemView.findViewById(R.id.btn_action);
        }
    }
}
