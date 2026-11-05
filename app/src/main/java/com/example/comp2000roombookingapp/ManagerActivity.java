package com.example.comp2000roombookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/** Manager home. Same idea as MainActivity: two buttons switch between two Fragments. */
public class ManagerActivity extends AppCompatActivity {

    private static final int TAB_ROOMS = 0;
    private static final int TAB_REQUESTS = 1;

    private Button btnRooms;
    private Button btnRequests;
    private int currentTab = TAB_ROOMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);       // same layout as the employee home

        // The ids still say "find" and "bookings" because the layout is shared; here they are the manager's two buttons.
        btnRooms = findViewById(R.id.btn_nav_find);
        btnRequests = findViewById(R.id.btn_nav_bookings);
        btnRooms.setText(R.string.mgr_nav_rooms);
        updateRequestCount();

        btnRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTab != TAB_ROOMS) showTab(TAB_ROOMS);
            }
        });
        btnRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTab != TAB_REQUESTS) showTab(TAB_REQUESTS);
            }
        });

        if (savedInstanceState == null) {
            showTab(TAB_ROOMS);
        } else {
            // After a rotation Android rebuilds the Fragment by itself; only work out which one is showing.
            Fragment showing = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            currentTab = (showing instanceof RequestsFragment) ? TAB_REQUESTS : TAB_ROOMS;
            highlight(currentTab);
        }
    }

    // Runs every time this screen comes back into view, e.g. after a decision on another
    // screen, so the counter in the Requests button is always correct.
    @Override
    protected void onResume() {
        super.onResume();
        updateRequestCount();
    }

    private void showTab(int tab) {
        currentTab = tab;
        highlight(tab);
        Fragment fragment = (tab == TAB_REQUESTS) ? new RequestsFragment() : new AllRoomsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void highlight(int tab) {
        btnRooms.setSelected(tab == TAB_ROOMS);
        btnRequests.setSelected(tab == TAB_REQUESTS);
        setTitle(tab == TAB_REQUESTS ? R.string.mgr_title_requests : R.string.mgr_title_rooms);
    }

    /** Puts the number of waiting requests in the second button, e.g. Requests (2). */
    public void updateRequestCount() {
        btnRequests.setText(getString(R.string.mgr_nav_requests, DemoData.requestCount()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);      // the same Log out menu as the employee screens
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
