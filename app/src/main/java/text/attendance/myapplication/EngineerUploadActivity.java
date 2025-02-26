package text.attendance.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class EngineerUploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_upload);

        // Reference to CardView inside onCreate()
        CardView cardView = findViewById(R.id.layout1);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EngineerCompanyDetailsActivity
                Intent intent = new Intent(EngineerUploadActivity.this, EngineerCompanyDetailsActivity.class);
                startActivity(intent);
            }
        });

        CardView cardView1 = findViewById(R.id.layout2);
        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EngineerCompanyDetailsActivity
                Intent intent = new Intent(EngineerUploadActivity.this, EngineerBasementActivity.class);
                startActivity(intent);
            }
        });

        CardView cardView2 = findViewById(R.id.layout3);
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EngineerCompanyDetailsActivity
                Intent intent = new Intent(EngineerUploadActivity.this, EngineerLintelActivity.class);
                startActivity(intent);
            }
        });

        CardView cardView3 = findViewById(R.id.layout4);
        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EngineerCompanyDetailsActivity
                Intent intent = new Intent(EngineerUploadActivity.this, EngineerCentringActivity.class);
                startActivity(intent);
            }
        });

        CardView cardView4 = findViewById(R.id.layout5);
        cardView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EngineerCompanyDetailsActivity
                Intent intent = new Intent(EngineerUploadActivity.this, EngineerWaterproofActivity.class);
                startActivity(intent);
            }
        });

        CardView cardView5 = findViewById(R.id.layout6);
        cardView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EngineerCompanyDetailsActivity
                Intent intent = new Intent(EngineerUploadActivity.this, EngineerTankActivity.class);
                startActivity(intent);
            }
        });

        CardView cardView6 = findViewById(R.id.layout7);
        cardView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EngineerCompanyDetailsActivity
                Intent intent = new Intent(EngineerUploadActivity.this, EngineerFinalActivity.class);
                startActivity(intent);
            }
        });
    }
}
