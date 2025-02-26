package text.attendance.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EngineerCompanyDetailsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_FILE_REQUEST = 2; // Added file picker request code

    private Uri imageUri;
    private Uri fileUri; // Added fileUri for non-image files
    private ImageView uploadImage;
    private String fileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_companydetails);

        uploadImage = findViewById(R.id.uploadImage);
        uploadImage.setOnClickListener(v -> openFileChooser("image"));

        findViewById(R.id.upload_soil_test).setOnClickListener(v -> openFileChooser("upload_soil_test"));
        findViewById(R.id.upload_building_draft).setOnClickListener(v -> openFileChooser("upload_building_draft"));
        findViewById(R.id.upload_concrete_test).setOnClickListener(v -> openFileChooser("upload_concrete_test"));
        findViewById(R.id.upload_govt_rules).setOnClickListener(v -> openFileChooser("upload_govt_rules"));
    }

    private void openFileChooser(String type) {
        fileType = type;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        if (type.equals("image")) {
            intent.setType("image/*"); // Image selection
        } else {
            intent.setType("*/*"); // Allow all file types
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        int requestCode = type.equals("image") ? PICK_IMAGE_REQUEST : PICK_FILE_REQUEST;
        startActivityForResult(Intent.createChooser(intent, "Select File"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
                uploadImage.setImageURI(imageUri); // Show selected image
                Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show();
            } else if (requestCode == PICK_FILE_REQUEST) {
                fileUri = data.getData();
                Toast.makeText(this, fileType + " file selected!", Toast.LENGTH_SHORT).show();
                // Here, you can upload the file to Firebase Storage or show a preview
            }
        }
    }
}
