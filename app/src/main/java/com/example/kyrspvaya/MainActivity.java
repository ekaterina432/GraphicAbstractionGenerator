package com.example.kyrspvaya;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private Button btnGenerate;
    private Button btnFractal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGenerate = findViewById(R.id.btnGenerate);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGeneratorActivity();
            }
        });

        btnFractal = findViewById(R.id.btnFractal);
        btnFractal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFractalActivity();
            }
        });
    }

    private void startGeneratorActivity() {
        Intent intent = new Intent(MainActivity.this, Generator.class);
        startActivity(intent);
    }

    private void startFractalActivity() {
        Intent intent = new Intent(MainActivity.this, Fractal.class);
        startActivity(intent);
    }
}
