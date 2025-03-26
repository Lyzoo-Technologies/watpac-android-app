package text.attendance.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.anastr.speedviewlib.SpeedView;
import com.razorpay.Checkout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

public class EngineerProjectActivity extends AppCompatActivity {

    private static final String TAG = "UserProjectActivity";
    private static final String RAZORPAY_KEY = "rzp_test_80ImuKJdgbgtsM";
    private static final String SERVER_URL = ApiConstants.BASE_URL + "getUserDetails";
    private static final String PDF_URL = "https://yourserver.com/files/soil_test.pdf";
    private SharedPreferences preferences;
    private TextView profileName;
    private ImageView homeIcon, adIcon, profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_projects);

        SpeedView speedView = findViewById(R.id.speedometer);
        speedView.speedTo(750);

        homeIcon = findViewById(R.id.home_icon);
        adIcon = findViewById(R.id.upload_icon);


        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userEmail = preferences.getString("userEmail", null);

        if (userEmail != null) {
            fetchUserEmailAndPhone(userEmail);
            fetchUsername(userEmail);
        } else {
            Log.e("USER_ERROR", "User email not found in SharedPreferences");
        }

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


        // Initialize LinearLayouts
        LinearLayout engihome = findViewById(R.id.engihome8);
        LinearLayout engiupload = findViewById(R.id.engiupload7);
        LinearLayout engilogout = findViewById(R.id.engilayout7);

        profileName = findViewById(R.id.profileName);

        // Debugging: Check if they are found
        if (engihome == null || engiupload == null || engilogout == null) {
            Log.e("EngineerBasementActivity", "One or more LinearLayouts are null! Check XML IDs.");
            return; // Stop execution if views are null
        }

        // Set OnClickListeners
        engihome.setOnClickListener(v -> {
            saveActiveScreen("EngineerDashboardActivity");
            Intent intent = new Intent(EngineerProjectActivity.this, EngineerDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        engiupload.setOnClickListener(v -> {
            saveActiveScreen("EngineerUploadActivity");
            Intent intent = new Intent(EngineerProjectActivity.this, EngineerUploadActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        engilogout.setOnClickListener(v -> {
            saveActiveScreen("EngineerProfileActivity");
            Intent intent = new Intent(EngineerProjectActivity.this, EngineerProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Download Soil Test PDF
        CardView soilPdfDownload = findViewById(R.id.testpdf);
        soilPdfDownload.setOnClickListener(v -> showDownloadDialog());

        setActiveIcon();

    }
    private void saveActiveScreen(String screenName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("activeScreen", screenName);
        editor.apply();
    }

    private void setActiveIcon() {
        String currentActivity = getClass().getSimpleName();

        if (currentActivity.equals("EngineerDashboardActivity")) {
            homeIcon.setImageResource(R.drawable.active_home);
            adIcon.setImageResource(R.drawable.upload);
        } else if (currentActivity.equals("EngineerUploadActivity")) {
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.active_upload);
        } else {
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.upload);
        }
    }

    private void showDownloadDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_soil_pdf);

        // Set transparent background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button downloadButton = dialog.findViewById(R.id.download_pdf_btn);
        Button cancelButton = dialog.findViewById(R.id.cancel_btn);

        downloadButton.setOnClickListener(v -> {
            dialog.dismiss();
            startPaymentProcess();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void startPaymentProcess() {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userEmail = preferences.getString("userEmail", null);

        if (userEmail != null) {
            fetchUserDetailsAndPay();
        } else {
            Toast.makeText(this, "User email not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserEmailAndPhone(String userEmail) {
        String url = ApiConstants.BASE_URL + "/getUserContact?email=" + userEmail;
        Log.d("FETCH_DEBUG", "Fetching email and phone from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("FETCH_DEBUG", "Response received: " + response.toString());
                    try {
                        if (response.has("email") && response.has("phone")) {
                            String email = response.getString("email");
                            String phone = response.getString("phone");
                            Log.d("FETCH_DEBUG", "Email: " + email + ", Phone: " + phone);

                            // Update the UI
//                            TextView emailTextView = findViewById(R.id.emailText);
//                            TextView phoneTextView = findViewById(R.id.contactText);
//
//                            emailTextView.setText("Email: " + email);
//                            phoneTextView.setText("Mobile: " + phone);
                        } else {
                            Log.e("FETCH_ERROR", "Email or Phone key missing in response");
                        }
                    } catch (JSONException e) {
                        Log.e("FETCH_ERROR", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("FETCH_ERROR", "Error fetching email and phone: " + error.toString());
                    if (error.networkResponse != null) {
                        String responseData = new String(error.networkResponse.data);
                        Log.e("FETCH_ERROR", "Response Code: " + error.networkResponse.statusCode);
                        Log.e("FETCH_ERROR", "Response Data: " + responseData);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        RequestQueue queue = SSLHelper.getSecureRequestQueue(this);
        queue.add(request);
    }

    private void fetchUserDetailsAndPay() {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userEmail = preferences.getString("userEmail", null); // Fetch stored email

        if (userEmail == null) {
            Toast.makeText(this, "User email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConstants.BASE_URL + "/getUserContact?email=" + userEmail;
        Log.d("FETCH_DEBUG", "Fetching email and phone from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("FETCH_DEBUG", "Server Response: " + response.toString());
                        if (response.has("email") && response.has("phone")) {
                            String email = response.getString("email");
                            String phone = response.getString("phone");
                            Log.d("FETCH_DEBUG", "Fetched Email: " + email + ", Phone: " + phone);

                            // Start Razorpay Payment with the retrieved email and phone
                            startRazorpayPayment(email, phone);
                        } else {
                            Toast.makeText(this, "User details not found!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing user details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FETCH_ERROR", "Failed to fetch email and phone: " + error.toString());
                    Toast.makeText(this, "Failed to fetch user details!", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = SSLHelper.getSecureRequestQueue(this);
        queue.add(request);
    }

    private void startRazorpayPayment(String userEmail, String userPhone) {
        try {
            Checkout checkout = new Checkout();
            checkout.setKeyID(RAZORPAY_KEY);

            JSONObject options = new JSONObject();
            options.put("name", "Watpac Cibil App");
            options.put("description", "Report PDF Purchase");
            options.put("currency", "INR");
            options.put("amount", 50000);
            options.put("prefill.email", userEmail);
            options.put("prefill.contact", userPhone);

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Payment Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onPaymentSuccess(String razorpayPaymentId) {
        Toast.makeText(this, "Payment Successful! ID: " + razorpayPaymentId, Toast.LENGTH_SHORT).show();

        // Get company name from intent
        String companyName = getIntent().getStringExtra("company_name");
        if (companyName != null) {
            fetchCompanyPdfs(companyName);
        } else {
            Toast.makeText(this, "Company name not found!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed! Try Again.", Toast.LENGTH_SHORT).show();
    }

    private void fetchCompanyPdfs(String companyName) {
        String url = ApiConstants.BASE_URL + "/getCompanyPdfs?company_name=" + Uri.encode(companyName);
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

                            downloadCompanyPdfs(pdfUrls);
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
    }

    private void downloadCompanyPdfs(String[] pdfUrls) {
        for (String pdfUrl : pdfUrls) {
            downloadPdf(pdfUrl);
        }
        Toast.makeText(this, "All PDFs downloaded successfully!", Toast.LENGTH_LONG).show();
    }

    private void downloadPdf(String pdfUrl) {
        Uri uri = Uri.parse(pdfUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void fetchUsername(String userEmail) {
        // ✅ Ensure email is not empty
        if (userEmail == null || userEmail.isEmpty()) {
            Log.e("FETCH_ERROR", "No email found.");
            Toast.makeText(this, "Error: No email found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Encode email to prevent URL errors
        String encodedEmail = Uri.encode(userEmail);
        String url = ApiConstants.BASE_URL + "/getUsername?email=" + encodedEmail;

        Log.d("FETCH_DEBUG", "Fetching username from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.has("username")) {
                            String username = response.getString("username");
                            Log.d("FETCH_DEBUG", "Username found: " + username);
                            profileName.setText(username);
                        } else {
                            Log.e("FETCH_ERROR", "Username key missing in response");
                        }
                    } catch (JSONException e) {
                        Log.e("FETCH_ERROR", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("FETCH_ERROR", "Error fetching username: " + error.toString());
                    if (error.networkResponse != null) {
                        String responseData = new String(error.networkResponse.data);
                        Log.e("FETCH_ERROR", "Response Code: " + error.networkResponse.statusCode);
                        Log.e("FETCH_ERROR", "Response Data: " + responseData);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        RequestQueue queue = SSLHelper.getSecureRequestQueue(this);
        queue.add(request);
    }


}
