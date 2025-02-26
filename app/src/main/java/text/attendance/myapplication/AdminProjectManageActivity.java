package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AdminProjectManageActivity extends AppCompatActivity {

    private LinearLayout home, ad, logout;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_project_manage);

        // Initialize LinearLayouts
        home = findViewById(R.id.home3);
        ad = findViewById(R.id.ad3);
        logout = findViewById(R.id.logout3);

        // Home Click -> Go to AdminDashboardActivity
        home.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProjectManageActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });

        // Ad Click -> Go to AdminAdActivity
        ad.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProjectManageActivity.this, AdminAdActivity.class);
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
        Intent intent = new Intent(AdminProjectManageActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close current activity
    }

    }
}
