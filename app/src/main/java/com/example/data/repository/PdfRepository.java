package com.example.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.data.db.AppDatabase;
import com.example.data.db.PdfDocumentDao;
import com.example.data.model.PdfDocumentEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfRepository {

    private final PdfDocumentDao dao;
    private final ExecutorService executor;

    public PdfRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dao = db.pdfDocumentDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<PdfDocumentEntity>> getAllRecents() {
        return dao.getAllRecents();
    }

    public LiveData<List<PdfDocumentEntity>> getFavorites() {
        return dao.getFavorites();
    }

    public LiveData<List<PdfDocumentEntity>> searchDocuments(String query) {
        return dao.searchDocuments(query);
    }

    public void insertOrUpdate(PdfDocumentEntity entity) {
        executor.execute(() -> dao.insert(entity));
    }

    public void toggleFavorite(PdfDocumentEntity entity) {
        executor.execute(() -> {
            entity.setFavorite(!entity.isFavorite());
            dao.update(entity);
        });
    }

    public void delete(PdfDocumentEntity entity) {
        executor.execute(() -> dao.delete(entity));
    }

    public void clearHistory() {
        executor.execute(dao::clearAll);
    }
}
