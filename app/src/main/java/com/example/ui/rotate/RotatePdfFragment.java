package com.example.ui.rotate;

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
import com.example.databinding.FragmentRotatePdfBinding;
import com.example.engine.PdfEngine;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

public class RotatePdfFragment extends Fragment {

    private FragmentRotatePdfBinding binding;
    private PdfViewModel viewModel;
    private PdfDocumentEntity selectedPdf;
    private int selectedDegrees = 90;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRotatePdfBinding.inflate(inflater, container, false);
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

        binding.btnRotate90.setOnClickListener(v -> selectedDegrees = 90);
        binding.btnRotate180.setOnClickListener(v -> selectedDegrees = 180);
        binding.btnRotate270.setOnClickListener(v -> selectedDegrees = 270);

        binding.btnSaveRotation.setOnClickListener(v -> performRotation());
    }

    private void performRotation() {
        if (selectedPdf == null) {
            Toast.makeText(requireContext(), "Please select a PDF document first", Toast.LENGTH_SHORT).show();
            return;
        }

        File sourceFile = new File(selectedPdf.getFilePath());
        if (!sourceFile.exists()) return;

        File outputDir = new File(requireContext().getFilesDir(), "PDFs");
        File outputFile = new File(outputDir, "Rotated_" + System.currentTimeMillis() + ".pdf");

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = PdfEngine.rotatePdf(sourceFile, outputFile, selectedDegrees);
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
                    Toast.makeText(requireContext(), "PDF Rotated Successfully!", Toast.LENGTH_LONG).show();
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
