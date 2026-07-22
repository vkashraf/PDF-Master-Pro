package com.example.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfEngine {

    private static final String TAG = "PdfEngine";

    /**
     * Get total page count of a PDF file
     */
    public static int getPageCount(File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) return 0;
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {
            return renderer.getPageCount();
        } catch (Exception e) {
            Log.e(TAG, "Error getting page count", e);
            return 0;
        }
    }

    /**
     * Render a single page of PDF into a Bitmap
     */
    public static Bitmap renderPage(File pdfFile, int pageIndex, int renderWidth) {
        if (pdfFile == null || !pdfFile.exists()) return null;
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {

            if (pageIndex < 0 || pageIndex >= renderer.getPageCount()) return null;

            try (PdfRenderer.Page page = renderer.openPage(pageIndex)) {
                int width = page.getWidth();
                int height = page.getHeight();

                if (renderWidth <= 0) renderWidth = width;
                int renderHeight = (int) (((float) renderWidth / width) * height);

                Bitmap bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888);
                // Fill background with white before rendering
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                return bitmap;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rendering page: " + pageIndex, e);
            return null;
        }
    }

    /**
     * Convert Image list to PDF
     */
    public static boolean convertImagesToPdf(List<String> imagePaths, File outputFile, int margin) {
        if (imagePaths == null || imagePaths.isEmpty() || outputFile == null) return false;

        PdfDocument document = new PdfDocument();
        try {
            for (int i = 0; i < imagePaths.size(); i++) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePaths.get(i));
                if (bitmap == null) continue;

                int pageWidth = bitmap.getWidth() + (margin * 2);
                int pageHeight = bitmap.getHeight() + (margin * 2);

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);

                Canvas canvas = page.getCanvas();
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(bitmap, margin, margin, null);

                document.finishPage(page);
                bitmap.recycle();
            }

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.writeTo(fos);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting images to PDF", e);
            return false;
        } finally {
            document.close();
        }
    }

    /**
     * Merge multiple PDF files into one
     */
    public static boolean mergePdfs(List<File> pdfFiles, File outputFile) {
        if (pdfFiles == null || pdfFiles.isEmpty() || outputFile == null) return false;

        PdfDocument document = new PdfDocument();
        int globalPageNumber = 1;

        try {
            for (File pdfFile : pdfFiles) {
                if (!pdfFile.exists()) continue;

                try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
                     PdfRenderer renderer = new PdfRenderer(pfd)) {

                    int pageCount = renderer.getPageCount();
                    for (int i = 0; i < pageCount; i++) {
                        try (PdfRenderer.Page sourcePage = renderer.openPage(i)) {
                            int width = sourcePage.getWidth();
                            int height = sourcePage.getHeight();

                            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bitmap);
                            canvas.drawColor(Color.WHITE);
                            sourcePage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, globalPageNumber++).create();
                            PdfDocument.Page newPage = document.startPage(pageInfo);

                            Canvas pageCanvas = newPage.getCanvas();
                            pageCanvas.drawBitmap(bitmap, 0, 0, null);

                            document.finishPage(newPage);
                            bitmap.recycle();
                        }
                    }
                }
            }

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.writeTo(fos);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error merging PDFs", e);
            return false;
        } finally {
            document.close();
        }
    }

    /**
     * Extract specific pages from source PDF into a new PDF
     */
    public static boolean extractPages(File sourcePdf, File outputFile, List<Integer> pageIndices) {
        if (sourcePdf == null || !sourcePdf.exists() || pageIndices == null || pageIndices.isEmpty()) return false;

        PdfDocument document = new PdfDocument();
        int newPageNum = 1;

        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(sourcePdf, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {

            for (Integer index : pageIndices) {
                if (index < 0 || index >= renderer.getPageCount()) continue;

                try (PdfRenderer.Page page = renderer.openPage(index)) {
                    int w = page.getWidth();
                    int h = page.getHeight();

                    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(w, h, newPageNum++).create();
                    PdfDocument.Page pdfPage = document.startPage(pageInfo);
                    pdfPage.getCanvas().drawBitmap(bitmap, 0, 0, null);

                    document.finishPage(pdfPage);
                    bitmap.recycle();
                }
            }

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.writeTo(fos);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting pages", e);
            return false;
        } finally {
            document.close();
        }
    }

    /**
     * Compress PDF by adjusting resolution
     */
    public static boolean compressPdf(File sourcePdf, File outputFile, float scaleFactor) {
        if (sourcePdf == null || !sourcePdf.exists() || scaleFactor <= 0) return false;

        PdfDocument document = new PdfDocument();

        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(sourcePdf, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {

            int count = renderer.getPageCount();
            for (int i = 0; i < count; i++) {
                try (PdfRenderer.Page page = renderer.openPage(i)) {
                    int origW = page.getWidth();
                    int origH = page.getHeight();

                    int renderW = Math.max(100, (int) (origW * scaleFactor));
                    int renderH = Math.max(100, (int) (origH * scaleFactor));

                    Bitmap bitmap = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(origW, origH, i + 1).create();
                    PdfDocument.Page pdfPage = document.startPage(pageInfo);

                    Bitmap scaledBack = Bitmap.createScaledBitmap(bitmap, origW, origH, true);
                    pdfPage.getCanvas().drawBitmap(scaledBack, 0, 0, null);

                    document.finishPage(pdfPage);
                    bitmap.recycle();
                    scaledBack.recycle();
                }
            }

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.writeTo(fos);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error compressing PDF", e);
            return false;
        } finally {
            document.close();
        }
    }

    /**
     * Rotate PDF pages
     */
    public static boolean rotatePdf(File sourcePdf, File outputFile, int degrees) {
        if (sourcePdf == null || !sourcePdf.exists()) return false;

        PdfDocument document = new PdfDocument();

        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(sourcePdf, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {

            int count = renderer.getPageCount();
            for (int i = 0; i < count; i++) {
                try (PdfRenderer.Page page = renderer.openPage(i)) {
                    int w = page.getWidth();
                    int h = page.getHeight();

                    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    Matrix matrix = new Matrix();
                    matrix.postRotate(degrees);

                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), i + 1).create();
                    PdfDocument.Page pdfPage = document.startPage(pageInfo);
                    pdfPage.getCanvas().drawBitmap(rotatedBitmap, 0, 0, null);

                    document.finishPage(pdfPage);
                    bitmap.recycle();
                    rotatedBitmap.recycle();
                }
            }

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.writeTo(fos);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rotating PDF", e);
            return false;
        } finally {
            document.close();
        }
    }

    /**
     * Add Watermark text to PDF (default color and alpha)
     */
    public static boolean addWatermark(File sourcePdf, File outputFile, String watermarkText, float angle) {
        return addWatermark(sourcePdf, outputFile, watermarkText, Color.GRAY, 128, angle);
    }

    /**
     * Add Watermark text to PDF
     */
    public static boolean addWatermark(File sourcePdf, File outputFile, String watermarkText, int textColor, int alpha, float angle) {
        if (sourcePdf == null || !sourcePdf.exists() || watermarkText == null) return false;

        PdfDocument document = new PdfDocument();

        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(sourcePdf, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {

            int count = renderer.getPageCount();
            for (int i = 0; i < count; i++) {
                try (PdfRenderer.Page page = renderer.openPage(i)) {
                    int w = page.getWidth();
                    int h = page.getHeight();

                    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    // Draw watermark
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setColor(textColor);
                    paint.setAlpha(alpha);
                    paint.setTextSize(w / 12f);
                    paint.setTextAlign(Paint.Align.CENTER);

                    canvas.save();
                    canvas.rotate(angle, w / 2f, h / 2f);
                    canvas.drawText(watermarkText, w / 2f, h / 2f, paint);
                    canvas.restore();

                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(w, h, i + 1).create();
                    PdfDocument.Page pdfPage = document.startPage(pageInfo);
                    pdfPage.getCanvas().drawBitmap(bitmap, 0, 0, null);

                    document.finishPage(pdfPage);
                    bitmap.recycle();
                }
            }

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.writeTo(fos);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding watermark", e);
            return false;
        } finally {
            document.close();
        }
    }

    /**
     * Sign PDF with drawn signature
     */
    public static boolean addSignature(File sourcePdf, File outputFile, Bitmap signature, int pageIndex, float normX, float normY) {
        return signPdf(sourcePdf, outputFile, signature, pageIndex, normX, normY);
    }

    /**
     * Sign PDF with drawn signature
     */
    public static boolean signPdf(File sourcePdf, File outputFile, Bitmap signature, int pageIndex, float normX, float normY) {
        if (sourcePdf == null || !sourcePdf.exists() || signature == null) return false;

        PdfDocument document = new PdfDocument();

        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(sourcePdf, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {

            int count = renderer.getPageCount();
            for (int i = 0; i < count; i++) {
                try (PdfRenderer.Page page = renderer.openPage(i)) {
                    int w = page.getWidth();
                    int h = page.getHeight();

                    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    if (i == pageIndex) {
                        int sigW = w / 3;
                        int sigH = (int) (((float) sigW / signature.getWidth()) * signature.getHeight());
                        float posX = normX * w;
                        float posY = normY * h;

                        Bitmap scaledSig = Bitmap.createScaledBitmap(signature, sigW, sigH, true);
                        canvas.drawBitmap(scaledSig, posX, posY, null);
                        scaledSig.recycle();
                    }

                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(w, h, i + 1).create();
                    PdfDocument.Page pdfPage = document.startPage(pageInfo);
                    pdfPage.getCanvas().drawBitmap(bitmap, 0, 0, null);

                    document.finishPage(pdfPage);
                    bitmap.recycle();
                }
            }

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.writeTo(fos);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error signing PDF", e);
            return false;
        } finally {
            document.close();
        }
    }

    /**
     * Reorder pages in PDF according to new page index list
     */
    public static boolean reorderPages(File sourcePdf, File outputFile, List<Integer> pageOrder) {
        return extractPages(sourcePdf, outputFile, pageOrder);
    }
}
