package text.attendance.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.anastr.speedviewlib.SpeedView;

import java.nio.BufferUnderflowException;

public class EngineerProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_projects);

        SpeedView speedView = findViewById(R.id.speedometer);
        speedView.speedTo(750);
    }
}
