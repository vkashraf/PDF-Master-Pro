package com.example.ui.watermark;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.data.model.PdfDocumentEntity;
import com.example.databinding.FragmentWatermarkPdfBinding;
import com.example.engine.PdfEngine;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

public class WatermarkPdfFragment extends Fragment {

    private FragmentWatermarkPdfBinding binding;
    private PdfViewModel viewModel;
    private PdfDocumentEntity selectedPdf;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWatermarkPdfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        viewModel.getSelectedPdf().observe(getViewLifecycleOwner(), pdf -> {
            this.selectedPdf = pdf;
            if (pdf != null) {
                binding.tvSelectedFilename.setText(pdf.getFileName());
            }
        });

        binding.btnSelectPdf.setOnClickListener(v -> {
            List<PdfDocumentEntity> recents = viewModel.getRecentFiles().getValue();
            if (recents != null && !recents.isEmpty()) {
                viewModel.setSelectedPdf(recents.get(0));
            }
        });

        binding.btnApplyWatermark.setOnClickListener(v -> applyWatermark());
    }

    private void applyWatermark() {
        if (selectedPdf == null) {
            Toast.makeText(requireContext(), "Please select a PDF document first", Toast.LENGTH_SHORT).show();
            return;
        }

        File sourceFile = new File(selectedPdf.getFilePath());
        if (!sourceFile.exists()) return;

        String watermarkText = binding.etWatermarkText.getText().toString();
        if (watermarkText.isEmpty()) watermarkText = "CONFIDENTIAL";

        float angle = binding.sliderAngle.getValue();

        File outputDir = new File(requireContext().getFilesDir(), "PDFs");
        File outputFile = new File(outputDir, "Watermarked_" + System.currentTimeMillis() + ".pdf");

        final String text = watermarkText;

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = PdfEngine.addWatermark(sourceFile, outputFile, text, angle);
            if (success && isAdded()) {
                int count = PdfEngine.getPageCount(outputFile);
                PdfDocumentEntity doc = new PdfDocumentEntity(
                        outputFile.getAbsolutePath(),
                        outputFile.getName(),
                        outputFile.length(),
                        count,
                        System.currentTimeMillis(),
                        false,
                        false,
                        "",
                        "REGULAR"
                );
                viewModel.insertOrUpdateDocument(doc);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Watermark Applied Successfully!", Toast.LENGTH_LONG).show();
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
