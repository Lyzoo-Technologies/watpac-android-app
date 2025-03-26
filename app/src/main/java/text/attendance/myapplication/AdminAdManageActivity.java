package text.attendance.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminAdManageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdAdapter adAdapter;
    private ArrayList<AdModel> adList = new ArrayList<>();
    private RequestQueue requestQueue;

    private LinearLayout homeLayout, adLayout, logoutLayout;


    private static final String FETCH_URL = ApiConstants.BASE_URL + "/companies";
    private static final String DELETE_URL = ApiConstants.BASE_URL + "/company-ads/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ad_manage);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adAdapter = new AdAdapter(this, adList);
        recyclerView.setAdapter(adAdapter);

        requestQueue = Volley.newRequestQueue(this);

        // Initialize footer layouts
        homeLayout = findViewById(R.id.home);
        adLayout = findViewById(R.id.ad);
        logoutLayout = findViewById(R.id.logout);

        // Set onClick listeners
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(AdminAdManageActivity.this, AdminDashboardActivity.class);
                startActivity(homeIntent);
                finish(); // Close the current activity
            }
        });

        adLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adIntent = new Intent(AdminAdManageActivity.this, AdminAdActivity.class);
                startActivity(adIntent);
                finish(); // Close the current activity
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(AdminAdManageActivity.this, AdminProfileActivity.class);
                startActivity(profileIntent);
                finish(); // Close the current activity
            }
        });

        fetchAds();
        reduceCardGap(4);
    }

    private void fetchAds() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, FETCH_URL, null,
                response -> {
                    adList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int id = obj.getInt("id");
                            String companyName = obj.getString("company_name");
                            String phoneNumber = obj.getString("phone_number");

                            adList.add(new AdModel(id, companyName, phoneNumber));
                        }
                        adAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("FetchAdsError", "Parsing error: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Parsing error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FetchAdsError", "Network error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Error fetching data!", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonArrayRequest);
    }

    private void deleteAd(int adId, int position) {
        StringRequest request = new StringRequest(Request.Method.DELETE, DELETE_URL + adId,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");

                        if (message.equals("Ad deleted successfully")) {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            adList.remove(position);
                            adAdapter.notifyItemRemoved(position);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("DeleteAdError", "Parsing error: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error parsing response!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        Toast.makeText(getApplicationContext(), "Ad not found", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("DeleteAdError", "Error deleting ad: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "Server error while deleting ad", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(request);
    }

    private class AdAdapter extends RecyclerView.Adapter<AdAdapter.AdViewHolder> {
        private Context context;
        private ArrayList<AdModel> ads;

        public AdAdapter(Context context, ArrayList<AdModel> ads) {
            this.context = context;
            this.ads = ads;
        }

        @NonNull
        @Override
        public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.ad_item, parent, false);
            return new AdViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
            AdModel ad = ads.get(position);
            holder.companyName.setText(ad.getCompanyName());
            holder.phoneNumber.setText(ad.getPhoneNumber());

            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Ad")
                        .setMessage("Are you sure you want to delete this ad?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteAd(ad.getId(), position))
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return ads.size();
        }

        public class AdViewHolder extends RecyclerView.ViewHolder {
            TextView companyName, phoneNumber;
            Button deleteButton;

            public AdViewHolder(@NonNull View itemView) {
                super(itemView);
                companyName = itemView.findViewById(R.id.company_name_text);
                phoneNumber = itemView.findViewById(R.id.phone_number_text);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }

    private class AdModel {
        private int id;
        private String companyName;
        private String phoneNumber;

        public AdModel(int id, String companyName, String phoneNumber) {
            this.id = id;
            this.companyName = companyName;
            this.phoneNumber = phoneNumber;
        }

        public int getId() {
            return id;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
    }

    private void reduceCardGap(int spaceInDp) {
        int spacingInPixels = (int) (5 * getResources().getDisplayMetrics().density); // Fixed to 5dp
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacingInPixels;
            }
        });
    }

}
