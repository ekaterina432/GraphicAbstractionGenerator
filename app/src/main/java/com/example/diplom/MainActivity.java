package com.example.diplom;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.LinearLayout;

import com.example.kyrspvaya.R;

public class MainActivity extends AppCompatActivity {

    private LinearLayout btnGenerate;
    private LinearLayout btnFractal;
    private LinearLayout btnPainting;
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

        btnPainting = findViewById(R.id.btnFingerPainting);
        btnPainting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPaintingActivity();
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

    private void startPaintingActivity() {
        Intent intent = new Intent(MainActivity.this, FingerPainting.class);
        startActivity(intent);
    }
}
