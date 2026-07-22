package com.example.ui.favorites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.R;
import com.example.data.model.PdfDocumentEntity;
import com.example.databinding.FragmentFavoritesBinding;
import com.example.ui.adapters.PdfDocumentAdapter;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.util.ArrayList;

public class FavoritesFragment extends Fragment implements PdfDocumentAdapter.OnPdfItemClickListener {

    private FragmentFavoritesBinding binding;
    private PdfViewModel viewModel;
    private PdfDocumentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        adapter = new PdfDocumentAdapter(new ArrayList<>(), this);
        binding.rvFavoritesFull.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavoritesFull.setAdapter(adapter);

        viewModel.getFavoriteFiles().observe(getViewLifecycleOwner(), list -> adapter.setPdfList(list));
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
    public void onShareClick(PdfDocumentEntity entity) {
        if (entity == null || entity.getFilePath() == null) return;
        File file = new File(entity.getFilePath());
        if (!file.exists()) return;
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share PDF"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
