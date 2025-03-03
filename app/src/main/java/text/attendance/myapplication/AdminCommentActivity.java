package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AdminCommentActivity extends AppCompatActivity {
    private LinearLayout home, ad, logout;
    private ImageView homeIcon, adIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_comment_manage);

        home = findViewById(R.id.home1);
        ad = findViewById(R.id.ad1);
        logout = findViewById(R.id.logout1);

        homeIcon = findViewById(R.id.homeIcon);
        adIcon = findViewById(R.id.adIcon);

        // Restore last active screen from SharedPreferences
        setActiveIcon();

        home.setOnClickListener(v -> {
            saveActiveScreen("AdminDashboardActivity");
            Intent intent = new Intent(AdminCommentActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        ad.setOnClickListener(v -> {
            saveActiveScreen("AdminAdActivity");
            Intent intent = new Intent(AdminCommentActivity.this, AdminAdActivity.class);
            startActivity(intent);
            finish();
        });

        // Logout Click -> Logout and Go to SignInActivity
        logout.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(AdminCommentActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveActiveScreen(String screenName) {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("activeScreen", screenName);
        editor.apply();
    }

    private void setActiveIcon() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String activeScreen = preferences.getString("activeScreen", "");

        if (activeScreen.equals("AdminDashboardActivity")) {
            homeIcon.setImageResource(R.drawable.active_home);
            adIcon.setImageResource(R.drawable.radio);
        } else if (activeScreen.equals("AdminAdActivity")) {
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.active_radio);
        } else {
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.radio);
        }
    }
}
