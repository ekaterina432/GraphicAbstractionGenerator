package com.example.diplom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class GeneralMethods extends AppCompatActivity {
    static void downloadImage(Context context, FrameLayout container) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "generated_image_" + timeStamp + ".png";
        Bitmap generatedImage = generateImage(container);
        String picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File imageFile = new File(picturesDirectory, fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            generatedImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()}, null, null);
            Toast.makeText(context, "Изображение успешно скачано", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка при скачивании изображения", Toast.LENGTH_SHORT).show();
        }
    }


    public static Bitmap generateImage(FrameLayout container) {
        int width = container.getWidth();
        int height = container.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        container.draw(canvas);

        return bitmap;
    }

    static int[] getRandomGradientColors(Random random) {
        int numColors = random.nextInt(3) + 2;
        int[] colors = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            colors[i] = getRandomColor(random);
        }

        return colors;
    }

    static int getRandomColor(Random random) {
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

}
