package com.example.ui.split;

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
import com.example.databinding.FragmentSplitPdfBinding;
import com.example.engine.PdfEngine;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SplitPdfFragment extends Fragment {

    private FragmentSplitPdfBinding binding;
    private PdfViewModel viewModel;
    private PdfDocumentEntity selectedPdf;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplitPdfBinding.inflate(inflater, container, false);
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
            } else {
                Toast.makeText(requireContext(), "No PDF selected. Convert or open a PDF first.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSplit.setOnClickListener(v -> performSplit());
    }

    private void performSplit() {
        if (selectedPdf == null) {
            Toast.makeText(requireContext(), "Please select a PDF document first", Toast.LENGTH_SHORT).show();
            return;
        }

        File sourceFile = new File(selectedPdf.getFilePath());
        if (!sourceFile.exists()) return;

        File outputDir = new File(requireContext().getFilesDir(), "PDFs");
        File outputFile = new File(outputDir, "Split_" + System.currentTimeMillis() + ".pdf");

        List<Integer> pagesToExtract = new ArrayList<>();
        pagesToExtract.add(0); // Default to extracting page 1

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = PdfEngine.extractPages(sourceFile, outputFile, pagesToExtract);
            if (success && isAdded()) {
                PdfDocumentEntity doc = new PdfDocumentEntity(
                        outputFile.getAbsolutePath(),
                        outputFile.getName(),
                        outputFile.length(),
                        pagesToExtract.size(),
                        System.currentTimeMillis(),
                        false,
                        false,
                        "",
                        "REGULAR"
                );
                viewModel.insertOrUpdateDocument(doc);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "PDF Split Successfully!", Toast.LENGTH_LONG).show();
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
