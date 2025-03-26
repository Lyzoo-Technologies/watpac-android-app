package text.attendance.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EngineerBasementActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private String selectedFileType;
    private Map<String, Uri> fileMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_basement);

        findViewById(R.id.upload_image1).setOnClickListener(v -> openFileChooser("basement_stage_video"));
        findViewById(R.id.upload_image2).setOnClickListener(v -> openFileChooser("other_related_video"));
        findViewById(R.id.upload_image3).setOnClickListener(v -> openFileChooser("steel_image"));
        findViewById(R.id.upload_image4).setOnClickListener(v -> openFileChooser("cement_image"));
        findViewById(R.id.upload_image5).setOnClickListener(v -> openFileChooser("aggregate_jelly_image"));

        Button uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(v -> uploadFilesToServer());

        // Initialize LinearLayouts
        LinearLayout engihome = findViewById(R.id.engihome);
        LinearLayout engiupload = findViewById(R.id.engiupload);
        LinearLayout engilogout = findViewById(R.id.engilogout);

        // Debugging: Check if they are found
        if (engihome == null || engiupload == null || engilogout == null) {
            Log.e("EngineerBasementActivity", "One or more LinearLayouts are null! Check XML IDs.");
            return; // Stop execution if views are null
        }

        // Set OnClickListeners
        engihome.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerBasementActivity.this, EngineerDashboardActivity.class);
            startActivity(intent);
        });

        engiupload.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerBasementActivity.this, EngineerUploadActivity.class);
            startActivity(intent);
        });

        engilogout.setOnClickListener(v -> {
            // Perform logout (optional: clear session data)
            Intent intent = new Intent(EngineerBasementActivity.this, SignInActivity.class);
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
            fileMap.put(selectedFileType, fileUri);
            Toast.makeText(this, selectedFileType + " file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFilesToServer() {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (fileUri != null) { // Ensure a file is selected
            File file = new File(FileUtils.getPath(this, fileUri));
            RequestBody fileBody = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file);
            builder.addFormDataPart("file", file.getName(), fileBody);
        } else {
            Toast.makeText(this, "Please select a file before uploading.", Toast.LENGTH_SHORT).show();
            return; // Stop execution if no file is selected
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(ApiConstants.UPLOAD_BASEMENT_DETAILS) // Use correct API URL
                .post(requestBody)
                .build();

        // Execute the request using OkHttpClient (ensure you're using it properly)
    }

}
