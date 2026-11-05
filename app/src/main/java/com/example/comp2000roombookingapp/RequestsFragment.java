package com.example.comp2000roombookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Manager requests page. Two tabs, Bookings and Cancellations. Review opens the decision screen. */
public class RequestsFragment extends Fragment {

    private RequestAdapter adapter;
    private TextView tvEmpty;
    private Button btnBookings;
    private Button btnCancellations;
    private boolean showCancellations = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmpty = view.findViewById(R.id.tv_requests_empty);
        btnBookings = view.findViewById(R.id.btn_tab_bookings);
        btnCancellations = view.findViewById(R.id.btn_tab_cancellations);
        RecyclerView list = view.findViewById(R.id.requests_list);

        // Review opens the Review request screen with just the booking id
        adapter = new RequestAdapter(new RequestAdapter.OnReviewListener() {
            @Override
            public void onReview(Booking booking) {
                Intent i = new Intent(requireContext(), ReviewRequestActivity.class);
                i.putExtra(Extras.BOOKING_ID, booking.id);
                startActivity(i);
            }
        });
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        btnBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancellations = false;
                refresh();
            }
        });
        btnCancellations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancellations = true;
                refresh();
            }
        });
        refresh();
    }

    // Runs when this page comes back into view, e.g. after a decision, so the list and counts update.
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) refresh();
    }

    // Updates the tab highlight, the counts, the list, the empty message and the navigation counter.
    private void refresh() {
        btnBookings.setSelected(!showCancellations);
        btnCancellations.setSelected(showCancellations);
        btnBookings.setText(getString(R.string.tab_bookings_count, DemoData.requests(false).size()));
        btnCancellations.setText(getString(R.string.tab_cancellations_count, DemoData.requests(true).size()));
        List<Booking> waiting = DemoData.requests(showCancellations);
        adapter.submit(waiting);
        tvEmpty.setVisibility(waiting.isEmpty() ? View.VISIBLE : View.GONE);
        ((ManagerActivity) requireActivity()).updateRequestCount();
    }
}
