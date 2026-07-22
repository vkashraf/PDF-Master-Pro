package com.example.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.data.model.PdfDocumentEntity;
import com.example.data.model.PdfOperation;
import com.example.databinding.FragmentHomeBinding;
import com.example.ui.adapters.PdfDocumentAdapter;
import com.example.ui.adapters.ToolGridAdapter;
import com.example.ui.viewmodel.PdfViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ToolGridAdapter.OnToolClickListener, PdfDocumentAdapter.OnPdfItemClickListener {

    private FragmentHomeBinding binding;
    private PdfViewModel viewModel;
    private PdfDocumentAdapter recentsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        setupToolsGrid();
        setupRecentsList();

        binding.cardHeroPro.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.nav_pro)
        );
    }

    private void setupToolsGrid() {
        List<PdfOperation> operations = new ArrayList<>();
        operations.add(new PdfOperation(PdfOperation.Type.READER, getString(R.string.module_reader), getString(R.string.module_reader_desc), R.drawable.ic_pdf_reader, false));
        operations.add(new PdfOperation(PdfOperation.Type.IMAGE_TO_PDF, getString(R.string.module_img_to_pdf), getString(R.string.module_img_to_pdf_desc), R.drawable.ic_image_to_pdf, false));
        operations.add(new PdfOperation(PdfOperation.Type.MERGE, getString(R.string.module_merge), getString(R.string.module_merge_desc), R.drawable.ic_merge_pdf, false));
        operations.add(new PdfOperation(PdfOperation.Type.SPLIT, getString(R.string.module_split), getString(R.string.module_split_desc), R.drawable.ic_split_pdf, false));
        operations.add(new PdfOperation(PdfOperation.Type.COMPRESS, getString(R.string.module_compress), getString(R.string.module_compress_desc), R.drawable.ic_compress_pdf, false));
        operations.add(new PdfOperation(PdfOperation.Type.ROTATE, getString(R.string.module_rotate), getString(R.string.module_rotate_desc), R.drawable.ic_rotate_pdf, false));
        operations.add(new PdfOperation(PdfOperation.Type.LOCK, getString(R.string.module_lock), getString(R.string.module_lock_desc), R.drawable.ic_lock_pdf, false));
        operations.add(new PdfOperation(PdfOperation.Type.UNLOCK, getString(R.string.module_unlock), getString(R.string.module_unlock_desc), R.drawable.ic_unlock_pdf, false));
        operations.add(new PdfOperation(PdfOperation.Type.WATERMARK, getString(R.string.module_watermark), getString(R.string.module_watermark_desc), R.drawable.ic_watermark_pdf, true));
        operations.add(new PdfOperation(PdfOperation.Type.EXTRACT, getString(R.string.module_extract), getString(R.string.module_extract_desc), R.drawable.ic_extract_pages, false));
        operations.add(new PdfOperation(PdfOperation.Type.REORDER, getString(R.string.module_reorder), getString(R.string.module_reorder_desc), R.drawable.ic_reorder_pages, false));
        operations.add(new PdfOperation(PdfOperation.Type.SIGN, getString(R.string.module_sign), getString(R.string.module_sign_desc), R.drawable.ic_sign_pdf, true));
        operations.add(new PdfOperation(PdfOperation.Type.SCANNER, getString(R.string.module_scanner), getString(R.string.module_scanner_desc), R.drawable.ic_scanner, false));

        ToolGridAdapter adapter = new ToolGridAdapter(operations, this);
        binding.rvToolsGrid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvToolsGrid.setAdapter(adapter);
    }

    private void setupRecentsList() {
        recentsAdapter = new PdfDocumentAdapter(new ArrayList<>(), this);
        binding.rvRecentFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentFiles.setAdapter(recentsAdapter);

        viewModel.getRecentFiles().observe(getViewLifecycleOwner(), list -> {
            recentsAdapter.setPdfList(list);
        });
    }

    @Override
    public void onToolClick(PdfOperation operation) {
        View view = getView();
        if (view == null) return;

        switch (operation.getType()) {
            case READER:
                Navigation.findNavController(view).navigate(R.id.nav_reader);
                break;
            case IMAGE_TO_PDF:
                Navigation.findNavController(view).navigate(R.id.nav_img_to_pdf);
                break;
            case MERGE:
                Navigation.findNavController(view).navigate(R.id.nav_merge);
                break;
            case SPLIT:
                Navigation.findNavController(view).navigate(R.id.nav_split);
                break;
            case COMPRESS:
                Navigation.findNavController(view).navigate(R.id.nav_compress);
                break;
            case ROTATE:
                Navigation.findNavController(view).navigate(R.id.nav_rotate);
                break;
            case LOCK:
                Navigation.findNavController(view).navigate(R.id.nav_lock);
                break;
            case UNLOCK:
                Navigation.findNavController(view).navigate(R.id.nav_unlock);
                break;
            case WATERMARK:
                Navigation.findNavController(view).navigate(R.id.nav_watermark);
                break;
            case EXTRACT:
                Navigation.findNavController(view).navigate(R.id.nav_extract);
                break;
            case REORDER:
                Navigation.findNavController(view).navigate(R.id.nav_reorder);
                break;
            case SIGN:
                Navigation.findNavController(view).navigate(R.id.nav_sign);
                break;
            case SCANNER:
                Navigation.findNavController(view).navigate(R.id.nav_scanner);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPdfClick(PdfDocumentEntity entity) {
        viewModel.setSelectedPdf(entity);
        View view = getView();
        if (view != null) {
            Navigation.findNavController(view).navigate(R.id.nav_reader);
        }
    }

    @Override
    public void onFavoriteClick(PdfDocumentEntity entity) {
        viewModel.toggleFavorite(entity);
    }

    @Override
    public void onDeleteClick(PdfDocumentEntity entity) {
        viewModel.deleteDocument(entity);
    }

    @Override
    public void onShareClick(PdfDocumentEntity entity) {
        // Handle share intent
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
