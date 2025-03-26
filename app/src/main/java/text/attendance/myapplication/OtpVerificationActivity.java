package text.attendance.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4;
    private Button verifyButton;
    private TextView resendOtp;
    private String phone;  // User's phone number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        // Initialize OTP input fields
        otpDigit1 = findViewById(R.id.otp_digit_1);
        otpDigit2 = findViewById(R.id.otp_digit_2);
        otpDigit3 = findViewById(R.id.otp_digit_3);
        otpDigit4 = findViewById(R.id.otp_digit_4);

        verifyButton = findViewById(R.id.verify_button);
        resendOtp = findViewById(R.id.resend_otp);

        // Retrieve phone number from Intent
        phone = getIntent().getStringExtra("phone");
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Phone number missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verify OTP when button is clicked
        verifyButton.setOnClickListener(view -> {
            String enteredOtp = getEnteredOtp();
            if (enteredOtp.length() != 4) {  // ✅ Changed to 4-digit OTP
                Toast.makeText(OtpVerificationActivity.this, "Enter a valid 4-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(OtpVerificationActivity.this, phone, enteredOtp);
        });

        resendOtp.setOnClickListener(view -> {
            resendOtp(OtpVerificationActivity.this, phone);
        });

    }

    // ✅ Modified to collect only 4-digit OTP
    private String getEnteredOtp() {
        return otpDigit1.getText().toString().trim() +
                otpDigit2.getText().toString().trim() +
                otpDigit3.getText().toString().trim() +
                otpDigit4.getText().toString().trim();
    }

    private void verifyOtp(Context context, String phone, String enteredOtp) {
        Log.d("OTP_DEBUG", "Verifying OTP for phone: " + phone + " with OTP: " + enteredOtp);

        String url = ApiConstants.VERIFY_OTP_URL;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("phone", phone);
            requestBody.put("otp", enteredOtp);
        } catch (JSONException e) {
            Log.e("OTP_ERROR", "JSON Exception: " + e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                response -> {
                    Log.d("OTP_SUCCESS", "Response: " + response.toString());

                    // Check if OTP verification was successful
                    if (response.has("message") && response.optString("message").equals("OTP verified successfully")) {
                        Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show();

                        // Redirect to SignInActivity after successful OTP verification
                        Intent intent = new Intent(context, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    } else {
                        Log.e("OTP_ERROR", "Unexpected response: " + response.toString());
                        Toast.makeText(context, "OTP Verification failed!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("OTP_VERIFY_ERROR", "Volley Error: " + error.toString());

                    String errorMessage = "Invalid or Expired OTP";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorResponse = new String(error.networkResponse.data);
                            Log.e("OTP_ERROR_RESPONSE", "Error Response: " + errorResponse);
                            JSONObject errorJson = new JSONObject(errorResponse);
                            if (errorJson.has("error")) {
                                errorMessage = errorJson.getString("error");
                            }
                        } catch (Exception e) {
                            Log.e("OTP_ERROR", "Error parsing error response: " + e.getMessage());
                        }
                    }

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                });

        SSLHelper.getSecureRequestQueue(context).add(jsonObjectRequest);
    }



    private void resendOtp(Context context, String phone) {
        Log.d("OTP_RESEND", "Resending OTP for phone: " + phone);

        String url = ApiConstants.RESEND_OTP_URL;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("phone", phone);
        } catch (JSONException e) {
            Log.e("OTP_RESEND_ERROR", "JSON Exception: " + e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                response -> {
                    Toast.makeText(context, "New OTP sent!", Toast.LENGTH_SHORT).show();
                    Log.d("OTP_RESEND_SUCCESS", "Response: " + response.toString());
                },
                error -> {
                    Log.e("OTP_RESEND_ERROR", "Volley Error: " + error.toString());
                    Toast.makeText(context, "Failed to resend OTP", Toast.LENGTH_SHORT).show();
                });

        SSLHelper.getSecureRequestQueue(context).add(jsonObjectRequest);
    }

    private void redirectToDashboard(Context context, String role) {
        Intent intent;

        switch (role) {
            case "Admin":
                intent = new Intent(context, SignInActivity.class);
                break;
            case "Builder":
                intent = new Intent(context, SignInActivity.class);
                break;
            case "Investor":
                intent = new Intent(context, SignInActivity.class);
                break;
            default:
                Toast.makeText(context, "Unknown role!", Toast.LENGTH_SHORT).show();
                return;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

}
