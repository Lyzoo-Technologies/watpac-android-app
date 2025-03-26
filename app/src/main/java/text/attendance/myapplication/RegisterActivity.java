package text.attendance.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



public class RegisterActivity extends AppCompatActivity {

    private static final String REGISTER_URL = ApiConstants.REGISTER_URL;
    private String selectedRole = "Builder";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        disableSSLCertificateValidation();

        EditText usernameInput = findViewById(R.id.usernames_input);
        EditText emailInput = findViewById(R.id.email_input);
        EditText phoneInput = findViewById(R.id.mobile_input);
        EditText passwordInput = findViewById(R.id.password_input);

        LinearLayout builderBox = findViewById(R.id.builder_box);
        LinearLayout investorBox = findViewById(R.id.investor_box);
        TextView signUpText = findViewById(R.id.sign_up_text);
        FrameLayout signUpButton = findViewById(R.id.signupbtn);

        signUpText.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, SignInActivity.class)));
        builderBox.setOnClickListener(v -> setSelectedRole(builderBox, investorBox, "Builder"));
        investorBox.setOnClickListener(v -> setSelectedRole(investorBox, builderBox, "Investor"));



        signUpButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!validateInputs(username, email, phone, password)) return;
            registerUser(RegisterActivity.this, username, email, phone, password, selectedRole);
        });

// Password visibility toggle
        passwordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getX() >= passwordInput.getWidth() - passwordInput.getCompoundPaddingRight()) {
                    if (passwordInput.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                        passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, getResources().getDrawable(R.drawable.password, null), null);
                    } else {
                        passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, getResources().getDrawable(R.drawable.pass_strike, null), null);
                    }
                    passwordInput.setSelection(passwordInput.getText().length());
                    return true;
                }
            }
            return false;
        });
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

    private void setSelectedRole(LinearLayout selected, LinearLayout unselected, String role) {
        selectedRole = role;
        selected.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.custom_blue));
        ((TextView) selected.getChildAt(0)).setTextColor(Color.WHITE);
        unselected.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        ((TextView) unselected.getChildAt(0)).setTextColor(ContextCompat.getColor(this, R.color.custom_blue));
    }

    private boolean validateInputs(String username, String email, String phone, String password) {
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Pattern.compile("^[0-9]{10}$").matcher(phone).matches()) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerUser(Context context, String username, String email, String phone, String password, String role) {
        Log.d("REGISTER_DEBUG", "Attempting registration with role: " + role);
        String url = ApiConstants.REGISTER_URL;
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("password", password);
            requestBody.put("phone", phone);
            requestBody.put("email", email);
            requestBody.put("role", role);
        } catch (JSONException e) {
            Log.e("REGISTER_ERROR", "JSON Exception: " + e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                response -> {
                    Log.d("REGISTER_SUCCESS", "Response: " + response.toString());
                    Toast.makeText(context, "Registration successful! Verify OTP.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, OtpVerificationActivity.class);
                    intent.putExtra("phone", phone);
                    context.startActivity(intent);
                },
                error -> {
                    Log.e("REGISTER_ERROR", "Volley Error: " + error.toString());
                    Toast.makeText(context, "Network error. Try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(1500000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = SSLHelper.getSecureRequestQueue(context);
        if (requestQueue != null) requestQueue.add(jsonObjectRequest);
    }
}
