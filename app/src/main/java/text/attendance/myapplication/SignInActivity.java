package text.attendance.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SignInActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button signInBtn;
    private TextView signUpText, forgotPswd;
    private static final String LOGIN_URL = ApiConstants.LOGIN_URL;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        disableSSLCertificateValidation();

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        signInBtn = findViewById(R.id.signInbtn);
        signUpText = findViewById(R.id.sign_up_text);
        forgotPswd = findViewById(R.id.forgot_password_text);

        signInBtn.setOnClickListener(v -> loginUser());

        // Password toggle functionality
        passwordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getX() >= passwordInput.getWidth() - passwordInput.getCompoundPaddingRight()) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        forgotPswd.setOnClickListener(v ->{
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        SSLHelper.allowAllHostnames();
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("email", email);
            jsonRequest.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, jsonRequest,
                response -> {
                    Log.d("API_RESPONSE", "Success: " + response.toString());  // ✅ Log response
                    try {
                        if (response.has("token")) {
                            String token = response.getString("token");
                            JSONObject userObject = response.getJSONObject("user");
                            String role = userObject.getString("role");
                            String phone = userObject.getString("phone"); // Fetch phone number from response
                            String emailFromResponse = userObject.getString("email"); // Fetch email from response

                            // Save user details and token in SharedPreferences
                            saveLoginStatus(true, role, emailFromResponse, phone);
                            saveToken(token);
                            Toast.makeText(SignInActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            // Navigate to the appropriate dashboard based on role
                            Intent intent;
                            if ("Admin".equalsIgnoreCase(role)) {
                                intent = new Intent(SignInActivity.this, AdminDashboardActivity.class);
                            } else if ("Builder".equalsIgnoreCase(role)) {
                                intent = new Intent(SignInActivity.this, EngineerDashboardActivity.class);
                            } else {
                                intent = new Intent(SignInActivity.this, UserDashboardActivity.class);
                            }

                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SignInActivity.this, "Response parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Volley Error: " + error.toString());  // ✅ Log error
                    Toast.makeText(SignInActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // ✅ Save login status in SharedPreferences
    private void saveLoginStatus(boolean isLoggedIn, String role, String email, String phone) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.putString("userRole", role);
        editor.putString("userEmail", email);
        editor.putString("userPhone", phone);  // Save phone number as well
        editor.apply();
    }

    // ✅ Function to Save Token (JWT)
    private void saveToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jwt_token", token);
        editor.apply();
    }

    // Password visibility toggle method
    private void togglePasswordVisibility() {
        if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pass_strike, 0);
        } else {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password, 0);
        }
        passwordInput.setSelection(passwordInput.getText().length());
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

}