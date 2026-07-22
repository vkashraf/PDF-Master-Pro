package com.example.data.model;

public class PdfOperation {
    public enum Type {
        READER,
        IMAGE_TO_PDF,
        MERGE,
        SPLIT,
        COMPRESS,
        ROTATE,
        LOCK,
        UNLOCK,
        WATERMARK,
        EXTRACT,
        REORDER,
        SIGN,
        SCANNER,
        RECENTS,
        FAVORITES,
        SETTINGS,
        PREMIUM
    }

    private final Type type;
    private final String title;
    private final String description;
    private final int iconResId;
    private final boolean isPro;

    public PdfOperation(Type type, String title, String description, int iconResId, boolean isPro) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.isPro = isPro;
    }

    public Type getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }
    public boolean isPro() { return isPro; }
}
