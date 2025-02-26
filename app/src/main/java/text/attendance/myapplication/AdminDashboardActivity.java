package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout home, ad, logout;

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

            // Find the TextView and set the click listener
            LinearLayout projects = findViewById(R.id.projects);
            projects.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminDashboardActivity.this, EngineerUploadActivity.class);
                    startActivity(intent);
                }
            });
        }


        // Initialize LinearLayouts
        home = findViewById(R.id.home2);
        ad = findViewById(R.id.ad2);
        logout = findViewById(R.id.logout2);

        // Home Click -> Go to AdminDashboardActivity
        home.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });

        // Ad Click -> Go to AdminAdActivity
        ad.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAdActivity.class);
            startActivity(intent);
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

}

