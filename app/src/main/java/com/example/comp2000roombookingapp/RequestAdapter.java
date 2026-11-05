package com.example.comp2000roombookingapp;

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

/** Adapter for the manager's Requests list. */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    // The fragment passes in what happens when Review is tapped, so this class only builds rows.
    public interface OnReviewListener {
        void onReview(Booking booking);
    }

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK);

    private final List<Booking> items = new ArrayList<>();
    private final OnReviewListener listener;

    public RequestAdapter(OnReviewListener listener) {
        this.listener = listener;
    }

    public void submit(List<Booking> requests) {
        items.clear();
        items.addAll(requests);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Booking b = items.get(position);
        Context c = h.itemView.getContext();
        h.name.setText(b.room.name);
        h.when.setText(c.getString(R.string.booking_when, b.date.format(DAY), b.start.toString(), b.end.toString()));
        h.by.setText(c.getString(R.string.requested_by, b.booker));

        // Say what kind of request it is, in words and with a colour.
        if (b.status == Booking.Status.CANCELLATION_REQUESTED) {
            h.type.setText(R.string.request_type_cancellation);
            h.type.setBackgroundResource(R.drawable.bg_chip_cancellation);
        } else {
            h.type.setText(R.string.request_type_booking);
            h.type.setBackgroundResource(R.drawable.bg_chip_pending);
        }

        // The room name is added so a screen reader says "Review Room 3", not just "Review".
        h.review.setContentDescription(c.getString(R.string.review) + " " + b.room.name);
        h.review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReview(b);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView when;
        final TextView by;
        final TextView type;
        final Button review;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_request_room);
            when = itemView.findViewById(R.id.tv_request_when);
            by = itemView.findViewById(R.id.tv_request_by);
            type = itemView.findViewById(R.id.tv_request_type);
            review = itemView.findViewById(R.id.btn_review);
        }
    }
}
