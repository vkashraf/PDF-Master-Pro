package com.example.ui.scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.data.model.PdfDocumentEntity;
import com.example.databinding.FragmentScannerBinding;
import com.example.engine.DocumentScannerEngine;
import com.example.engine.PdfEngine;
import com.example.ui.viewmodel.PdfViewModel;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class DocumentScannerFragment extends Fragment {

    private FragmentScannerBinding binding;
    private PdfViewModel viewModel;
    private ImageCapture imageCapture;
    private Bitmap rawCapturedBitmap;
    private Bitmap processedBitmap;
    private File tempImageFile;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission required for document scanning", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        binding.fabCapture.setOnClickListener(v -> takePhoto());

        binding.chipOriginal.setOnClickListener(v -> applyFilter(DocumentScannerEngine.FilterType.ORIGINAL));
        binding.chipMagic.setOnClickListener(v -> applyFilter(DocumentScannerEngine.FilterType.MAGIC_COLOR));
        binding.chipGrayscale.setOnClickListener(v -> applyFilter(DocumentScannerEngine.FilterType.GRAYSCALE));
        binding.chipBw.setOnClickListener(v -> applyFilter(DocumentScannerEngine.FilterType.HIGH_CONTRAST_BW));

        binding.btnSaveScan.setOnClickListener(v -> saveScannedPdf());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        tempImageFile = new File(requireContext().getCacheDir(), "scan_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(tempImageFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                rawCapturedBitmap = BitmapFactory.decodeFile(tempImageFile.getAbsolutePath());
                processedBitmap = rawCapturedBitmap;

                binding.viewFinder.setVisibility(View.GONE);
                binding.fabCapture.setVisibility(View.GONE);
                binding.ivCapturedPreview.setVisibility(View.VISIBLE);
                binding.llFilterBar.setVisibility(View.VISIBLE);
                binding.btnSaveScan.setVisibility(View.VISIBLE);

                binding.ivCapturedPreview.setImageBitmap(rawCapturedBitmap);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(requireContext(), "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter(DocumentScannerEngine.FilterType filterType) {
        if (rawCapturedBitmap == null) return;
        processedBitmap = DocumentScannerEngine.applyFilter(rawCapturedBitmap, filterType);
        binding.ivCapturedPreview.setImageBitmap(processedBitmap);
    }

    private void saveScannedPdf() {
        if (tempImageFile == null || !tempImageFile.exists()) return;

        File outputDir = new File(requireContext().getFilesDir(), "PDFs");
        File outputFile = new File(outputDir, "ScannedDoc_" + System.currentTimeMillis() + ".pdf");

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = PdfEngine.convertImagesToPdf(Collections.singletonList(tempImageFile.getAbsolutePath()), outputFile, 0);
            if (success && isAdded()) {
                PdfDocumentEntity doc = new PdfDocumentEntity(
                        outputFile.getAbsolutePath(),
                        outputFile.getName(),
                        outputFile.length(),
                        1,
                        System.currentTimeMillis(),
                        false,
                        false,
                        "",
                        "SCANNED"
                );
                viewModel.insertOrUpdateDocument(doc);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Scanned PDF Saved Successfully!", Toast.LENGTH_LONG).show();
                    viewModel.setSelectedPdf(doc);
                    View v = getView();
                    if (v != null) Navigation.findNavController(v).popBackStack();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
