
package text.attendance.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDashboardActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private TextView profileName;
    private LinearLayout companyContainer;
    private CompanyAdapter adapter;
    private List<String> companyList;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private RequestQueue requestQueue;
    private static final String COMPANY_LIST_URL = ApiConstants.BASE_URL + "/get-company-list";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        Log.d("ACTIVITY_LAUNCH", "UserDashboardActivity Started!");

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Initialize UI elements
        LinearLayout userhome = findViewById(R.id.userhome2);
        LinearLayout userlogout = findViewById(R.id.userlogout2);
        profileName = findViewById(R.id.profileName);
        companyContainer = findViewById(R.id.companyContainer);
        LinearLayout projectLayout = findViewById(R.id.project);

        // Navigation Handlers
        projectLayout.setOnClickListener(view -> startActivity(new Intent(this, UserProjectActivity.class)));
        userhome.setOnClickListener(view -> startActivity(new Intent(this, UserCommentActivity.class)));
        userlogout.setOnClickListener(view -> startActivity(new Intent(this, EngineerProfileActivity.class)));

        // Fetch Profile Name
        String userEmail = preferences.getString("userEmail", null);
        if (userEmail != null) {
            Log.d("USER_DEBUG", "Email found: " + userEmail);
            fetchUsername(userEmail);
        } else {
            Log.e("USER_ERROR", "User email not found in SharedPreferences");
            profileName.setText("Profile");
        }

        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestQueue = SSLHelper.getSecureRequestQueue(this);
        companyList = new ArrayList<>();
        adapter = new CompanyAdapter(companyList);
        recyclerView.setAdapter(adapter);

        // Fetch company list dynamically
        fetchCompanyList();

        // Add search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Fetch Company Names
        fetchCompanyNames();
        SSLHelper.allowAllHostnames();

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

                            // Add company card
                            addCompanyCard(companyId, email, companyName, mobileNumber, address, website, gstNumber);

                            // Insert Ad View after every 10 cards
                            if ((i + 1) % 10 == 0) {
                                addAdView();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("API_ERROR", "Error fetching data: " + error.toString()));

        RequestQueue queue = SSLHelper.getSecureRequestQueue(this);
        queue.add(request);
    }
    private void addAdView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View adView = inflater.inflate(R.layout.activity_ad_view, companyContainer, false);

        ImageView closeButton = adView.findViewById(R.id.closeButton);
        ImageView adImageView = adView.findViewById(R.id.adImageView); // Ensure this ID exists in `activity_ad_view.xml`

        // Fetch ad image
        fetchAdImage(adImageView);

        // Close button to remove the ad view
        closeButton.setOnClickListener(v -> {
            companyContainer.removeView(adView);
            Log.d("AD_DEBUG", "Ad view closed.");
        });

        companyContainer.addView(adView);
        Log.d("AD_DEBUG", "Ad view added to the layout.");
    }

    private void fetchAdImage(ImageView adImageView) {
        String url = ApiConstants.BASE_URL + "/adscroll";
        Log.d("API_DEBUG", "Fetching ad images from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("API_RESPONSE", "Full response: " + response.toString());

                        if (response.length() > 0) {
                            JSONObject adObj = response.getJSONObject(0);
                            String imageData = adObj.getString("image");

                            // Log the first 100 characters of the image data
                            Log.d("IMAGE_DATA", "Image data sample: " +
                                    (imageData.length() > 100 ? imageData.substring(0, 100) + "..." : imageData));

                            // Decode and display
                            byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            if (decodedByte != null) {
                                adImageView.setImageBitmap(decodedByte);
                                Log.d("IMAGE_DEBUG", "Bitmap dimensions: " +
                                        decodedByte.getWidth() + "x" + decodedByte.getHeight());
                            } else {
                                Log.e("IMAGE_ERROR", "Failed to decode bitmap");
                                adImageView.setImageResource(R.drawable.placeholder_image);
                            }
                        } else {
                            Log.e("API_ERROR", "No ads available in response");
                            adImageView.setImageResource(R.drawable.placeholder_image);
                        }
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "JSON Parsing error: " + e.getMessage());
                        adImageView.setImageResource(R.drawable.placeholder_image);
                    } catch (IllegalArgumentException e) {
                        Log.e("IMAGE_ERROR", "Base64 decoding error: " + e.getMessage());
                        adImageView.setImageResource(R.drawable.placeholder_image);
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Error fetching ad image: " + error.toString());
                    adImageView.setImageResource(R.drawable.placeholder_image);
                    if (error.networkResponse != null) {
                        String responseData = new String(error.networkResponse.data);
                        Log.e("API_ERROR", "Response Code: " + error.networkResponse.statusCode);
                        Log.e("API_ERROR", "Response Data: " + responseData);
                    }
                });

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

            Intent intent = new Intent(this, UserProjectActivity.class);
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

    private void fetchCompanyList() {
        Log.d("API_DEBUG", "Fetching company list from: " + COMPANY_LIST_URL);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, COMPANY_LIST_URL, null,
                response -> {
                    companyList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject companyObj = response.getJSONObject(i);
                            String companyName = companyObj.getString("company_name"); // Ensure correct JSON field
                            companyList.add(companyName);
                        } catch (JSONException e) {
                            Log.e("API_ERROR", "JSON Parsing error: " + e.getMessage());
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("API_ERROR", "Error fetching company list: " + error.toString());
                    Toast.makeText(this, "Failed to load company list", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }


}
