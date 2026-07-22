package com.example.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.model.PdfDocumentEntity;
import com.example.data.repository.PdfRepository;
import com.example.engine.PremiumManager;

import java.util.List;

public class PdfViewModel extends AndroidViewModel {

    private final PdfRepository repository;
    private final PremiumManager premiumManager;

    private final LiveData<List<PdfDocumentEntity>> recentFiles;
    private final LiveData<List<PdfDocumentEntity>> favoriteFiles;

    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<PdfDocumentEntity> selectedPdf = new MutableLiveData<>();

    public PdfViewModel(@NonNull Application application) {
        super(application);
        this.repository = new PdfRepository(application);
        this.premiumManager = new PremiumManager(application);

        this.recentFiles = repository.getAllRecents();
        this.favoriteFiles = repository.getFavorites();
    }

    public LiveData<List<PdfDocumentEntity>> getRecentFiles() { return recentFiles; }
    public LiveData<List<PdfDocumentEntity>> getFavoriteFiles() { return favoriteFiles; }
    public LiveData<Boolean> getIsProcessing() { return isProcessing; }
    public LiveData<String> getStatusMessage() { return statusMessage; }
    public LiveData<PdfDocumentEntity> getSelectedPdf() { return selectedPdf; }

    public void setSelectedPdf(PdfDocumentEntity pdf) {
        selectedPdf.setValue(pdf);
    }

    public void setProcessing(boolean processing) {
        isProcessing.postValue(processing);
    }

    public void setStatusMessage(String message) {
        statusMessage.postValue(message);
    }

    public void insertOrUpdateDocument(PdfDocumentEntity doc) {
        repository.insertOrUpdate(doc);
    }

    public void toggleFavorite(PdfDocumentEntity doc) {
        repository.toggleFavorite(doc);
    }

    public void deleteDocument(PdfDocumentEntity doc) {
        repository.delete(doc);
    }

    public boolean isPro() {
        return premiumManager.isProUnlocked();
    }

    public void setProUnlocked(boolean unlocked) {
        premiumManager.setProUnlocked(unlocked);
    }

    public PremiumManager getPremiumManager() {
        return premiumManager;
    }
}
