package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout home, ad, logout;
    private ImageView homeIcon, adIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Find the LinearLayout and set click listener
        LinearLayout project = findViewById(R.id.project);

        if (project != null) { // Prevent NullPointerException
            project.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminProjectActivity.class);
                    startActivity(intent);
                }
            });

        }

        home = findViewById(R.id.home2);
        ad = findViewById(R.id.ad2);
        logout = findViewById(R.id.logout2);

        homeIcon = findViewById(R.id.homeIcon);
        adIcon = findViewById(R.id.adIcon);

        // Restore last active screen from SharedPreferences
        setActiveIcon();

        home.setOnClickListener(v -> {
            saveActiveScreen("AdminDashboardActivity");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        ad.setOnClickListener(v -> {
            saveActiveScreen("AdminAdActivity");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAdActivity.class);
            startActivity(intent);
            finish();
        });

        // Logout Click -> Logout and Go to SignInActivity
        logout.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void logoutUser() {
        // Clear SharedPreferences (if storing login session)
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // Go to SignInActivity and clear the backstack
        Intent intent = new Intent(AdminDashboardActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close current activity
    }

    private void saveActiveScreen(String screenName) {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("activeScreen", screenName);
        editor.apply();
    }

    // Function to update the active icon
    private void setActiveIcon() {
        String currentActivity = getClass().getSimpleName(); // Get the current activity name

        if (currentActivity.equals("AdminDashboardActivity")) {
            homeIcon.setImageResource(R.drawable.active_home);
            adIcon.setImageResource(R.drawable.radio);
        } else if (currentActivity.equals("AdminAdActivity")) {
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.active_radio);
        } else {
            // Default state
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.radio);
        }
    }
}

