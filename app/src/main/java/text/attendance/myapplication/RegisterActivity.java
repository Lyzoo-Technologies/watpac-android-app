package text.attendance.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String SERVER_IP = "http://localhost:5000";
    private static final String REGISTER_URL = SERVER_IP + "/register";

    private String selectedRole = ""; // Stores the selected role

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Find UI elements
        EditText usernameInput = findViewById(R.id.usernames_input);
        EditText emailInput = findViewById(R.id.email_input);
        EditText phoneInput = findViewById(R.id.mobile_input);
        EditText passwordInput = findViewById(R.id.password_input);

        LinearLayout builderBox = findViewById(R.id.builder_box);
        LinearLayout investorBox = findViewById(R.id.investor_box);

        TextView signUpText = findViewById(R.id.sign_up_text);
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Role selection toggle
        builderBox.setOnClickListener(v -> setSelectedRole(builderBox, investorBox, "builder"));
        investorBox.setOnClickListener(v -> setSelectedRole(investorBox, builderBox, "investor"));

        findViewById(R.id.signUpImage).setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || selectedRole.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(RegisterActivity.this, username, email, phone, password, selectedRole);
        });
    }

    // Toggle role selection
    // Toggle role selection
    private void setSelectedRole(LinearLayout selected, LinearLayout unselected, String role) {
        selectedRole = role;

        // Set selected role: background #61A5F2, text white
        selected.setBackgroundTintList(getResources().getColorStateList(R.color.custom_blue, null));
        ((TextView) selected.getChildAt(0)).setTextColor(Color.WHITE);

        // Set unselected role: background white, text #61A5F2
        unselected.setBackgroundTintList(getResources().getColorStateList(android.R.color.white, null));
        ((TextView) unselected.getChildAt(0)).setTextColor(getResources().getColor(R.color.custom_blue, null));
    }


    // Register User API Call
    private void registerUser(Context context, String username, String email, String phone, String password, String role) {
        StringRequest request = new StringRequest(Request.Method.POST, REGISTER_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("userId")) {
                            int userId = jsonObject.getInt("userId");
                            Toast.makeText(context, "Registration successful! User ID: " + userId, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                        } else {
                            Toast.makeText(context, "Unexpected response", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("REGISTER_ERROR", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(context, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("REGISTER_ERROR", "Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                    Toast.makeText(context, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                params.put("phone", phone);
                params.put("email", email);
                params.put("role", role);
                return params;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }
}
