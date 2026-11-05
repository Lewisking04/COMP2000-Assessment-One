package com.example.comp2000roombookingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Adapter for the list of free rooms. Used on a few screens. */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    // The screen passes in a listener to decide what a tap does, so one adapter works on three screens.
    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    private final List<Room> rooms = new ArrayList<>();
    private final OnRoomClickListener listener;

    public RoomAdapter(OnRoomClickListener listener) {
        this.listener = listener;
    }

    /** Gives the adapter a new list of rooms and tells the RecyclerView to redraw. */
    public void submit(List<Room> newRooms) {
        rooms.clear();
        rooms.addAll(newRooms);
        notifyDataSetChanged();
    }

    // Step 1: build an empty row from the XML layout.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new ViewHolder(v);
    }

    // Step 2: fill a row with the data for this position.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Room room = rooms.get(position);
        holder.name.setText(room.name);
        holder.details.setText(room.details());
        // The label names the room so a screen reader says "Request Room 3", not just "Request"
        holder.request.setContentDescription(
                holder.itemView.getContext().getString(R.string.request) + " " + room.name);
        holder.request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRoomClick(room);
            }
        });
    }

    // The RecyclerView asks how many rows there are.
    @Override
    public int getItemCount() {
        return rooms.size();
    }

    // The ViewHolder keeps one row's views so they are not looked up again each time.
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView details;
        final Button request;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_room_name);
            details = itemView.findViewById(R.id.tv_room_details);
            request = itemView.findViewById(R.id.btn_request);
        }
    }
}
