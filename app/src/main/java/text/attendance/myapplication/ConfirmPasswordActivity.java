package text.attendance.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ConfirmPasswordActivity extends AppCompatActivity {

    private static final String RESET_PASSWORD_URL = ApiConstants.RESET_PASSWORD_URL;
    private EditText emailInput, newPasswordInput;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_password);

        emailInput = findViewById(R.id.change_signin_email_input1);
        newPasswordInput = findViewById(R.id.new_password);
        resetButton = findViewById(R.id.resetBtn);

        String email = getIntent().getStringExtra("email");
        if (email != null) {
            emailInput.setText(email);
        }

        resetButton.setOnClickListener(v -> {
            String emailText = emailInput.getText().toString().trim();
            String newPasswordText = newPasswordInput.getText().toString().trim();

            if (newPasswordText.isEmpty()) {
                Toast.makeText(ConfirmPasswordActivity.this, "Enter a new password", Toast.LENGTH_SHORT).show();
                return;
            }

            resetPassword(ConfirmPasswordActivity.this, emailText, newPasswordText);
        });

        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordInput.setCompoundDrawablesWithIntrinsicBounds(
                null, null, getResources().getDrawable(R.drawable.pass_strike, null), null);

        newPasswordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getX() >= newPasswordInput.getWidth() - newPasswordInput.getCompoundPaddingRight()) {
                    if (newPasswordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        newPasswordInput.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, getResources().getDrawable(R.drawable.password, null), null);
                    } else {
                        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        newPasswordInput.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, getResources().getDrawable(R.drawable.pass_strike, null), null);
                    }
                    newPasswordInput.setSelection(newPasswordInput.getText().length());
                    return true;
                }
            }
            return false;
        });

    }

    private void resetPassword(Context context, String email, String newPassword) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("newPassword", newPassword);
        } catch (JSONException e) {
            Log.e("RESET_ERROR", "JSON Exception: " + e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, RESET_PASSWORD_URL, requestBody,
                response -> {
                    Log.d("RESET_SUCCESS", "Response: " + response.toString());
                    Toast.makeText(context, "Password reset successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(context, SignInActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("RESET_ERROR", "Volley Error: " + error.networkResponse.statusCode + " - " + new String(error.networkResponse.data));
                    } else {
                        Log.e("RESET_ERROR", "Volley Error: " + error.toString());
                    }
                    Toast.makeText(context, "Failed to reset password. Try again.", Toast.LENGTH_SHORT).show();
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
