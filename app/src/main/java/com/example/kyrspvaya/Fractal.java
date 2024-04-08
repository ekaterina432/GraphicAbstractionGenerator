package com.example.kyrspvaya;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

public class Fractal extends AppCompatActivity{
    private Spinner fileSpinner;
    private String axiom;
    private double angle;
    private String direction;
    private FrameLayout container;
    private ImageButton backButton;
    private Button generateButton;
    private ImageButton downloadButton;
    private Random random;
    private int screenWidth;
    private int screenHeight;
    private int iterations;
    private Map<Character, String> rules;
    private Stack<Pair<Double, Double>> savedStates;
    private List<Pair<Double, Double>> lSystPoints;
    private double re, gree, wi;
    private double randAngle;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fractal);

        container = findViewById(R.id.container);
        backButton = findViewById(R.id.btnBack);
        generateButton = findViewById(R.id.btnGenerate1);
        downloadButton = findViewById(R.id.btnDownload);
        fileSpinner = findViewById(R.id.fileSpinner);
        random = new Random();
        rules = new HashMap<>(); // Initialize rules map
        savedStates = new Stack<>();
        lSystPoints = new ArrayList<>();
        int[] gradientColors = getRandomGradientColors();
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors);
        container.setBackground(gradientDrawable);
        String[] fileList = getResources().getStringArray(R.array.spinner_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileSpinner.setAdapter(adapter);

        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedFileName = (String) adapterView.getItemAtPosition(position);
                readFile(selectedFileName);
                generateAbstraction();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(Fractal.this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }

        });
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAbstraction();
            }
        });

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage();
            }
        });
    }
    private void downloadImage() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "generated_image_" + timeStamp + ".png";
        Bitmap generatedImage = generateImage();
        String picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File imageFile = new File(picturesDirectory, fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            generatedImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            MediaScannerConnection.scanFile(this, new String[]{imageFile.getAbsolutePath()}, null, null);
            Toast.makeText(this, "Изображение успешно скачано", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при скачивании изображения", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap generateImage() {
        int width = container.getWidth();
        int height = container.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        container.draw(canvas);

        return bitmap;
    }

    private void readFile(String fileName) {
        BufferedReader reader = null;
        String fileNameWithoutExtension = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'));


        try {
            int resId = getResources().getIdentifier(fileNameWithoutExtension, "raw", getPackageName());
            InputStream inputStream = getResources().openRawResource(resId);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            if ((line = reader.readLine()) != null) {
                String[] parameters = line.split(" ");
                axiom = parameters[0];
                System.out.println(axiom);
                angle = Integer.parseInt(parameters[1]);
                direction = parameters[2];
                rules.clear();
                while ((line = reader.readLine()) != null) {
                    String[] rule = line.split(">");
                    rules.put(rule[0].charAt(0), rule[1]);
                }
                Toast.makeText(this, "File loaded successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "File is empty", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing file content", Toast.LENGTH_SHORT).show();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void drawLSystem(String path) {
       lSystPoints.clear();
        savedStates.clear();
        randAngle = Math.random() * 180;

        double x = 0, y = 0, dx = 0, dy = 0;

        switch (direction) {
            case "LEFT":
                x = screenWidth;
                y = screenHeight / 2;
                dx = -(screenWidth / Math.pow(10, iterations + 1));
                break;
            case "RIGHT":
                y = screenHeight / 2;
                dx = screenWidth / Math.pow(10, iterations + 1);
                break;
            case "UP":
                x = screenWidth / 2;
                y = screenHeight;
                dy = -(screenHeight / Math.pow(10, iterations + 1));
                break;
            case "DOWN":
                x = screenWidth / 2;
                dy = screenHeight / Math.pow(10, iterations + 1);
                break;
        }

        lSystPoints.add(new Pair<>(x, y));

        double rx, ry;
        for (int i = 0; i < path.length(); ++i) {
            switch (path.charAt(i)) {
                case 'F':
                    lSystPoints.add(new Pair<>(x, y));
                    x += dx;
                    y += dy;
                    break;
                case '+':
                    rx = dx;
                    ry = dy;
                    dx = rx * Math.cos(angle * Math.PI / 180) - ry * Math.sin(angle * Math.PI / 180);
                    dy = rx * Math.sin(angle * Math.PI / 180) + ry * Math.cos(angle * Math.PI / 180);
                    break;
                case '-':
                    rx = dx;
                    ry = dy;
                    dx = rx * Math.cos(-angle * Math.PI / 180) - ry * Math.sin(-angle * Math.PI / 180);
                    dy = rx * Math.sin(-angle * Math.PI / 180) + ry * Math.cos(-angle * Math.PI / 180);
                    break;
                case '[':
                    savedStates.push(new Pair<>(x, y));
                    break;
                case ']':
                    Pair<Double, Double> coords = savedStates.pop();
                    x = coords.first;
                    y = coords.second;
                    break;
            }
        }
        re =  Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        gree = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        wi = new Random().nextInt(11); // Генерация числа от 0 до 10 (включительно)

        angle = randAngle; // Используем случайный угол для начальной установки

        drawLSystPoints(screenWidth, screenHeight);
    }

    private void drawLSystPoints(double imageViewWidth, double imageViewHeight) {
        double xMax = 0, xMin = Double.MAX_VALUE, yMax = 0, yMin = Double.MAX_VALUE;
        for (Pair<Double, Double> point : lSystPoints) {
            xMax = Math.max(xMax, point.first);
            xMin = Math.min(xMin, point.first);
            yMax = Math.max(yMax, point.second);
            yMin = Math.min(yMin, point.second);
        }
        double scale = Math.max(xMax - xMin, yMax - yMin);

        Bitmap bitmap = Bitmap.createBitmap((int) imageViewWidth, (int) imageViewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        int strokeWidth = random.nextInt(15) + 3;
        paint.setStrokeWidth(strokeWidth);

        for (int i = 0; i < lSystPoints.size() - 1; ++i) {
            double x1 = (xMax - lSystPoints.get(i).first) / scale * imageViewWidth;
            double y1 = (yMax - lSystPoints.get(i).second) / scale * imageViewHeight;
            double x2 = (xMax - lSystPoints.get(i + 1).first) / scale * imageViewWidth;
            double y2 = (yMax - lSystPoints.get(i + 1).second) / scale * imageViewHeight;

            int color = Color.argb((int) re, (int) gree, Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)), 0);
            paint.setColor(color);
            canvas.drawLine((float) x1, (float) y1, (float) x2, (float) y2, paint);

            re -= 98 * 1.0 / lSystPoints.size();
            gree += 128 * 1.0 * 10 / lSystPoints.size();
            wi -= 8.0 / lSystPoints.size();
            if (re < 0) re = 0;
            if (gree > 128) gree = 128;
            angle = Math.random() * 180;
        }
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        ImageView imageView = findViewById(R.id.fractalImageView);
        imageView.setImageDrawable(drawable);  }


    String buildPath() {
        String prev = axiom;
        String next = axiom;
        int iter = 0;
        while (iter < iterations) {
            prev = next;
            next = "";
            for (int i = 0; i < prev.length(); ++i) {
                if (rules.containsKey(prev.charAt(i))) {
                    next += rules.get(prev.charAt(i));
                } else {
                    next += prev.charAt(i);
                }
            }
            ++iter;
        }
        return next;
    }

    private void generateAbstraction() {
        iterations = new Random().nextInt(3) + 2;
        path = buildPath();
        drawLSystem(path);
        int[] gradientColors = getRandomGradientColors();
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors);
        container.setBackground(gradientDrawable);
        container.invalidate(); // инвалидация контейнера для обновления вида
    }
    private int[] getRandomGradientColors() {
        int numColors = random.nextInt(3) + 2;
        int[] colors = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            colors[i] = getRandomColor();
        }

        return colors;
    }

    private int getRandomColor() {
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}



