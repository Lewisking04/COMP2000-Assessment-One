package com.example.roombooking;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;

/** Employee home. Holds the two main pages and the navigation bar, which is a side rail on wide screens. */
public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private NavigationBarView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                return true;
            }
            return false;
        });

        nav = findViewById(R.id.nav);
        nav.setOnItemSelectedListener(item -> {
            show(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(getIntent().getBooleanExtra(Extras.OPEN_BOOKINGS, false)
                    ? R.id.nav_bookings : R.id.nav_find);
        } else {
            updateTitle(nav.getSelectedItemId());   // the system restores the fragment
        }
    }

    /** Called when View my bookings or Find another room reuses this screen. */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        nav.setSelectedItemId(intent.getBooleanExtra(Extras.OPEN_BOOKINGS, false)
                ? R.id.nav_bookings : R.id.nav_find);
    }

    private void show(int itemId) {
        Fragment fragment = itemId == R.id.nav_bookings ? new MyBookingsFragment() : new FindRoomFragment();
        updateTitle(itemId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void updateTitle(int itemId) {
        toolbar.setTitle(itemId == R.id.nav_bookings ? R.string.nav_my_bookings : R.string.nav_find_room);
    }
}
