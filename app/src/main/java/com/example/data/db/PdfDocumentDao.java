package com.example.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.model.PdfDocumentEntity;

import java.util.List;

@Dao
public interface PdfDocumentDao {

    @Query("SELECT * FROM pdf_documents ORDER BY lastOpenedTimestamp DESC")
    LiveData<List<PdfDocumentEntity>> getAllRecents();

    @Query("SELECT * FROM pdf_documents WHERE isFavorite = 1 ORDER BY lastOpenedTimestamp DESC")
    LiveData<List<PdfDocumentEntity>> getFavorites();

    @Query("SELECT * FROM pdf_documents WHERE fileName LIKE '%' || :query || '%' ORDER BY lastOpenedTimestamp DESC")
    LiveData<List<PdfDocumentEntity>> searchDocuments(String query);

    @Query("SELECT * FROM pdf_documents WHERE filePath = :path LIMIT 1")
    PdfDocumentEntity getByPath(String path);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PdfDocumentEntity entity);

    @Update
    void update(PdfDocumentEntity entity);

    @Delete
    void delete(PdfDocumentEntity entity);

    @Query("DELETE FROM pdf_documents WHERE filePath = :path")
    void deleteByPath(String path);

    @Query("DELETE FROM pdf_documents")
    void clearAll();
}
