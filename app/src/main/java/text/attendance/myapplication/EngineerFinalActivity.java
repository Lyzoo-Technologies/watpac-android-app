package text.attendance.myapplication;  // Update with your package name

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EngineerFinalActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private String selectedFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_final);

        findViewById(R.id.upload_image1).setOnClickListener(v -> openFileChooser("upload_image1"));
        findViewById(R.id.upload_image2).setOnClickListener(v -> openFileChooser("upload_image2"));
        findViewById(R.id.upload_image3).setOnClickListener(v -> openFileChooser("upload_image3"));
        findViewById(R.id.upload_image4).setOnClickListener(v -> openFileChooser("upload_image4"));
        findViewById(R.id.upload_image5).setOnClickListener(v -> openFileChooser("upload_image5"));
        findViewById(R.id.upload_image6).setOnClickListener(v -> openFileChooser("upload_image6"));

        // Initialize LinearLayouts
        LinearLayout engihome = findViewById(R.id.engihome6);
        LinearLayout engiupload = findViewById(R.id.engiupload6);
        LinearLayout engilogout = findViewById(R.id.engilogout6);

        // Debugging: Check if they are found
        if (engihome == null || engiupload == null || engilogout == null) {
            Log.e("EngineerBasementActivity", "One or more LinearLayouts are null! Check XML IDs.");
            return; // Stop execution if views are null
        }

        // Set OnClickListeners
        engihome.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerFinalActivity.this, EngineerDashboardActivity.class);
            startActivity(intent);
        });

        engiupload.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerFinalActivity.this, EngineerUploadActivity.class);
            startActivity(intent);
        });

        engilogout.setOnClickListener(v -> {
            // Perform logout (optional: clear session data)
            Intent intent = new Intent(EngineerFinalActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
            startActivity(intent);
            finish(); // Close the current activity
        });
    }

    private void openFileChooser(String fileType) {
        selectedFileType = fileType;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Allow all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            Toast.makeText(this, selectedFileType + " file selected!", Toast.LENGTH_SHORT).show();
            // You can now upload the file to Firebase Storage or show a preview
        }
    }
}
