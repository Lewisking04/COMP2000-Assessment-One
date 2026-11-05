package com.example.roombooking;

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

    private static final int TAB_FIND = 0;
    private static final int TAB_BOOKINGS = 1;
    private static final String KEY_TAB = "key_tab";

    private Button btnFind;
    private Button btnBookings;
    private int currentTab = TAB_FIND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFind = findViewById(R.id.btn_nav_find);
        btnBookings = findViewById(R.id.btn_nav_bookings);

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTab != TAB_FIND) showTab(TAB_FIND);
            }
        });
        btnBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTab != TAB_BOOKINGS) showTab(TAB_BOOKINGS);
            }
        });

        if (savedInstanceState == null) {
            boolean openBookings = getIntent().getBooleanExtra(Extras.OPEN_BOOKINGS, false);
            showTab(openBookings ? TAB_BOOKINGS : TAB_FIND, getIntent().getBooleanExtra(Extras.SHOW_WITHDRAWN, false));
        } else {
            // After a rotation the system restores the Fragment, so only the highlight and title are redone
            currentTab = savedInstanceState.getInt(KEY_TAB, TAB_FIND);
            highlight(currentTab);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TAB, currentTab);
    }

    /** View my bookings and Find another room come back here and reuse this screen. */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showTab(intent.getBooleanExtra(Extras.OPEN_BOOKINGS, false) ? TAB_BOOKINGS : TAB_FIND,
                intent.getBooleanExtra(Extras.SHOW_WITHDRAWN, false));
    }

    private void showTab(int tab) {
        showTab(tab, false);
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /** The selected button is darker with bold text. */
    private void highlight(int tab) {
        btnFind.setSelected(tab == TAB_FIND);
        btnBookings.setSelected(tab == TAB_BOOKINGS);
        setTitle(tab == TAB_BOOKINGS ? R.string.nav_my_bookings : R.string.nav_find_room);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
