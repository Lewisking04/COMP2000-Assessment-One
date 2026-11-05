package com.example.roombooking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adapter for the My bookings list. Each row shows the status and a button that depends on it. */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    public interface OnActionListener {
        void onAction(Booking booking);
    }

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    private final List<Booking> items = new ArrayList<>();
    private final OnActionListener listener;

    public BookingAdapter(OnActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<Booking> bookings) {
        items.clear();
        items.addAll(bookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Booking b = items.get(position);
        Context c = h.itemView.getContext();
        h.name.setText(b.room.name);
        h.when.setText(c.getString(R.string.booking_when, b.date.format(DAY), b.start.toString(), b.end.toString()));

        int label;
        int background;
        switch (b.status) {
            case PENDING:
                label = R.string.status_pending;
                background = R.drawable.bg_chip_pending;
                break;
            case CONFIRMED:
                label = R.string.status_confirmed;
                background = R.drawable.bg_chip_confirmed;
                break;
            default:
                label = R.string.status_cancellation_requested;
                background = R.drawable.bg_chip_cancellation;
                break;
        }
        h.status.setText(label);
        h.status.setBackgroundResource(background);

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
            h.action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAction(b);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView when;
        final TextView status;
        final Button action;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_booking_room);
            when = itemView.findViewById(R.id.tv_booking_when);
            status = itemView.findViewById(R.id.tv_status);
            action = itemView.findViewById(R.id.btn_action);
        }
    }
}
