package com.example.comp2000roombookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/** Employee home. Holds the two main pages as Fragments and switches between them. Log out is in the menu. */
public class MainActivity extends AppCompatActivity {

    // Named numbers are easier to read
    private static final int TAB_FIND = 0;
    private static final int TAB_BOOKINGS = 1;

    private Button btnFind;
    private Button btnBookings;
    private int currentTab = TAB_FIND;     // remembers which page is showing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect the two navigation buttons
        btnFind = findViewById(R.id.btn_nav_find);
        btnBookings = findViewById(R.id.btn_nav_bookings);

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTab != TAB_FIND) showTab(TAB_FIND, false);
            }
        });
        btnBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTab != TAB_BOOKINGS) showTab(TAB_BOOKINGS, false);
            }
        });

        // savedInstanceState is null the first time the screen opens; it is filled in after a rotation.
        if (savedInstanceState == null) {
            // The previous screen may ask for My bookings to open first (it sends these as Intent extras).
            boolean openBookings = getIntent().getBooleanExtra(Extras.OPEN_BOOKINGS, false);
            boolean showWithdrawn = getIntent().getBooleanExtra(Extras.SHOW_WITHDRAWN, false);
            showTab(openBookings ? TAB_BOOKINGS : TAB_FIND, showWithdrawn);
        } else {
            // After a rotation Android rebuilds the Fragment by itself, so we only ask which one is showing
            // and redo the button highlight and the title.
            Fragment showing = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            currentTab = (showing instanceof MyBookingsFragment) ? TAB_BOOKINGS : TAB_FIND;
            highlight(currentTab);
        }
    }

    /** showWithdrawn is passed to My bookings. */
    private void showTab(int tab, boolean showWithdrawn) {
        currentTab = tab;
        highlight(tab);
        Fragment fragment;
        if (tab == TAB_BOOKINGS) {
            fragment = new MyBookingsFragment();
            Bundle args = new Bundle();
            args.putBoolean(Extras.SHOW_WITHDRAWN, showWithdrawn);
            fragment.setArguments(args);
        } else {
            fragment = new FindRoomFragment();
        }
        // Replace whatever is in the container with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /** Selected state is a darker button and bold text */
    private void highlight(int tab) {
        btnFind.setSelected(tab == TAB_FIND);
        btnBookings.setSelected(tab == TAB_BOOKINGS);
        setTitle(tab == TAB_BOOKINGS ? R.string.nav_my_bookings : R.string.nav_find_room);
    }

    // Builds the three-dot menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Runs when a menu item is tapped.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Go back to the login screen and close this one
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
