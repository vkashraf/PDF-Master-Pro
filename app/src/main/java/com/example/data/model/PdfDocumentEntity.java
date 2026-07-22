package com.example.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pdf_documents")
public class PdfDocumentEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String filePath;
    private String fileName;
    private long fileSize;
    private int pageCount;
    private long lastOpenedTimestamp;
    private boolean isFavorite;
    private boolean isEncrypted;
    private String thumbnailPath;
    private String category;

    public PdfDocumentEntity(String filePath, String fileName, long fileSize, int pageCount,
                             long lastOpenedTimestamp, boolean isFavorite, boolean isEncrypted,
                             String thumbnailPath, String category) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pageCount = pageCount;
        this.lastOpenedTimestamp = lastOpenedTimestamp;
        this.isFavorite = isFavorite;
        this.isEncrypted = isEncrypted;
        this.thumbnailPath = thumbnailPath;
        this.category = category;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public long getLastOpenedTimestamp() { return lastOpenedTimestamp; }
    public void setLastOpenedTimestamp(long lastOpenedTimestamp) { this.lastOpenedTimestamp = lastOpenedTimestamp; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isEncrypted() { return isEncrypted; }
    public void setEncrypted(boolean encrypted) { isEncrypted = encrypted; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
