package text.attendance.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EngineerCommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_comment);

        ImageView comment = findViewById(R.id.engicomment);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EngineerCommentActivity.this, AdminCommentActivity.class);
                startActivity(intent);
            }
        });

        TextView comment1 = findViewById(R.id.engicomment1);
        comment1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EngineerCommentActivity.this, AdminCommentActivity.class);
                startActivity(intent);
            }
        });

        // Initialize LinearLayouts
        LinearLayout engihome = findViewById(R.id.engihome2);
        LinearLayout engiupload = findViewById(R.id.engiupload2);
        LinearLayout engilogout = findViewById(R.id.engilogout2);

        // Debugging: Check if they are found
        if (engihome == null || engiupload == null || engilogout == null) {
            Log.e("EngineerBasementActivity", "One or more LinearLayouts are null! Check XML IDs.");
            return; // Stop execution if views are null
        }

        // Set OnClickListeners
        engihome.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerCommentActivity.this, EngineerDashboardActivity.class);
            startActivity(intent);
        });

        engiupload.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerCommentActivity.this, EngineerUploadActivity.class);
            startActivity(intent);
        });

        engilogout.setOnClickListener(v -> {
            // Perform logout (optional: clear session data)
            Intent intent = new Intent(EngineerCommentActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
            startActivity(intent);
            finish(); // Close the current activity
        });
    }
}
