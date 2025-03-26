package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class EngineerUploadActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private ImageView homeIcon, adIcon;
    private TextView profileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_upload);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        profileName = findViewById(R.id.profileName);

        // ✅ Fetch userEmail from SharedPreferences
        String userEmail = preferences.getString("userEmail", null);

        if (userEmail != null) {
            fetchUsername(userEmail);
        } else {
            Log.e("USER_ERROR", "User email not found in SharedPreferences");
        }

        // Reference to CardView inside onCreate()
        CardView cardView = findViewById(R.id.layout1);
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerCompanyDetailsActivity.class);
            startActivity(intent);
        });

        CardView cardView1 = findViewById(R.id.layout2);
        cardView1.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerBasementActivity.class);
            startActivity(intent);
        });

        CardView cardView2 = findViewById(R.id.layout3);
        cardView2.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerLintelActivity.class);
            startActivity(intent);
        });

        CardView cardView3 = findViewById(R.id.layout4);
        cardView3.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerCentringActivity.class);
            startActivity(intent);
        });

        CardView cardView4 = findViewById(R.id.layout5);
        cardView4.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerWaterproofActivity.class);
            startActivity(intent);
        });

        CardView cardView5 = findViewById(R.id.layout6);
        cardView5.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerTankActivity.class);
            startActivity(intent);
        });

        CardView cardView6 = findViewById(R.id.layout7);
        cardView6.setOnClickListener(v -> {
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerFinalActivity.class);
            startActivity(intent);
        });

        // Initialize LinearLayouts
        LinearLayout engihome = findViewById(R.id.engihome10);
        LinearLayout engiupload = findViewById(R.id.engiupload10);
        LinearLayout engilogout = findViewById(R.id.engilogout10);

        homeIcon = findViewById(R.id.home_icon);
        adIcon = findViewById(R.id.upload_icon);

        // Debugging: Check if they are found
        if (engihome == null || engiupload == null || engilogout == null) {
            Log.e("EngineerBasementActivity", "One or more LinearLayouts are null! Check XML IDs.");
            return; // Stop execution if views are null
        }

        setActiveIcon();

        // Set OnClickListeners
        engihome.setOnClickListener(v -> {
            saveActiveScreen("EngineerDashboardActivity");
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        engiupload.setOnClickListener(v -> {
            saveActiveScreen("EngineerUploadActivity");
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerUploadActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        engilogout.setOnClickListener(v -> {
            saveActiveScreen("ProfileActivity");
            Intent intent = new Intent(EngineerUploadActivity.this, EngineerProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

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
