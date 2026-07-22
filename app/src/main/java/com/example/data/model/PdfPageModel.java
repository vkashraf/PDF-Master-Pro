package com.example.data.model;

import android.graphics.Bitmap;

public class PdfPageModel {
    private final int pageIndex;
    private Bitmap pageBitmap;
    private boolean isSelected;
    private int rotationAngle;

    public PdfPageModel(int pageIndex, Bitmap pageBitmap) {
        this(pageIndex, pageBitmap, false);
    }

    public PdfPageModel(int pageIndex, Bitmap pageBitmap, boolean isSelected) {
        this.pageIndex = pageIndex;
        this.pageBitmap = pageBitmap;
        this.isSelected = isSelected;
        this.rotationAngle = 0;
    }

    public int getPageIndex() { return pageIndex; }
    public Bitmap getPageBitmap() { return pageBitmap; }
    public void setPageBitmap(Bitmap pageBitmap) { this.pageBitmap = pageBitmap; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public int getRotationAngle() { return rotationAngle; }
    public void setRotationAngle(int rotationAngle) { this.rotationAngle = rotationAngle; }
}
