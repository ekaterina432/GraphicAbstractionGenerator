package com.example.kyrspvaya;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Generator extends AppCompatActivity {
    private FrameLayout container;
    private ImageButton backButton;

    private Button generateButton;
    private ImageButton downloadButton;

    private Random random;


    private int screenWidth;
    private int screenHeight;

    private static final int MIN_SIZE = 200;
    private static final int MAX_SIZE = 1300;
    private static final int MIN_SHAPES = 7;
    private static final int MAX_SHAPES = 13;
    private static final int MIN_OFFSET = -600;
    private static final int MAX_OFFSET = 800;
    public void goBackToMain(View view) {
        finish();
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generat);

        container = findViewById(R.id.container);
        backButton = findViewById(R.id.btnBack);
        generateButton = findViewById(R.id.btnGenerate);
        downloadButton = findViewById(R.id.btnDownload);
        random = new Random();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToMain(v);
            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAbstraction();
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage();
            }
        });

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        generateAbstraction();
    }

    private void generateAbstraction() {
        container.removeAllViews();

        int numShapes = random.nextInt(MAX_SHAPES - MIN_SHAPES + 1) + MIN_SHAPES;

        Drawable backgroundDrawable = getRandomGradientDrawable();

        container.setBackground(backgroundDrawable);

        for (int i = 0; i < numShapes; i++) {
            View shape = createRandomShape();
            container.addView(shape);
        }
    }




    private Drawable getRandomGradientDrawable() {
        int[] colors = {getRandomColor(), getRandomColor(), getRandomColor()};
        int gradientOrientation = random.nextInt(4);
        GradientDrawable gradientDrawable = new GradientDrawable();

        switch (gradientOrientation) {
            case 0:
                gradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                break;
            case 1:
                gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                break;
            case 2:
                gradientDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
                break;
            case 3:
                gradientDrawable.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
                break;
        }

        gradientDrawable.setColors(colors);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(10f);

        return gradientDrawable;
    }


    private int getRandomColor() {
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }


    private View createRandomShape() {
        int shapeType = random.nextInt(3);
        View shape = new View(this);

        int shapeSize = getRandomSize();
        int shapeX = random.nextInt(screenWidth - shapeSize);
        int shapeY = random.nextInt(screenHeight - shapeSize);

        int offsetX = random.nextInt(MAX_OFFSET - MIN_OFFSET + 1) + MIN_OFFSET;
        int offsetY = random.nextInt(MAX_OFFSET - MIN_OFFSET + 1) + MIN_OFFSET;

        if (random.nextBoolean()) {
            offsetX = -offsetX;
        }
        if (random.nextBoolean()) {
            offsetY = -offsetY;
        }

        shapeX += offsetX;
        shapeY += offsetY;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(shapeSize, shapeSize);
        layoutParams.leftMargin = shapeX;
        layoutParams.topMargin = shapeY;
        shape.setLayoutParams(layoutParams);

        int[] gradientColors = getRandomGradientColors();

        switch (shapeType) {
            case 0:
                shape.setBackground(new SquareDrawable(gradientColors, getRandomRotation(), shapeSize));
                break;
            case 1:
                shape.setBackground(new CircleDrawable(gradientColors, getRandomRotation(), shapeSize));
                break;
            case 2:
                shape.setBackground(new TriangleDrawable(gradientColors, getRandomRotation(), shapeSize));
                break;
        }

        return shape;
    }


    private int[] getRandomGradientColors() {
        int numColors = random.nextInt(3) + 2;
        int[] colors = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            colors[i] = getRandomColor();
        }

        return colors;
    }

    private int getRandomSize() {
        return random.nextInt(MAX_SIZE - MIN_SIZE + 1) + MIN_SIZE;
    }

    private float getRandomRotation() {
        return random.nextFloat() * 360f;
    }

    class SquareDrawable extends ShapeDrawable {
        private int[] colors;
        private float rotation;
        private int size;

        public SquareDrawable(int[] colors, float rotation, int size) {
            super(colors);
            this.colors = colors;
            this.rotation = rotation;
            this.size = size;
        }

        @Override
        public void drawShape(Canvas canvas, Paint paint, Path path, int width, int height, int color) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;

            paint.setColor(color);
            canvas.save();
            canvas.rotate(rotation, halfWidth, halfHeight);

            path.reset();
            path.moveTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.close();

            canvas.drawPath(path, paint);
            canvas.restore();
        }


        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    class CircleDrawable extends ShapeDrawable {
        private float rotation;
        private int size;

        public CircleDrawable(int[] colors, float rotation, int size) {
            super(colors);
            this.rotation = rotation;
            this.size = size;
        }

        @Override
        public void drawShape(Canvas canvas, Paint paint, Path path, int width, int height, int color) {
            int radius = size / 2;
            int centerX = width / 2;
            int centerY = height / 2;

            paint.setColor(color);
            canvas.save();
            canvas.rotate(rotation, centerX, centerY);

            path.reset();
            path.addCircle(centerX, centerY, radius, Path.Direction.CW);

            canvas.drawPath(path, paint);
            canvas.restore();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    class TriangleDrawable extends ShapeDrawable {
        private float rotation;
        private int size;

        public TriangleDrawable(int[] colors, float rotation, int size) {
            super(colors);
            this.rotation = rotation;
            this.size = size;
        }

        @Override
        public void drawShape(Canvas canvas, Paint paint, Path path, int width, int height, int color) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;

            path.reset();
            path.moveTo(halfWidth, 0); // Top point of the triangle
            path.lineTo(0, height); // Left point of the triangle
            path.lineTo(width, height); // Right point of the triangle
            path.close();

            paint.setColor(color);
            canvas.save();
            canvas.rotate(rotation, halfWidth, halfHeight);

            canvas.drawPath(path, paint);
            canvas.restore();
        }
    }


    abstract class ShapeDrawable extends Drawable {
        protected Paint paint;
        protected Path path;
        private int[] gradientColors;

        public ShapeDrawable(int[] colors) {
            paint = new Paint();
            path = new Path();
            gradientColors = colors;
        }

        public abstract void drawShape(Canvas canvas, Paint paint, Path path, int width, int height, int color);

        @Override
        public void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();

            // Создание градиентного шейдера
            Shader shader = new LinearGradient(0, 0, width, height, gradientColors, null, Shader.TileMode.CLAMP);
            paint.setShader(shader);

            drawShape(canvas, paint, path, width, height, getRandomColor());
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }


}