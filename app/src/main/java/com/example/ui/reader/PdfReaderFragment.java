package com.example.ui.reader;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.data.model.PdfDocumentEntity;
import com.example.databinding.FragmentPdfReaderBinding;
import com.example.engine.PdfEngine;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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

        binding.btnCapturePage.setOnClickListener(v -> captureAndSavePage());
    }

    private void captureAndSavePage() {
        if (currentPdfFile == null || !currentPdfFile.exists()) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            Bitmap pageBitmap = PdfEngine.renderPage(currentPdfFile, currentPageIndex, 1080);
            if (pageBitmap == null) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to render page for capture", Toast.LENGTH_SHORT).show();
                    });
                }
                return;
            }

            String filename = "PDF_Page_" + (currentPageIndex + 1) + "_" + System.currentTimeMillis() + ".png";
            boolean success = saveBitmapToPicturesDirectory(pageBitmap, filename);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (success) {
                        Toast.makeText(requireContext(), "Page captured & saved to Pictures!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save captured page.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean saveBitmapToPicturesDirectory(Bitmap bitmap, String filename) {
        OutputStream outputStream = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PDF_Captures");

                Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    outputStream = requireContext().getContentResolver().openOutputStream(uri);
                }
            } else {
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File appDir = new File(picturesDir, "PDF_Captures");
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }
                File imageFile = new File(appDir, filename);
                outputStream = new FileOutputStream(imageFile);

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(imageFile));
                requireContext().sendBroadcast(mediaScanIntent);
            }

            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try { outputStream.close(); } catch (Exception ignored) {}
            }
        }
        return false;
    }

    private void loadPdfDocument(PdfDocumentEntity doc) {
        if (doc == null || doc.getFilePath() == null) return;
        currentPdfFile = new File(doc.getFilePath());
        if (!currentPdfFile.exists()) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        Executors.newSingleThreadExecutor().execute(() -> {
            totalPages = PdfEngine.getPageCount(currentPdfFile);
            if (totalPages < 1) totalPages = 1;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (totalPages > 1) {
                        binding.sliderPage.setValueTo((float) totalPages);
                        binding.sliderPage.setStepSize(1.0f);
                    } else {
                        binding.sliderPage.setValueTo(1.0f);
                        binding.sliderPage.setStepSize(0.0f);
                    }
                    binding.sliderPage.setValue(1.0f);
                    currentPageIndex = 0;
                    renderPage();
                });
            }
        });
    }

    private void renderPage() {
        if (currentPdfFile == null || !currentPdfFile.exists()) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvPageIndicator.setText("Page " + (currentPageIndex + 1) + " of " + totalPages);

        Executors.newSingleThreadExecutor().execute(() -> {
            Bitmap pageBitmap = PdfEngine.renderPage(currentPdfFile, currentPageIndex, 1080);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (pageBitmap != null) {
                        binding.ivPdfPageView.setImageBitmap(pageBitmap);
                    }
                });
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
