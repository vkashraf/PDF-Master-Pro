package com.example.ui.merge;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.data.model.PdfDocumentEntity;
import com.example.databinding.FragmentMergePdfBinding;
import com.example.engine.PdfEngine;
import com.example.ui.adapters.PdfDocumentAdapter;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MergePdfFragment extends Fragment implements PdfDocumentAdapter.OnPdfItemClickListener {

    private FragmentMergePdfBinding binding;
    private PdfViewModel viewModel;
    private final List<PdfDocumentEntity> selectedPdfs = new ArrayList<>();
    private PdfDocumentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMergePdfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        adapter = new PdfDocumentAdapter(selectedPdfs, this);
        binding.rvSelectedPdfs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSelectedPdfs.setAdapter(adapter);

        // Add recent PDFs if available
        viewModel.getRecentFiles().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty() && selectedPdfs.isEmpty()) {
                selectedPdfs.addAll(list);
                adapter.notifyDataSetChanged();
            }
        });

        binding.btnMergePdfs.setOnClickListener(v -> mergePdfs());
    }

    private void mergePdfs() {
        if (selectedPdfs.size() < 2) {
            Toast.makeText(requireContext(), "Need at least 2 PDF files to merge", Toast.LENGTH_SHORT).show();
            return;
        }

        List<File> filesToMerge = new ArrayList<>();
        for (PdfDocumentEntity entity : selectedPdfs) {
            File f = new File(entity.getFilePath());
            if (f.exists()) filesToMerge.add(f);
        }

        File outputDir = new File(requireContext().getFilesDir(), "PDFs");
        File outputFile = new File(outputDir, "Merged_" + System.currentTimeMillis() + ".pdf");

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = PdfEngine.mergePdfs(filesToMerge, outputFile);
            if (success && isAdded()) {
                int totalPages = PdfEngine.getPageCount(outputFile);
                PdfDocumentEntity doc = new PdfDocumentEntity(
                        outputFile.getAbsolutePath(),
                        outputFile.getName(),
                        outputFile.length(),
                        totalPages,
                        System.currentTimeMillis(),
                        false,
                        false,
                        "",
                        "MERGED"
                );
                viewModel.insertOrUpdateDocument(doc);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "PDFs Merged Successfully!", Toast.LENGTH_LONG).show();
                    View v = getView();
                    if (v != null) {
                        Navigation.findNavController(v).popBackStack();
                    }
                });
            }
        });
    }

    @Override
    public void onPdfClick(PdfDocumentEntity entity) {}
    @Override
    public void onFavoriteClick(PdfDocumentEntity entity) {}
    @Override
    public void onDeleteClick(PdfDocumentEntity entity) {
        selectedPdfs.remove(entity);
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onShareClick(PdfDocumentEntity entity) {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
