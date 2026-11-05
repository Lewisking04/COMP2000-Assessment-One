package com.example.comp2000roombookingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Adapter for the manager's All rooms list. Shows Vacant, or the time and booker if it is booked. */
public class RoomStatusAdapter extends RecyclerView.Adapter<RoomStatusAdapter.ViewHolder> {

    private final List<Room> rooms = new ArrayList<>();
    private final RoomAdapter.OnRoomClickListener listener;
    // The slot being shown. Each row asks DemoData who (if anyone) holds its room during this slot.
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;

    public RoomStatusAdapter(RoomAdapter.OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Room> newRooms, LocalDate date, LocalTime start, LocalTime end) {
        rooms.clear();
        rooms.addAll(newRooms);
        this.date = date;
        this.start = start;
        this.end = end;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_status, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Room room = rooms.get(position);
        Context c = h.itemView.getContext();
        h.name.setText(room.name);
        h.details.setText(room.details());

        Booking holder = DemoData.holder(room, date, start, end);     // null means the room is vacant
        if (holder == null) {
            h.status.setText(R.string.status_vacant);
            h.status.setBackgroundResource(R.drawable.bg_chip_confirmed);
            h.booker.setVisibility(View.GONE);
        } else {
            h.status.setText(c.getString(R.string.status_booked_until, holder.end.toString()));
            h.status.setBackgroundResource(R.drawable.bg_chip_cancellation);
            h.booker.setText(c.getString(R.string.booked_by, holder.booker));
            h.booker.setVisibility(View.VISIBLE);
        }

        // The whole row can be tapped
        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRoomClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView details;
        final TextView status;
        final TextView booker;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_room_name);
            details = itemView.findViewById(R.id.tv_room_details);
            status = itemView.findViewById(R.id.tv_room_status);
            booker = itemView.findViewById(R.id.tv_room_booker);
        }
    }
}
