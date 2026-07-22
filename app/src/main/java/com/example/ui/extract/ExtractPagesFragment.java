package com.example.ui.extract;

import android.graphics.Bitmap;
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
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.data.model.PdfDocumentEntity;
import com.example.data.model.PdfPageModel;
import com.example.databinding.FragmentExtractPagesBinding;
import com.example.engine.PdfEngine;
import com.example.ui.adapters.PdfPageAdapter;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ExtractPagesFragment extends Fragment implements PdfPageAdapter.OnPageClickListener {

    private FragmentExtractPagesBinding binding;
    private PdfViewModel viewModel;
    private PdfDocumentEntity selectedPdf;
    private final List<PdfPageModel> pageList = new ArrayList<>();
    private PdfPageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExtractPagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        adapter = new PdfPageAdapter(pageList, this);
        binding.rvPagesGrid.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvPagesGrid.setAdapter(adapter);

        viewModel.getSelectedPdf().observe(getViewLifecycleOwner(), pdf -> {
            this.selectedPdf = pdf;
            if (pdf != null) loadPages(pdf);
        });

        binding.btnSelectPdf.setOnClickListener(v -> {
            List<PdfDocumentEntity> recents = viewModel.getRecentFiles().getValue();
            if (recents != null && !recents.isEmpty()) {
                viewModel.setSelectedPdf(recents.get(0));
            }
        });

        binding.btnExtract.setOnClickListener(v -> performExtract());
    }

    private void loadPages(PdfDocumentEntity doc) {
        pageList.clear();
        File file = new File(doc.getFilePath());
        if (!file.exists()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            int count = PdfEngine.getPageCount(file);
            for (int i = 0; i < count; i++) {
                Bitmap bmp = PdfEngine.renderPage(file, i, 300);
                pageList.add(new PdfPageModel(i, bmp, false));
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        });
    }

    private void performExtract() {
        if (selectedPdf == null) return;
        List<Integer> selectedIndices = new ArrayList<>();
        for (PdfPageModel page : pageList) {
            if (page.isSelected()) selectedIndices.add(page.getPageIndex());
        }

        if (selectedIndices.isEmpty()) {
            Toast.makeText(requireContext(), "Tap pages to select them for extraction", Toast.LENGTH_SHORT).show();
            return;
        }

        File sourceFile = new File(selectedPdf.getFilePath());
        File outputDir = new File(requireContext().getFilesDir(), "PDFs");
        File outputFile = new File(outputDir, "Extracted_" + System.currentTimeMillis() + ".pdf");

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = PdfEngine.extractPages(sourceFile, outputFile, selectedIndices);
            if (success && isAdded()) {
                PdfDocumentEntity doc = new PdfDocumentEntity(
                        outputFile.getAbsolutePath(),
                        outputFile.getName(),
                        outputFile.length(),
                        selectedIndices.size(),
                        System.currentTimeMillis(),
                        false,
                        false,
                        "",
                        "REGULAR"
                );
                viewModel.insertOrUpdateDocument(doc);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Pages Extracted Successfully!", Toast.LENGTH_LONG).show();
                    View v = getView();
                    if (v != null) Navigation.findNavController(v).popBackStack();
                });
            }
        });
    }

    @Override
    public void onPageClick(PdfPageModel page, int position) {
        page.setSelected(!page.isSelected());
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
