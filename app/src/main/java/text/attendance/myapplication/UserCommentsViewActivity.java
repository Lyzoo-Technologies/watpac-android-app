package text.attendance.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserCommentsViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_comments_view);

        ImageView comment = findViewById(R.id.usercomment);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserCommentsViewActivity.this, AdminCommentActivity.class);
                startActivity(intent);
            }
        });

        TextView comment1 = findViewById(R.id.usercomment1);
        comment1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserCommentsViewActivity.this, AdminCommentActivity.class);
                startActivity(intent);
            }
        });

        // Initialize LinearLayouts
        LinearLayout userhome = findViewById(R.id.userhome1);
        LinearLayout userlogout = findViewById(R.id.userlogout1);

        // Debugging: Check if they are found
        if (userhome == null || userlogout == null) {
            Log.e("EngineerBasementActivity", "One or more LinearLayouts are null! Check XML IDs.");
            return; // Stop execution if views are null
        }

        // Set OnClickListeners
        userhome.setOnClickListener(v -> {
            Intent intent = new Intent(UserCommentsViewActivity.this, UserCommentActivity.class);
            startActivity(intent);
        });

        userlogout.setOnClickListener(v -> {
            // Perform logout (optional: clear session data)
            Intent intent = new Intent(UserCommentsViewActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
            startActivity(intent);
            finish(); // Close the current activity
        });
    }
}
