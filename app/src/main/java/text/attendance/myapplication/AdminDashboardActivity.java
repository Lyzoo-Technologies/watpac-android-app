package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout home, ad, profile;
    private ImageView homeIcon, adIcon;
    private TextView profileName;
    private SharedPreferences preferences;
    private LinearLayout companyContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize SharedPreferences
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Initialize views
        home = findViewById(R.id.home2);
        ad = findViewById(R.id.ad2);
        profile = findViewById(R.id.profile2);
        homeIcon = findViewById(R.id.homeIcon);
        adIcon = findViewById(R.id.adIcon);
        profileName = findViewById(R.id.profileName);

        LinearLayout project = findViewById(R.id.project);
        project.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminProjectActivity.class);
            startActivity(intent);
        });

        // Get stored email from SharedPreferences
        String userEmail = preferences.getString("userEmail", null);
        if (userEmail != null) {
            Log.d("USER_DEBUG", "Email found: " + userEmail);
            fetchUsername(userEmail);
        } else {
            Log.e("USER_ERROR", "User email not found in SharedPreferences");
            profileName.setText("Profile");
        }

        // Restore last active screen
        setActiveIcon();

        home.setOnClickListener(v -> {
            saveActiveScreen("AdminDashboardActivity");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        ad.setOnClickListener(v -> {
            saveActiveScreen("AdminAdActivity");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAdActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        profile.setOnClickListener(v -> {
            saveActiveScreen("ProfileActivity");
            Intent intent = new Intent(AdminDashboardActivity.this, EngineerProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        companyContainer = findViewById(R.id.companyContainer); // LinearLayout where CardViews will be added


        fetchCompanyNames();
        SSLHelper.allowAllHostnames();
    }

    // Fetch username using stored email
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

    private void saveActiveScreen(String screenName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("activeScreen", screenName);
        editor.apply();
    }

    private void setActiveIcon() {
        String currentActivity = getClass().getSimpleName();
        if (currentActivity.equals("AdminDashboardActivity")) {
            homeIcon.setImageResource(R.drawable.active_home);
            adIcon.setImageResource(R.drawable.radio);
        } else if (currentActivity.equals("AdminAdActivity")) {
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.active_radio);
        } else {
            homeIcon.setImageResource(R.drawable.home);
            adIcon.setImageResource(R.drawable.radio);
        }
    }

    private void fetchCompanyNames() {
        String url = ApiConstants.BASE_URL + "/get-company-names";
        Log.d("API_DEBUG", "Fetching companies from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray companies = response.getJSONArray("companies");
                        companyContainer.removeAllViews(); // Clear previous data

                        for (int i = 0; i < companies.length(); i++) {
                            JSONObject companyObj = companies.getJSONObject(i);
                            int companyId = companyObj.getInt("id"); // Order by ID
                            String email = companyObj.getString("email");
                            String companyName = companyObj.getString("company_name");
                            String mobileNumber = companyObj.optString("mobile_number", "N/A");
                            String address = companyObj.optString("address", "N/A");
                            String website = companyObj.optString("website", "N/A");
                            String gstNumber = companyObj.optString("gst_number", "N/A");

                            addCompanyCard(companyId, email, companyName, mobileNumber, address, website, gstNumber);
                        }
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("API_ERROR", "Error fetching data: " + error.toString()));

        RequestQueue queue = SSLHelper.getSecureRequestQueue(this);
        queue.add(request);
    }

    private void addCompanyCard(int companyId, String email, String companyName, String mobileNumber, String address, String website, String gst) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_company_card, companyContainer, false);

        CardView entireCard = cardView.findViewById(R.id.entirecard);
        TextView companyNameTextView = cardView.findViewById(R.id.companyNameTextView);

        companyNameTextView.setText(companyName);

        // Fetch JWT token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        entireCard.setOnClickListener(v -> {
            Log.d("CLICK_EVENT", "Navigating to UserProjectActivity with company ID: " + companyId);

            Intent intent = new Intent(this, AdminProjectActivity.class);
            intent.putExtra("company_id", companyId);
            intent.putExtra("company_name", companyName);
            intent.putExtra("mobile_number", mobileNumber);
            intent.putExtra("address", address);
            intent.putExtra("email", email);
            intent.putExtra("website", website);
            intent.putExtra("gst_number", gst);
            intent.putExtra("jwt_token", token);  // Pass token here

            startActivity(intent);
        });

        companyContainer.addView(cardView);
    }


}
