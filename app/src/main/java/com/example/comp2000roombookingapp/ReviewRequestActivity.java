package com.example.comp2000roombookingapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Manager review screen. Shows the request and any clash, with Approve and Reject (optional reason). */
public class ReviewRequestActivity extends AppCompatActivity {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);

    private Booking request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_request);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find the booking from the id that was sent. If it no longer exists, there is nothing to review.
        request = DemoData.findBooking(getIntent().getLongExtra(Extras.BOOKING_ID, -1));
        if (request == null) {
            finish();
            return;
        }

        boolean cancellation = request.status == Booking.Status.CANCELLATION_REQUESTED;
        ((TextView) findViewById(R.id.tv_rv_room)).setText(request.room.name);
        ((TextView) findViewById(R.id.tv_rv_when)).setText(getString(R.string.review_when,
                request.date.format(DAY), request.start.toString(), request.end.toString()));
        ((TextView) findViewById(R.id.tv_rv_by)).setText(getString(R.string.requested_by, request.booker));
        TextView tvType = (TextView) findViewById(R.id.tv_rv_type);
        tvType.setText(cancellation ? R.string.request_type_cancellation : R.string.request_type_booking);
        tvType.setBackgroundResource(cancellation ? R.drawable.bg_chip_cancellation : R.drawable.bg_chip_pending);

        // The clash check: the system checks and tells the manager in words
        TextView tvClash = (TextView) findViewById(R.id.tv_rv_clash);
        if (cancellation) {
            tvClash.setText(R.string.clash_cancellation_note);
        } else {
            Booking clash = DemoData.clashFor(request);
            if (clash == null) {
                tvClash.setText(R.string.clash_none);
            } else {
                tvClash.setText(getString(R.string.clash_found, clash.booker,
                        clash.start.toString(), clash.end.toString()));
            }
        }

        findViewById(R.id.btn_approve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // approve() checks for a clash again, in case something changed while this screen was open.
                if (DemoData.approve(request)) {
                    Toast.makeText(ReviewRequestActivity.this, R.string.message_approved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_clash,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btn_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoData.reject(request);
                Toast.makeText(ReviewRequestActivity.this, R.string.message_rejected, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
