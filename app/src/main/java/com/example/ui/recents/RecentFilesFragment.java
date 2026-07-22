package com.example.ui.recents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.data.model.PdfDocumentEntity;
import com.example.databinding.FragmentRecentsBinding;
import com.example.ui.adapters.PdfDocumentAdapter;
import com.example.ui.viewmodel.PdfViewModel;

import java.util.ArrayList;

public class RecentFilesFragment extends Fragment implements PdfDocumentAdapter.OnPdfItemClickListener {

    private FragmentRecentsBinding binding;
    private PdfViewModel viewModel;
    private PdfDocumentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        adapter = new PdfDocumentAdapter(new ArrayList<>(), this);
        binding.rvRecentsFull.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentsFull.setAdapter(adapter);

        viewModel.getRecentFiles().observe(getViewLifecycleOwner(), list -> adapter.setPdfList(list));
    }

    @Override
    public void onPdfClick(PdfDocumentEntity entity) {
        viewModel.setSelectedPdf(entity);
        View view = getView();
        if (view != null) Navigation.findNavController(view).navigate(R.id.nav_reader);
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
    public void onShareClick(PdfDocumentEntity entity) {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
