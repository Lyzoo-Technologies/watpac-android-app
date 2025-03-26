package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class EngineerProfileActivity extends AppCompatActivity {

    private TextView usernameLayout;
    private SharedPreferences preferences;
    private ImageView profileIcon; // Declare the ImageView for profile icon

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_profile);

        usernameLayout = findViewById(R.id.usernameLayout);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Reference the ImageView for profile icon
        profileIcon = findViewById(R.id.profile_icon);

        // Set the profile icon to 'activity_account'
        profileIcon.setImageResource(R.drawable.active_account); // Update icon dynamically

        LinearLayout profilePassword = findViewById(R.id.profilePswd);

        profilePassword.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String userEmail = preferences.getString("userEmail", ""); // Retrieve email

            Intent intent = new Intent(EngineerProfileActivity.this, ConfirmPasswordActivity.class);
            intent.putExtra("USER_EMAIL", userEmail); // Pass email
            startActivity(intent);
        });

        LinearLayout profileLogout = findViewById(R.id.profileLogout);

        profileLogout.setOnClickListener(v -> logoutUser());

        LinearLayout carLogout = findViewById(R.id.carLogout);
        TextView emailTextView = findViewById(R.id.emailTextView);

        carLogout.setOnClickListener(v -> {
            if (emailTextView.getVisibility() == View.VISIBLE) {
                emailTextView.setVisibility(View.GONE); // Hide email if visible
            } else {
                emailTextView.setVisibility(View.VISIBLE); // Show email if hidden
            }
        });

        // Footer redirection click listeners
        LinearLayout homeLayout = findViewById(R.id.home);
        LinearLayout uploadLayout = findViewById(R.id.ad);

        homeLayout.setOnClickListener(v -> {
            Intent homeIntent = new Intent(EngineerProfileActivity.this, EngineerDashboardActivity.class);
            startActivity(homeIntent);
        });

        uploadLayout.setOnClickListener(v -> {
            Intent uploadIntent = new Intent(EngineerProfileActivity.this, EngineerUploadActivity.class);
            startActivity(uploadIntent);
        });

        // Fetch Profile Name
        String userEmail = preferences.getString("userEmail", null);
        if (userEmail != null) {
            Log.d("USER_DEBUG", "Email found: " + userEmail);
            fetchUsername(userEmail);
        } else {
            Log.e("USER_ERROR", "User email not found in SharedPreferences");
            usernameLayout.setText("Username");
        }
    }

    private void fetchUsername(String userEmail) {
        String url = ApiConstants.BASE_URL + "/getUsername?email=" + userEmail;
        Log.d("FETCH_DEBUG", "Fetching username from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("FETCH_DEBUG", "Response received: " + response.toString());
                    try {
                        if (response.has("username")) {
                            String username = response.getString("username");
                            Log.d("FETCH_DEBUG", "Username found: " + username);
                            usernameLayout.setText(username);
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

    private void logoutUser() {
        // Clear ALL stored data to reset session
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false); // Explicitly set to false
        editor.remove("userRole"); // Remove user role
        editor.remove("userEmail"); // Remove stored email
        editor.apply();

        // Redirect to SignInActivity
        Intent intent = new Intent(EngineerProfileActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
        startActivity(intent);
        finish();
    }

}
