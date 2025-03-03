package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AdminAdActivity extends AppCompatActivity {

    private LinearLayout home, ad, logout;
    private ImageView homeIcon, adIcon;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView uploadImage1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ad);

        uploadImage1 = findViewById(R.id.upload_image1);
        uploadImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        home = findViewById(R.id.home);
        ad = findViewById(R.id.ad);
        logout = findViewById(R.id.logout);

        homeIcon = findViewById(R.id.homeIcon);
        adIcon = findViewById(R.id.adIcon);

        // Restore last active screen from SharedPreferences
        setActiveIcon();

        home.setOnClickListener(v -> {
            saveActiveScreen("AdminDashboardActivity");
            Intent intent = new Intent(AdminAdActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        ad.setOnClickListener(v -> {
            saveActiveScreen("AdminAdActivity");
            Intent intent = new Intent(AdminAdActivity.this, AdminAdActivity.class);
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
        Intent intent = new Intent(AdminAdActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close current activity
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImage1.setImageURI(imageUri);
        }
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
