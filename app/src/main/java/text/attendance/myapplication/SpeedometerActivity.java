package text.attendance.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class SpeedometerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedometer); // Ensure this layout contains the SpeedView

        SpeedView speedView = findViewById(R.id.speedometer);
        speedView.speedTo(750);
    }
}
