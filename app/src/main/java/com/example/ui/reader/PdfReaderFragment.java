package com.example.ui.reader;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.data.model.PdfDocumentEntity;
import com.example.databinding.FragmentPdfReaderBinding;
import com.example.engine.PdfEngine;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.util.concurrent.Executors;

public class PdfReaderFragment extends Fragment {

    private FragmentPdfReaderBinding binding;
    private PdfViewModel viewModel;
    private File currentPdfFile;
    private int totalPages = 1;
    private int currentPageIndex = 0;
    private boolean isNightMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPdfReaderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        viewModel.getSelectedPdf().observe(getViewLifecycleOwner(), this::loadPdfDocument);

        binding.sliderPage.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                currentPageIndex = (int) value - 1;
                renderPage();
            }
        });

        binding.btnNightMode.setOnClickListener(v -> {
            isNightMode = !isNightMode;
            applyNightFilter();
        });
    }

    private void loadPdfDocument(PdfDocumentEntity doc) {
        if (doc == null || doc.getFilePath() == null) return;
        currentPdfFile = new File(doc.getFilePath());
        if (!currentPdfFile.exists()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            totalPages = PdfEngine.getPageCount(currentPdfFile);
            if (totalPages < 1) totalPages = 1;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    binding.sliderPage.setValueTo((float) totalPages);
                    currentPageIndex = 0;
                    renderPage();
                });
            }
        });
    }

    private void renderPage() {
        if (currentPdfFile == null || !currentPdfFile.exists()) return;

        binding.tvPageIndicator.setText("Page " + (currentPageIndex + 1) + " / " + totalPages);

        Executors.newSingleThreadExecutor().execute(() -> {
            Bitmap pageBitmap = PdfEngine.renderPage(currentPdfFile, currentPageIndex, 1080);
            if (pageBitmap != null && isAdded()) {
                requireActivity().runOnUiThread(() -> binding.ivPdfPageView.setImageBitmap(pageBitmap));
            }
        });
    }

    private void applyNightFilter() {
        if (isNightMode) {
            ColorMatrix cm = new ColorMatrix(new float[]{
                    -1, 0, 0, 0, 255,
                    0, -1, 0, 0, 255,
                    0, 0, -1, 0, 255,
                    0, 0, 0, 1, 0
            });
            binding.ivPdfPageView.setColorFilter(new ColorMatrixColorFilter(cm));
        } else {
            binding.ivPdfPageView.setColorFilter(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
