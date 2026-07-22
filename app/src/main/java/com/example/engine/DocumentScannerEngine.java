package com.example.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class DocumentScannerEngine {

    public enum FilterType {
        ORIGINAL,
        MAGIC_COLOR,
        GRAYSCALE,
        HIGH_CONTRAST_BW
    }

    public static Bitmap applyFilter(Bitmap source, FilterType filterType) {
        if (source == null) return null;
        if (filterType == FilterType.ORIGINAL) return source;

        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ColorMatrix cm = new ColorMatrix();

        switch (filterType) {
            case GRAYSCALE:
                cm.setSaturation(0);
                break;
            case MAGIC_COLOR:
                // Boost contrast and saturation for clear document reading
                float contrast = 1.3f;
                float brightness = 10f;
                float[] matrix = new float[]{
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                };
                cm.set(matrix);
                break;
            case HIGH_CONTRAST_BW:
                // Stark black and white document filter
                cm.set(new float[]{
                        85f, 85f, 85f, 0, -128f * 255f,
                        85f, 85f, 85f, 0, -128f * 255f,
                        85f, 85f, 85f, 0, -128f * 255f,
                        0, 0, 0, 1, 0
                });
                break;
        }

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(source, 0, 0, paint);
        return result;
    }
}
