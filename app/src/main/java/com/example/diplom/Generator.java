package com.example.diplom;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kyrspvaya.R;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
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
                finish();
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
                GeneralMethods.downloadImage(container.getContext(), container);
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
        int[] colors = {GeneralMethods.getRandomColor(random), GeneralMethods.getRandomColor(random), GeneralMethods.getRandomColor(random)};
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

        int[] gradientColors = GeneralMethods.getRandomGradientColors(random);

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
            path.moveTo(halfWidth, 0);
            path.lineTo(0, height);
            path.lineTo(width, height);
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

            Shader shader = new LinearGradient(0, 0, width, height, gradientColors, null, Shader.TileMode.CLAMP);
            paint.setShader(shader);

            drawShape(canvas, paint, path, width, height, GeneralMethods.getRandomColor(random));
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