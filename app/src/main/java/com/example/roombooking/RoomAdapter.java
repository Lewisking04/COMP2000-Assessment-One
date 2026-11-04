package com.example.roombooking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** Adapter for the room cards. Used on a few different screens. */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.VH> {

    public interface OnRoomClick {
        void onRoom(Room room);
    }

    private final List<Room> rooms = new ArrayList<>();
    private final OnRoomClick listener;
    private final String actionLabel;

    public RoomAdapter(OnRoomClick listener, String actionLabel) {
        this.listener = listener;
        this.actionLabel = actionLabel;
    }

    public void submit(List<Room> newRooms) {
        rooms.clear();
        rooms.addAll(newRooms);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Room room = rooms.get(position);
        h.name.setText(room.name);
        h.details.setText(room.details());
        h.action.setText(actionLabel);
        h.action.setContentDescription(actionLabel + " " + room.name);   // screen readers say which room
        h.action.setOnClickListener(v -> listener.onRoom(room));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView details;
        final MaterialButton action;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_room_name);
            details = itemView.findViewById(R.id.tv_room_details);
            action = itemView.findViewById(R.id.btn_request);
        }
    }
}
