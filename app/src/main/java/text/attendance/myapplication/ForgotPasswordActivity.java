package text.attendance.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String CHECK_EMAIL_URL = ApiConstants.CHECK_EMAIL_URL;
    private EditText emailInput;
    private TextView submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pswd);

        emailInput = findViewById(R.id.change_signin_email_input);
        submitButton = findViewById(R.id.textView8);

        submitButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            checkEmailExists(ForgotPasswordActivity.this, email);
        });
    }

    private void checkEmailExists(Context context, String email) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
        } catch (JSONException e) {
            Log.e("EMAIL_CHECK_ERROR", "JSON Exception: " + e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, CHECK_EMAIL_URL, requestBody,
                response -> {
                    Log.d("EMAIL_CHECK_SUCCESS", "Response: " + response.toString());
                    try {
                        if (response.has("email")) {
                            String foundEmail = response.getString("email");
                            Intent intent = new Intent(context, ConfirmPasswordActivity.class);
                            intent.putExtra("email", foundEmail);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        Log.e("EMAIL_CHECK_ERROR", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(context, "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("EMAIL_CHECK_ERROR", "Volley Error: " + error.networkResponse.statusCode + " - " + new String(error.networkResponse.data));
                    } else {
                        Log.e("EMAIL_CHECK_ERROR", "Volley Error: " + error.toString());
                    }
                    Toast.makeText(context, "Email not found. Please try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        SSLHelper.getSecureRequestQueue(context).add(jsonObjectRequest);
    }
}
