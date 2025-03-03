package text.attendance.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class SignInActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        TextView signUpText = findViewById(R.id.sign_up_text);
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button signInBtn = findViewById(R.id.signInbtn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        TextView forgotPswd = findViewById(R.id.forgot_password_text);
        forgotPswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });


        EditText passwordInput = findViewById(R.id.password_input);  // Assuming your EditText is password_input

        // Set the password toggle icon click listener on the drawableRight of the EditText
        passwordInput.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getX() >= passwordInput.getWidth() - passwordInput.getCompoundPaddingRight()) {
                        // Toggle password visibility
                        if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
                            // Hide password
                            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                    null, null, getResources().getDrawable(R.drawable.pass_strike, null), null); // Closed eye icon
                        } else {
                            // Show password
                            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                    null, null, getResources().getDrawable(R.drawable.password, null), null); // Open eye icon
                        }

                        // Move the cursor to the end of the text
                        passwordInput.setSelection(passwordInput.getText().length());

                        return true; // Consume the event
                    }
                }
                return false; // Let the event propagate
            }
        });

    }
}
