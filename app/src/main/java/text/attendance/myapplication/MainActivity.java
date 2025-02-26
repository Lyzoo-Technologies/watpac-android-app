package text.attendance.myapplication;  // Update with your package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);

        // Finish MainActivity so it doesn't stay in the back stack
        finish();

    }
}