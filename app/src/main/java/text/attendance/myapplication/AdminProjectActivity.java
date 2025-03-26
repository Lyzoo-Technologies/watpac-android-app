package text.attendance.myapplication;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;

public class AdminProjectActivity extends AppCompatActivity {
    private LinearLayout home, ad, logout;
    private ImageView homeIcon, adIcon;
    private SharedPreferences preferences;
    private long downloadID;
    private TextView downloadStatusText;
    private ImageView downloadIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_projects);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userEmail = preferences.getString("userEmail", null);


        // Retrieve company details from Intent
        String companyName = getIntent().getStringExtra("company_name");
        String mobileNumber = getIntent().getStringExtra("mobile_number");
        String address = getIntent().getStringExtra("address");
        String email = getIntent().getStringExtra("email");
        String website = getIntent().getStringExtra("website");
        String gstNumber = getIntent().getStringExtra("gst_number");

        // Find UI components
        TextView companyNameTextView = findViewById(R.id.companyNameDisplay);
        TextView mobileNumberTextView = findViewById(R.id.contactText);
        TextView addressTextView = findViewById(R.id.addressText);
        TextView emailTextView = findViewById(R.id.emailText);
        TextView websiteTextView = findViewById(R.id.websiteText);
        TextView gstNumberTextView = findViewById(R.id.gstText);

        // Set text in UI
        if (companyName != null) companyNameTextView.setText(companyName);
        if (mobileNumber != null) mobileNumberTextView.setText("Mobile: " + mobileNumber);
        if (address != null) addressTextView.setText("Address: " + address);
        if (email != null) emailTextView.setText("Email: " + email);
        if (website != null) websiteTextView.setText("Website: " + website);
        if (gstNumber != null) gstNumberTextView.setText("GST: " + gstNumber);


        // Initialize the layout inside onCreate
        LinearLayout firstLayout = findViewById(R.id.firstLayout);

        firstLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminProjectActivity.this, AdminProjectManageActivity.class);
                startActivity(intent);
            }
        });

        Button deleteButton = findViewById(R.id.delete_button_right);
        String emails = getIntent().getStringExtra("email");

        deleteButton.setOnClickListener(v -> {
            if (emails != null && !emails.isEmpty()) {
                deleteCompanyDetails(emails);
            } else {
                Toast.makeText(this, "Email not found!", Toast.LENGTH_SHORT).show();
            }
        });


        // Initialize LinearLayouts
        home = findViewById(R.id.home4);
        ad = findViewById(R.id.ad4);
        logout = findViewById(R.id.logout4);

        homeIcon = findViewById(R.id.homeIcon);
        adIcon = findViewById(R.id.adIcon);

        // Restore last active screen from SharedPreferences
        setActiveIcon();

        home.setOnClickListener(v -> {
            saveActiveScreen("AdminDashboardActivity");
            Intent intent = new Intent(AdminProjectActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        ad.setOnClickListener(v -> {
            saveActiveScreen("AdminAdActivity");
            Intent intent = new Intent(AdminProjectActivity.this, AdminAdActivity.class);
            startActivity(intent);
            finish();
        });

        // Logout Click -> Logout and Go to SignInActivity
        logout.setOnClickListener(v -> {
            saveActiveScreen("AdminProfileActivity");
            Intent intent = new Intent(AdminProjectActivity.this, AdminProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Download Soil Test PDF
        CardView soilPdfDownload = findViewById(R.id.testpdf);
        soilPdfDownload.setOnClickListener(v -> showDownloadDialog(companyName));

    }

    private void deleteCompanyDetails(String email) {
        String url = ApiConstants.BASE_URL + "/deleteCompany?email=" + Uri.encode(email);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    Toast.makeText(this, "Company deleted successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after deletion
                },
                error -> {
                    Log.e("DELETE_ERROR", "Error deleting company: " + error.toString());
                    Toast.makeText(this, "Failed to delete company!", Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = SSLHelper.getSecureRequestQueue(this);
        queue.add(request);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // Only needed for Android 9 and below
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void showDownloadDialog(String companyName) {
        runOnUiThread(() -> {
            Dialog dialog = new Dialog(AdminProjectActivity.this);
            dialog.setContentView(R.layout.dialog_soil_pdf);
            dialog.setCancelable(true);

            Button downloadButton = dialog.findViewById(R.id.download_pdf_btn);
            downloadButton.setOnClickListener(v -> {
                if (companyName != null) {
                    fetchCompanyPdfs(companyName);
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Company name not found!", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();
        });
    }

    private void fetchCompanyPdfs(String companyName) {
        try {
            String encodedCompanyName = URLEncoder.encode(companyName, "UTF-8");
            String url = ApiConstants.BASE_URL + "/getCompanyPdfs?company_name=" + companyName;
            Log.d("PDF_FETCH", "Fetching PDFs from: " + url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            Log.d("PDF_FETCH", "Response: " + response.toString());

                            if (response.has("soil_test_pdf") && response.has("concrete_test_pdf") &&
                                    response.has("building_draft_pdf") && response.has("govt_rules_pdf")) {

                                String[] pdfUrls = {
                                        response.getString("soil_test_pdf"),
                                        response.getString("concrete_test_pdf"),
                                        response.getString("building_draft_pdf"),
                                        response.getString("govt_rules_pdf")
                                };

                                for (int i = 0; i < pdfUrls.length; i++) {
                                    //String pdfUrl = pdfUrls[i];
                                    String relativePath = pdfUrls[i];
                                    String pdfUrl = ApiConstants.BASE_URL + "/" + relativePath.replace("\\", "/"); // Fix backslashes

                                    // Check if URL is valid
                                    if (pdfUrl == null || pdfUrl.isEmpty() || !pdfUrl.startsWith("http")) {
                                        Log.e("PDF_FETCH", "Invalid URL: " + pdfUrl);
                                        continue;
                                    }

                                    // Download PDFs
                                    downloadFileWithHttpURLConnection(pdfUrl, "Company_Document_" + (i + 1) + ".pdf");
                                }

                            } else {
                                Toast.makeText(this, "PDF URLs not found!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("PDF_FETCH", "JSON Parsing error: " + e.getMessage());
                        }
                    },
                    error -> {
                        Log.e("PDF_FETCH", "Error fetching PDFs: " + error.toString());
                        Toast.makeText(this, "Failed to fetch PDF URLs!", Toast.LENGTH_SHORT).show();
                    }
            );

            RequestQueue queue = SSLHelper.getSecureRequestQueue(this);
            queue.add(request);
        } catch (Exception e) {
            Log.e("PDF_FETCH", "Error encoding URL: " + e.getMessage());
        }
    }

    private void downloadFileWithHttpURLConnection(String fileURL, String fileName) {
        new Thread(() -> {
            try {
                Log.d("DOWNLOAD_DEBUG", "Thread started...");

                URL url = new URL(fileURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("DOWNLOAD_ERROR", "Server returned HTTP " + connection.getResponseCode());
                    return;
                }

                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                Log.d("DOWNLOAD", "Download completed: " + file.getAbsolutePath());

                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Download Complete!", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                Log.e("DOWNLOAD_ERROR", "Error: " + e.getMessage());
            }
        }).start();  // Start the thread
    }

    private void downloadPDF(String pdfUrl, String fileName) {
        if (pdfUrl == null || pdfUrl.isEmpty() || !pdfUrl.startsWith("http")) {
            Log.e("DOWNLOAD_ERROR", "Invalid PDF URL: " + pdfUrl);
            Toast.makeText(this, "Invalid PDF URL!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DOWNLOAD", "Downloading: " + pdfUrl);

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
            request.setTitle(fileName);
            request.setDescription("Downloading...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            } else {
                request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName);
            }

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadID = downloadManager.enqueue(request);
                Log.d("DOWNLOAD", "Download started with ID: " + downloadID);
                Toast.makeText(this, "Downloading: " + fileName, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("DOWNLOAD_ERROR", "Download Manager is null");
                Toast.makeText(this, "Download Manager not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DOWNLOAD_ERROR", "Error downloading PDF: " + e.getMessage());
            Toast.makeText(this, "Error downloading PDF", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update UI
    private void updateDownloadUI(String status, int iconRes) {
        if (downloadStatusText != null) {
            downloadStatusText.setText(status);
        }
        if (downloadIcon != null) {
            downloadIcon.setImageResource(iconRes);
        }
    }

    // Register a BroadcastReceiver to track download status
    private void registerDownloadReceiver() {
        BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadID) {
                    updateDownloadUI("Downloaded", R.drawable.ic_downloaded);
                    Toast.makeText(context, "Download Completed!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerDownloadReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted! Try downloading again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied! Cannot download files.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void saveActiveScreen(String screenName) {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("activeScreen", screenName);
        editor.apply();
    }

    // Function to update the active icon
    private void setActiveIcon() {
        String currentActivity = getClass().getSimpleName(); // Get the current activity name

        if (currentActivity.equals("AdminDashboardActivity")) {
            homeIcon.setImageResource(R.drawable.active_home);
            adIcon.setImageResource(R.drawable.radio);
        } else if (currentActivity.equals("AdminAdActivity")) {
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.active_radio);
        } else {
            // Default state
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.radio);
        }
    }

}
