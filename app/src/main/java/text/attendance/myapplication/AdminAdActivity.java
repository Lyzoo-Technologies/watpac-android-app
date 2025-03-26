package text.attendance.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AdminAdActivity extends AppCompatActivity {

    private LinearLayout home, ad, logout;
    private ImageView homeIcon, adIcon, uploadImage1;
    private EditText companyName, phoneNumber;
    private Button submitButton,manage_button;
    private Uri selectedImageUri = null;
    private Bitmap selectedBitmap = null;
    private TextView selectedFileName;


    private static final String UPLOAD_URL = ApiConstants.BASE_URL + "/company-ads"; // Replace with your API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ad);

        // Initialize views
        uploadImage1 = findViewById(R.id.upload_image1);
        home = findViewById(R.id.home);
        ad = findViewById(R.id.ad);
        logout = findViewById(R.id.logout);
        homeIcon = findViewById(R.id.homeIcon);
        adIcon = findViewById(R.id.adIcon);
        companyName = findViewById(R.id.company_name);
        phoneNumber = findViewById(R.id.phone_number);
        submitButton = findViewById(R.id.submit_button);
        manage_button = findViewById(R.id.manage_button);

        // Set initial active state
        setActiveIcon();

        selectedFileName = findViewById(R.id.selected_file_name);

        // Image selection
        uploadImage1.setOnClickListener(v -> openFileChooser());

        // Submit button
        submitButton.setOnClickListener(v -> {
            if (validateFields()) {
                uploadAd();
            }
        });

        manage_button.setOnClickListener(v ->{
            Intent intent = new Intent(AdminAdActivity.this, AdminAdManageActivity.class);
            startActivity(intent);
        });

        // Navigation
        home.setOnClickListener(v -> {
            saveActiveScreen("AdminDashboardActivity");
            navigateTo(AdminDashboardActivity.class);
        });

        ad.setOnClickListener(v -> {
            saveActiveScreen("AdminAdActivity");
            navigateTo(AdminAdActivity.class);
        });

        logout.setOnClickListener(v -> {
            saveActiveScreen("AdminProfileActivity");
            navigateTo(AdminProfileActivity.class);
        });

        disableSSLCertificateValidation();

    }


    private void openFileChooser() {
        pickImageLauncher.launch("image/*");
    }

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    uploadImage1.setImageURI(uri);

                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        String fileName = getFileNameFromUri(uri);
                        if (selectedFileName != null) { // Ensure TextView is initialized
                            selectedFileName.setText(fileName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private String getFileNameFromUri(Uri uri) {
        String result = "Selected File";
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }



    private boolean validateFields() {
        if (companyName.getText().toString().trim().isEmpty()) {
            companyName.setError("Company name is required");
            return false;
        }
        if (phoneNumber.getText().toString().trim().isEmpty()) {
            phoneNumber.setError("Phone number is required");
            return false;
        }
        if (selectedBitmap == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void uploadAd() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        // Convert image to byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        final byte[] imageBytes = byteArrayOutputStream.toByteArray();

        new Thread(() -> {
            try {
                URL url = new URL(UPLOAD_URL);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary");

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // Add form fields
                writeFormField(outputStream, "company_name", companyName.getText().toString().trim());
                writeFormField(outputStream, "phone_number", phoneNumber.getText().toString().trim());

                // Attach image
                attachFileToRequest(outputStream, "image", imageBytes);

                outputStream.writeBytes("\r\n------WebKitFormBoundary--\r\n");
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_CREATED) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Company details uploaded successfully!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Upload failed. Server error!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    private void writeFormField(DataOutputStream outputStream, String fieldName, String fieldValue) throws IOException {
        outputStream.writeBytes("------WebKitFormBoundary\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
        outputStream.writeBytes(fieldValue + "\r\n");
    }
    private void attachFileToRequest(DataOutputStream outputStream, String fieldName, byte[] imageBytes) throws IOException {
        outputStream.writeBytes("------WebKitFormBoundary\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"image.jpg\"\r\n");
        outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n");
        outputStream.write(imageBytes);
        outputStream.writeBytes("\r\n");
    }


    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(AdminAdActivity.this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

        // Reset all icons first
        homeIcon.setImageResource(R.drawable.home);
        adIcon.setImageResource(R.drawable.radio);

        // Set active icon based on current screen
        if ("AdminDashboardActivity".equals(activeScreen)) {
            homeIcon.setImageResource(R.drawable.active_home);
        } else if ("AdminAdActivity".equals(activeScreen)) {
            adIcon.setImageResource(R.drawable.active_radio);
        }
    }

    private void disableSSLCertificateValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            Log.e("SSL_ERROR", "Failed to disable SSL validation", e);
        }
    }
}
