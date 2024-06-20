package com.example.diplom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyrspvaya.R;

import yuku.ambilwarna.AmbilWarnaDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class FingerPainting extends Activity {

   public enum ShapeType {
      CIRCLE,
      SQUARE,
      TRIANGLE,
      STRAIGHT_LINE,
      CURVED_LINE

   }

   private FrameLayout container;

   private int screenWidth;
   private int screenHeight;
   private RectF figureBounds;

   private Bitmap bitmap;
   private MyCanvas myCanvas;
   private Paint paint;
   private float downX, downY, upX, upY;
   private int currentBrushColor = Color.BLACK;
   private TextView messageTextView;
   private int currentBrushSize = 10;
   int[] colorsArray;

   boolean gradient;
   Random random;

   private ShapeType currentShape;
   private RelativeLayout fingerPaint;

   private ImageButton backButton;
   private ImageButton colorButton;
   private ImageButton trashButton;
   private ImageButton sizeButton;
   private ImageButton downloadButton;
   private int originalBrushSize;

   private Context context;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.finger_painting);
      fingerPaint = (RelativeLayout) findViewById(R.id.panel_painting);
      container = findViewById(R.id.container);
      context = this;
      messageTextView = findViewById(R.id.messageTextView);
      random = new Random();
      figureBounds = new RectF();
      gradient = false;
      paint = new Paint();
      paint.setColor(Color.BLACK);
      paint.setStrokeWidth(5);
      currentShape = ShapeType.CURVED_LINE;
      messageTextView.setVisibility(View.VISIBLE);


      Spinner shapeSpinner = findViewById(R.id.spinner_shape_selection);
      shapeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String message = "";
            switch (position) {
               case 0:
                  currentShape = ShapeType.CIRCLE;
                  message = "Проведите пальцем для определения радиуса круга.";
                  break;
               case 1:
                  currentShape = ShapeType.SQUARE;
                  message = "Проведите диагональ для определения начала и конца фигуры.";
                  break;
               case 2:
                  currentShape = ShapeType.TRIANGLE;
                  message = "Поставьте 3 точки.";
                  break;
               case 3:
                  currentShape = ShapeType.STRAIGHT_LINE;
                  message = "Проведите пальцем по экрану.\n" +
                          " Линия отобразится, когда отпустите палец.";
                  break;
               case 4:
                  currentShape = ShapeType.CURVED_LINE;
                  message = "Проведите пальцем по экрану.";
                  break;
            }
            if (!message.isEmpty()) {
               showToast(message);
            }
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent) {
            // Если ничего не выбрано, не делаем ничего
         }

         private void showToast(String message) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
         }
      });

      backButton = findViewById(R.id.btnBack);
      backButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });

      trashButton = findViewById(R.id.trash_button);

      trashButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            myCanvas.clearCanvas(v);
         }
      });


      colorButton = findViewById(R.id.color_button);
      colorButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            final String[] colors = {"Изменить цвет фона", "Изменить цвет кисти"};

            AlertDialog.Builder builder = new AlertDialog.Builder(FingerPainting.this);
            builder.setTitle("Выберите действие");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                  // Вызываем соответствующий метод в зависимости от выбранного пункта
                  switch (which) {
                     case 0:
                        // Изменить цвет фона
                        gradientColorBackground();
                        break;
                     case 1:
                        // Изменить цвет кисти
                        gradientColorBraches();
                        break;
                  }
               }
            });

            // Отображаем диалоговое окно
            builder.show();
         }
      });
      /*sizeButton = findViewById(R.id.buttonBrushSize);
      sizeButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            showBrushSizeDialog();
         }
      });*/

      downloadButton = findViewById(R.id.btnDownload);
      downloadButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            GeneralMethods.downloadImage(container.getContext(), container);
         }
      });



      screenWidth = getResources().getDisplayMetrics().widthPixels;
      screenHeight = getResources().getDisplayMetrics().heightPixels;

      myCanvas = new MyCanvas(findViewById(R.id.paintImageView));
      RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
              RelativeLayout.LayoutParams.MATCH_PARENT,
              RelativeLayout.LayoutParams.MATCH_PARENT);
      layoutParams.addRule(RelativeLayout.BELOW, R.id.spinner_shape_selection);
      ((RelativeLayout) findViewById(R.id.panel_painting)).addView(myCanvas, layoutParams);


   }

   private void showBrushSizeDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      View dialogView = getLayoutInflater().inflate(R.layout.brush_size_dialog, null);
      builder.setView(dialogView);

      SeekBar seekBarBrushSize = dialogView.findViewById(R.id.seekBarBrushSize);
      final TextView textViewBrushSize = dialogView.findViewById(R.id.textViewBrushSize);

      seekBarBrushSize.setProgress(currentBrushSize);
      textViewBrushSize.setText("Толщина кисти: " + currentBrushSize);

      seekBarBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            currentBrushSize = progress;
            textViewBrushSize.setText("Толщина кисти: " + progress);
         }

         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {}

         @Override
         public void onStopTrackingTouch(SeekBar seekBar) {}
      });

      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
         }
      });
      builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
         }
      });

      builder.show();
   }





   private void openBackgroundColorPicker() {
      AmbilWarnaDialog colordialog = new AmbilWarnaDialog(this,
              Color.BLUE, new AmbilWarnaDialog.OnAmbilWarnaListener() {
         @Override
         public void onCancel(AmbilWarnaDialog dialog) {

         }

         @Override
         public void onOk(AmbilWarnaDialog dialog, int color) {
            fingerPaint.setBackgroundColor(color);

         }
      });
      colordialog.show();
   }

   //Однотонная кисть
   private void openBrushColorPicker() {
      paint.setShader(null);
      AmbilWarnaDialog colordialog = new AmbilWarnaDialog(this,
              currentBrushColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
         @Override
         public void onCancel(AmbilWarnaDialog dialog) {
         }

         @Override
         public void onOk(AmbilWarnaDialog dialog, int color) {
            currentBrushColor = color;
            paint.setColor(color);
         }
      });
      colordialog.show();
   }

   //выбор режима фона
   private void gradientColorBackground() {
      final String[] colors = {"Однотонный", "Градиентный"};

      AlertDialog.Builder builder = new AlertDialog.Builder(FingerPainting.this);
      builder.setTitle("Выберите действие");
      builder.setItems(colors, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            switch (which) {
               case 0:
                  openBackgroundColorPicker();
                  break;
               case 1:
                  openGradientBackgroundDrawableDialog();
                  break;

            }
         }
      });

      builder.show();

   }

   //выюор режима кисти
   private void gradientColorBraches() {
      final String[] colors = {"Однотонный", "Градиентный"};
      AlertDialog.Builder builder = new AlertDialog.Builder(FingerPainting.this);
      builder.setTitle("Выберите действие");
      builder.setItems(colors, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            switch (which) {
               case 0:
                  openBrushColorPicker();
                  gradient = false;

                  break;
               case 1:
                  openGradientBrushDialog();
                  gradient = true;
                  break;

            }
         }
      });
      builder.show();

   }

   // Метод для открытия диалога настройки градиента
   private void openGradientBrushDialog() {

      AlertDialog.Builder builder = new AlertDialog.Builder(FingerPainting.this);
      builder.setTitle("Градиентный режим");
      List<Integer> colors = new ArrayList<>();
      View gradientDialogView = getLayoutInflater().inflate(R.layout.gradient_dialog_layout, null);
      LinearLayout colorIconsLayout = gradientDialogView.findViewById(R.id.colorIconsLayout);
      Button addColorButton = gradientDialogView.findViewById(R.id.addColorButton);
      Button makeGradientButton = gradientDialogView.findViewById(R.id.makeGradientButton);
      addColorButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            AmbilWarnaDialog colordialog = new AmbilWarnaDialog(FingerPainting.this,
                    Color.BLUE, new AmbilWarnaDialog.OnAmbilWarnaListener() {
               @Override
               public void onCancel(AmbilWarnaDialog dialog) {

               }

               @Override
               public void onOk(AmbilWarnaDialog dialog, int color) {
                  colors.add(color);
                  addColorIcon(color, colorIconsLayout);

               }
            });
            colordialog.show();

         }
      });

      makeGradientButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (colors.size() < 2) {
               Toast.makeText(FingerPainting.this, "Выберите 2 или более цвета.", Toast.LENGTH_SHORT).show();
            } else {
               colorsArray = new int[colors.size()];
               for (int i = 0; i < colors.size(); i++) {
                  colorsArray[i] = colors.get(i);
               }


                Shader gradientShader = createGradientShader(colorsArray, figureBounds);

               paint.setShader(gradientShader);

            }
         }
      });

      builder.setView(gradientDialogView);

      builder.show();
   }
   private Shader createGradientShader(int[] colors, RectF bounds) {
      int randomGradientType = getRandomGradientType();

      switch (randomGradientType) {

         case 0:
            return new LinearGradient(bounds.left, bounds.top, bounds.left, bounds.bottom, colors, null, Shader.TileMode.CLAMP);
         case 1:
            return new LinearGradient(bounds.left, bounds.bottom, bounds.left, bounds.top, colors, null, Shader.TileMode.CLAMP);
         case 2:
            return new LinearGradient(bounds.left, bounds.top, bounds.right, bounds.top, colors, null, Shader.TileMode.CLAMP);
         default:
            return new LinearGradient(bounds.right, bounds.top, bounds.left, bounds.top, colors, null, Shader.TileMode.CLAMP);
         }
   }

   private int getRandomGradientType() {
      return new Random().nextInt(5);
   }

   private void openGradientBackgroundDrawableDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(FingerPainting.this);
      builder.setTitle("Градиентный режим");
      List<Integer> colors = new ArrayList<>();
      View gradientDialogView = getLayoutInflater().inflate(R.layout.gradient_dialog_layout, null);
      LinearLayout colorIconsLayout = gradientDialogView.findViewById(R.id.colorIconsLayout);
      Button addColorButton = gradientDialogView.findViewById(R.id.addColorButton);
      Button makeGradientButton = gradientDialogView.findViewById(R.id.makeGradientButton);
      addColorButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            AmbilWarnaDialog colordialog = new AmbilWarnaDialog(FingerPainting.this,
                    Color.BLUE, new AmbilWarnaDialog.OnAmbilWarnaListener() {
               @Override
               public void onCancel(AmbilWarnaDialog dialog) {}
               @Override
               public void onOk(AmbilWarnaDialog dialog, int color) {
                  colors.add(color);
                  addColorIcon(color, colorIconsLayout);
               }
            });
            colordialog.show();
         }
      });
      makeGradientButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (colors.size() < 2) {
               Toast.makeText(FingerPainting.this, "Выберите 2 или более цвета.", Toast.LENGTH_SHORT).show();
            } else {
               int[] colorsArray = new int[colors.size()];
               for (int i = 0; i < colors.size(); i++) {
                  colorsArray[i] = colors.get(i);
               }
               Drawable backgroundDrawable = createGradient(colors);
               fingerPaint.setBackground(backgroundDrawable);
            }
         }
      });
      builder.setView(gradientDialogView);
      builder.show();
   }
   private void addColorIcon(int color, LinearLayout colorIconsLayout) {
      ImageView colorIcon = new ImageView(this);
      colorIcon.setBackgroundColor(color);
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
              getResources().getDimensionPixelSize(R.dimen.color_icon_size),
              getResources().getDimensionPixelSize(R.dimen.color_icon_size));
      layoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.color_icon_margin));
      colorIcon.setLayoutParams(layoutParams);
      colorIconsLayout.addView(colorIcon);
   }
   private Drawable createGradient(List<Integer> colors) {

      int[] colorsArray = new int[colors.size()];
      for (int i = 0; i < colors.size(); i++) {
         colorsArray[i] = colors.get(i);
      }

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

      gradientDrawable.setColors(colorsArray);
      gradientDrawable.setShape(GradientDrawable.RECTANGLE);
      gradientDrawable.setCornerRadius(10f);
      return gradientDrawable;

   }


   public class GradientTriangle {
      private List<PointF> points;
      private int[] colors;
      private int color;

      public GradientTriangle(List<PointF> points, int color) {
         this.points = points;
         this.color = color;
      }

      public GradientTriangle(List<PointF> points, int[] colors) {
         this.points = points;
         this.colors = colors;
      }

      public List<PointF> getPoints() {
         return points;
      }

      public int[] getColors() {
         return colors;
      }

      public int getColor() {
         return color;
      }

   }




   private class MyCanvas extends View {

      private Canvas canvas;

      private PointF firstPoint = null;
      private PointF secondPoint = null;
      private PointF thirdPoint = null;


      private ImageView imageView;

      public MyCanvas(ImageView imageView) {
         super(imageView.getContext());
         this.imageView = imageView;
         imageView.setDrawingCacheEnabled(true);
      }

      public void clearCanvas(View view) {
         bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
         canvas = new Canvas(bitmap);
         canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
         drawnTriangleVertices.clear();
         fingerPaint.setBackgroundColor(Color.WHITE);
         invalidate();

      }

      private List<GradientTriangle> drawnTriangleVertices = new ArrayList<>();


      @Override
      protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);

         if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
         }

         paint.setColor(Color.BLACK);
         paint.setStyle(Paint.Style.FILL);

         // Отображаем уже нарисованные треугольники
         drawDrawnTriangles(canvas);

      }


      @Override
      public boolean onTouchEvent(MotionEvent event) {
         float x = event.getX();
         float y = event.getY();
         if (!isPointInsideView(x, y, imageView)) {
            return false;
         }
         paint.setColor(currentBrushColor);
         switch (currentShape) {
            case CIRCLE:
               onTouchEventCircle(event);
               break;
            case SQUARE:
               onTouchEventQuare(event);
               break;
            case TRIANGLE:
               onTouchEventTriangles(event);
               break;
            case STRAIGHT_LINE:
               onTouchEventStraightLine(event);
               break;
            case CURVED_LINE:
               onTouchEventCurvedLine(event);
               break;
         }
         return true;
      }

      private boolean isPointInsideView(float x, float y, View view) {
         int[] location = new int[2];
         view.getLocationOnScreen(location);
         int viewX = location[0];
         int viewY = location[1];
         int viewWidth = view.getWidth();
         int viewHeight = view.getHeight();
         return (x >= viewX && x <= (viewX + viewWidth)) && (y >= viewY && y <= (viewY + viewHeight));
      }

      public void onTouchEventCircle(MotionEvent event) {
         int action = event.getActionMasked();

         switch (action) {
            case MotionEvent.ACTION_DOWN:
               downX = event.getX();
               downY = event.getY();
               break;

            case MotionEvent.ACTION_MOVE:
               break;

            case MotionEvent.ACTION_UP:
               upX = event.getX();
               upY = event.getY();
               drawCircle(downX, downY, upX, upY);
               break;
         }
      }

      private void drawCircle(float centerX, float centerY, float touchX, float touchY) {
         if (canvas != null) {
            float radius = (float) Math.sqrt(Math.pow(touchX - centerX, 2) + Math.pow(touchY - centerY, 2));
            float left = centerX - radius;
            float top = centerY - radius;
            float right = centerX + radius;
            float bottom = centerY + radius;
            paint.setStrokeWidth(currentBrushSize);
            if (gradient && colorsArray != null && colorsArray.length > 1) {
               figureBounds = new RectF(left, top, right, bottom);
               Shader gradientShader = createGradientShader(colorsArray, figureBounds);
               paint.setShader(gradientShader);
            } else {
               paint.setColor(currentBrushColor);
            }

            canvas.drawCircle(centerX, centerY, radius, paint);
            invalidate();
         }
      }


      public void onTouchEventQuare(MotionEvent event) {
         int action = event.getActionMasked();

         switch (action) {
            case MotionEvent.ACTION_DOWN:
               downX = event.getX();
               downY = event.getY();
               break;

            case MotionEvent.ACTION_MOVE:
               float moveX = event.getX();
               float moveY = event.getY();
               drawSquare(downX, downY, moveX, moveY);
               break;

            case MotionEvent.ACTION_UP:
               upX = event.getX();
               upY = event.getY();
               drawSquare(downX, downY, upX, upY);
               break;
         }
      }

      private void drawSquare(float startX, float startY, float endX, float endY) {
         if (canvas != null) {
            float left = Math.min(startX, endX);
            float top = Math.min(startY, endY);
            float right = Math.max(startX, endX);
            float bottom = Math.max(startY, endY);
            paint.setStrokeWidth(currentBrushSize);
            if (gradient && colorsArray != null && colorsArray.length > 1) {
               figureBounds = new RectF(left, top, right, bottom);
               Shader gradientShader = createGradientShader(colorsArray, figureBounds);
               paint.setShader(gradientShader);
            } else {
               paint.setColor(currentBrushColor);
            }
            canvas.drawRect(left, top, right, bottom, paint);
            invalidate();


         }
      }

      public void onTouchEventTriangles(MotionEvent event) {
         int action = event.getActionMasked();

         switch (action) {
            case MotionEvent.ACTION_DOWN:
               break;

            case MotionEvent.ACTION_MOVE:
               break;

            case MotionEvent.ACTION_UP:
               if (firstPoint == null) {
                  firstPoint = new PointF(event.getX(), event.getY());
               } else if (secondPoint == null) {
                  secondPoint = new PointF(event.getX(), event.getY());
               } else if (thirdPoint == null) {
                  thirdPoint = new PointF(event.getX(), event.getY());
               }

               // Если все три точки установлены, нарисуйте треугольник и сбросьте координаты точек
               if (firstPoint != null && secondPoint != null && thirdPoint != null) {
                  List<PointF> trianglePoints = new ArrayList<>();
                  trianglePoints.add(firstPoint);
                  trianglePoints.add(secondPoint);
                  trianglePoints.add(thirdPoint);

                  if (gradient && colorsArray != null && colorsArray.length >= 2) {
                     GradientTriangle triangle = new GradientTriangle(trianglePoints, colorsArray);
                     drawnTriangleVertices.add(triangle);
                  } else {
                     GradientTriangle triangle = new GradientTriangle(trianglePoints, currentBrushColor);
                     drawnTriangleVertices.add(triangle);
                  }

                  firstPoint = null;
                  secondPoint = null;
                  thirdPoint = null;
                  invalidate();
               }
               break;
         }
      }

      private void drawDrawnTriangles(Canvas canvas) {
         for (GradientTriangle triangle : drawnTriangleVertices) {
            List<PointF> trianglePoints = triangle.getPoints();

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);

            Path path = new Path();
            path.moveTo(trianglePoints.get(0).x, trianglePoints.get(0).y);
            path.lineTo(trianglePoints.get(1).x, trianglePoints.get(1).y);
            path.lineTo(trianglePoints.get(2).x, trianglePoints.get(2).y);
            path.lineTo(trianglePoints.get(0).x, trianglePoints.get(0).y);

            paint.setStrokeWidth(currentBrushSize);

            if (gradient && triangle.getColors() != null && triangle.getColors().length >= 2) {
               int[] colors = triangle.getColors();
               Shader gradientShader = new LinearGradient(trianglePoints.get(0).x, trianglePoints.get(0).y,
                       trianglePoints.get(2).x, trianglePoints.get(2).y,
                       colors, null, Shader.TileMode.CLAMP);
               paint.setShader(gradientShader);
            } else {
               // Очистить шейдер, чтобы убедиться, что используется только однотонный цвет
               paint.setShader(null);
               int color = triangle.getColor();
               paint.setColor(color);
            }


            canvas.drawPath(path, paint);
         }
      }


      public void onTouchEventStraightLine(MotionEvent event) {
         int action = event.getActionMasked();

         switch (action) {
            case MotionEvent.ACTION_DOWN:
               downX = event.getX();
               downY = event.getY();
               break;

            case MotionEvent.ACTION_MOVE:
               break;

            case MotionEvent.ACTION_UP:
               upX = event.getX();
               upY = event.getY();
               drawStraightLine(downX, downY, upX, upY);

               break;
         }
      }

      private void drawStraightLine(float startX, float startY, float endX, float endY) {
         if (canvas != null) {
            paint.setStrokeWidth(currentBrushSize);
            if (gradient && colorsArray != null && colorsArray.length > 1) {
               Shader gradientShader = new LinearGradient(startX, startY, endX, endY, colorsArray, null, Shader.TileMode.CLAMP);
               paint.setShader(gradientShader);
            } else {
               paint.setColor(currentBrushColor);

            }
            canvas.drawLine(startX, startY, endX, endY, paint);
            invalidate();
         }
      }


      public void onTouchEventCurvedLine(MotionEvent event) {
         int action = event.getActionMasked();

         switch (action) {
            case MotionEvent.ACTION_DOWN:
               downX = event.getX();
               downY = event.getY();
               break;

            case MotionEvent.ACTION_MOVE:
               float moveX = event.getX();
               float moveY = event.getY();
               paint.setStrokeWidth(currentBrushSize);
               if (gradient) {
                  Shader gradientShader = new LinearGradient(downX, downY, moveX, moveY, colorsArray, null, Shader.TileMode.CLAMP);

                  paint.setShader(gradientShader);
               } else {
                  paint.setColor(currentBrushColor);
               }

               if (canvas != null) {
                  canvas.drawLine(downX, downY, moveX, moveY, paint);
                  downX = moveX;
                  downY = moveY;
                  invalidate();
               }
               break;

            case MotionEvent.ACTION_UP:
               break;
         }
      }

   }
}

