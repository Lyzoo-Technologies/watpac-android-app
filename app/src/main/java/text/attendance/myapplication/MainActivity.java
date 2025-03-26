package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Show splash screen for 1.5 seconds before redirecting
        new Handler().postDelayed(this::checkLoginStatus, 1500);
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String userRole = sharedPreferences.getString("userRole", "");

        Intent intent;

        if (isLoggedIn && userRole != null && !userRole.isEmpty()) {
            // ✅ Redirect to appropriate dashboard
            intent = getDashboardIntent(userRole);
        } else {
            // ✅ Redirect to Login screen (Force user to sign in)
            intent = new Intent(MainActivity.this, SignInActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private Intent getDashboardIntent(String role) {
        switch (role) {
            case "Admin":
                return new Intent(this, AdminDashboardActivity.class);
            case "Builder":
                return new Intent(this, EngineerDashboardActivity.class); // FIXED REDIRECTION
            case "Investor":
                return new Intent(this, UserDashboardActivity.class);
            default:
                return new Intent(this, UserDashboardActivity.class);
        }
    }
}
