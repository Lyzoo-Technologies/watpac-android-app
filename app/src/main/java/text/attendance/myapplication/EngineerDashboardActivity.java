package text.attendance.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class EngineerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_dashboard);

        // Initialize LinearLayouts
        LinearLayout engihome = findViewById(R.id.engihome5);
        LinearLayout engiupload = findViewById(R.id.engiupload5);
        LinearLayout engilogout = findViewById(R.id.engilogout5);

        // Debugging: Check if they are found
        if (engihome == null || engiupload == null || engilogout == null) {
            Log.e("EngineerBasementActivity", "One or more LinearLayouts are null! Check XML IDs.");
            return; // Stop execution if views are null
        }

        // Set OnClickListeners
        engihome.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerDashboardActivity.this, EngineerDashboardActivity.class);
            startActivity(intent);
        });

        engiupload.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerDashboardActivity.this, EngineerUploadActivity.class);
            startActivity(intent);
        });

        engilogout.setOnClickListener(v -> {
            // Perform logout (optional: clear session data)
            Intent intent = new Intent(EngineerDashboardActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
            startActivity(intent);
            finish(); // Close the current activity
        });
    }
}
