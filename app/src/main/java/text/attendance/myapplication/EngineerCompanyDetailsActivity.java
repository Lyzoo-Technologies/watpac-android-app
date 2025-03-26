package text.attendance.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.net.Uri;
import com.android.volley.Request;



public class EngineerCompanyDetailsActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final String UPLOAD_URL = ApiConstants.BASE_URL + "/upload-company-details"; // Change to your backend URL

    private Uri imageUri, soilTestUri, buildingDraftUri, concreteTestUri, govtRulesUri;
    private String fileType = "";
    private static final String FETCH_USER_DETAILS_URL = ApiConstants.BASE_URL + "/userdetails?email=";
    private EditText companyName, mobileNumber, address, email, website, gstNumber;
    private ImageView uploadImage, uploadSoilTest, uploadBuildingDraft, uploadConcreteTest, uploadGovtRules;
    private Button submitButton;
    private TextView pdfSoilTestName, pdfBuildingDraftName, pdfConcreteTestName, pdfGovtRulesName, imageFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_companydetails);

        // Initialize Views
        imageFileName = findViewById(R.id.image_file_name);
        pdfSoilTestName = findViewById(R.id.pdf_soil_test_name);
        pdfBuildingDraftName = findViewById(R.id.pdf_building_draft_name);
        pdfConcreteTestName = findViewById(R.id.pdf_concrete_test_name);
        pdfGovtRulesName = findViewById(R.id.pdf_govt_rules_name);

        uploadImage = findViewById(R.id.uploadImage);
        uploadSoilTest = findViewById(R.id.upload_soil_test);
        uploadBuildingDraft = findViewById(R.id.upload_building_draft);
        uploadConcreteTest = findViewById(R.id.upload_concrete_test);
        uploadGovtRules = findViewById(R.id.upload_govt_rules);

        companyName = findViewById(R.id.company_name);
        mobileNumber = findViewById(R.id.mobile_number);
        address = findViewById(R.id.address);
        email = findViewById(R.id.email);
        website = findViewById(R.id.website);
        gstNumber = findViewById(R.id.gst_number);

        submitButton = findViewById(R.id.submit_button);

        // File selection handlers
        uploadImage.setOnClickListener(v -> openFileChooser("image"));
        uploadSoilTest.setOnClickListener(v -> openFileChooser("soil_test"));
        uploadBuildingDraft.setOnClickListener(v -> openFileChooser("building_draft"));
        uploadConcreteTest.setOnClickListener(v -> openFileChooser("concrete_test"));
        uploadGovtRules.setOnClickListener(v -> openFileChooser("govt_rules"));

        submitButton = findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String userEmail = sharedPreferences.getString("userEmail", "");

            // Check if email is available
            if (!userEmail.isEmpty()) {
                fetchUserDetails(userEmail);  // Pass the email as an argument
            } else {
                Toast.makeText(EngineerCompanyDetailsActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        });


        disableSSLCertificateValidation(); // Remove in production
    }

    private void fetchUserDetails(String userEmail) {
        String url = ApiConstants.BASE_URL + "/getUseremail?email=" + Uri.encode(userEmail); // Encode email to prevent URL issues
        Log.d("FETCH_DEBUG", "Fetching details from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("FETCH_DEBUG", "Response received: " + response.toString());
                    try {
                        if (response.has("email") && response.has("username") && response.has("phone") && response.has("role")) {
                            String email = response.getString("email");
                            String username = response.getString("username");
                            String phone = response.getString("phone");
                            String role = response.getString("role");

                            // Call the method to upload the company details
                            //uploadCompanyDetails(email, phone, role, username);
                            runOnUiThread(() -> {
                                uploadCompanyDetails(email, phone, role, username);
                            });
                        } else {
                            Log.e("FETCH_ERROR", "Missing keys in response (email, username, phone, role)");
                        }
                    } catch (JSONException e) {
                        Log.e("FETCH_ERROR", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    // No error handling — ignore errors and continue
                    Log.d("FETCH_DEBUG", "Error ignored to prevent form submission blocking.");
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        // Use application context to avoid memory leaks
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }



    private void uploadCompanyDetails(String engiEmail, String engiPhone, String engiRole, String engiUsername) {

        if (companyName.getText().toString().isEmpty() || mobileNumber.getText().toString().isEmpty() ||
                address.getText().toString().isEmpty() || email.getText().toString().isEmpty() || gstNumber.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Log.d("THREAD_DEBUG", "Thread started...");
            try {
                URL url = new URL(UPLOAD_URL);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary");

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                writeFormField(outputStream, "company_name", companyName.getText().toString());
                writeFormField(outputStream, "mobile_number", mobileNumber.getText().toString());
                writeFormField(outputStream, "address", address.getText().toString());
                writeFormField(outputStream, "email", email.getText().toString());
                writeFormField(outputStream, "website", website.getText().toString());
                writeFormField(outputStream, "gst_number", gstNumber.getText().toString());
                writeFormField(outputStream, "engi_email", engiEmail);
                writeFormField(outputStream, "engi_username", engiUsername);
                writeFormField(outputStream, "engi_phone", engiPhone);
                writeFormField(outputStream, "engi_role", engiRole);

                attachFileToRequest(outputStream, "profile_image", imageUri);
                attachFileToRequest(outputStream, "soil_test_pdf", soilTestUri);
                attachFileToRequest(outputStream, "building_draft_pdf", buildingDraftUri);
                attachFileToRequest(outputStream, "concrete_test_pdf", concreteTestUri);
                attachFileToRequest(outputStream, "govt_rules_pdf", govtRulesUri);

                outputStream.writeBytes("\r\n------WebKitFormBoundary--\r\n");
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK  || responseCode == HttpsURLConnection.HTTP_CREATED) {
                    runOnUiThread(() -> Toast.makeText(this, "Company details uploaded successfully!", Toast.LENGTH_SHORT).show());
                    // Redirect to EngineerUploadActivity
                    Intent intent = new Intent(this, EngineerUploadActivity.class);
                    startActivity(intent);

                    // Optional: Finish current activity if you don't want to come back to it
                    finish();

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Upload failed. Server error!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void writeFormField(DataOutputStream outputStream, String fieldName, String value) throws IOException {
        outputStream.writeBytes("------WebKitFormBoundary\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
        outputStream.writeBytes(value + "\r\n");
    }

    private void attachFileToRequest(DataOutputStream outputStream, String fieldName, Uri fileUri) throws IOException {
        if (fileUri == null) return;

        InputStream inputStream = getContentResolver().openInputStream(fileUri);
        if (inputStream == null) {
            Log.e("UPLOAD_ERROR", "Failed to open input stream for file: " + fileUri);
            return;
        }

        String fileName = getFileName(fileUri);
        String mimeType = getMimeType(fileUri);

        outputStream.writeBytes("------WebKitFormBoundary\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        outputStream.writeBytes("Content-Type: " + mimeType + "\r\n\r\n");

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        outputStream.writeBytes("\r\n");
    }

    private String getRealPathFromURI(Uri contentUri) {
        String filePath = null;
        String[] proj = {android.provider.MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(column_index);
            }
            cursor.close();
        }
        return filePath;
    }

    private String getMimeType(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
        }
        return mimeType != null ? mimeType : "application/octet-stream";
    }



    private void openFileChooser(String type) {
        fileType = type;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type.equals("image") ? "image/*" : "application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedFileUri = data.getData();
            String fileName = getFileName(selectedFileUri);

            Log.d("FileSelection", "File Type: " + fileType);
            Log.d("FileSelection", "File URI: " + selectedFileUri);
            Log.d("FileSelection", "File Name: " + fileName);

            if (fileName == null || fileName.isEmpty()) {
                Toast.makeText(this, "Failed to get file name", Toast.LENGTH_SHORT).show();
                return;
            }

            switch (fileType) {
                case "image":
                    imageUri = selectedFileUri;
                    imageFileName.setText(fileName);
                    imageFileName.setVisibility(View.VISIBLE); // Ensure it's visible
                    break;
                case "soil_test":
                    soilTestUri = selectedFileUri;
                    pdfSoilTestName.setText(fileName);
                    pdfSoilTestName.setVisibility(View.VISIBLE);
                    break;
                case "building_draft":
                    buildingDraftUri = selectedFileUri;
                    pdfBuildingDraftName.setText(fileName);
                    pdfBuildingDraftName.setVisibility(View.VISIBLE);
                    break;
                case "concrete_test":
                    concreteTestUri = selectedFileUri;
                    pdfConcreteTestName.setText(fileName);
                    pdfConcreteTestName.setVisibility(View.VISIBLE);
                    break;
                case "govt_rules":
                    govtRulesUri = selectedFileUri;
                    pdfGovtRulesName.setText(fileName);
                    pdfGovtRulesName.setVisibility(View.VISIBLE);
                    break;
                default:
                    Log.e("FileSelection", "Unknown file type: " + fileType);
            }
        }
    }


    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e("FileNameError", "Error retrieving file name: " + e.getMessage());
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


    private void disableSSLCertificateValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            Log.e("SSL_ERROR", "Failed to disable SSL validation", e);
        }
    }

}