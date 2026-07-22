package com.example.ui.lock;

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
import com.example.databinding.FragmentLockPdfBinding;
import com.example.ui.viewmodel.PdfViewModel;

import java.util.List;

public class LockPdfFragment extends Fragment {

    private FragmentLockPdfBinding binding;
    private PdfViewModel viewModel;
    private PdfDocumentEntity selectedPdf;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLockPdfBinding.inflate(inflater, container, false);
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

        binding.btnLock.setOnClickListener(v -> {
            if (selectedPdf == null) {
                Toast.makeText(requireContext(), "Please select a PDF document first", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedPdf.setEncrypted(true);
            viewModel.insertOrUpdateDocument(selectedPdf);
            Toast.makeText(requireContext(), "PDF Protection Password Applied!", Toast.LENGTH_LONG).show();
            Navigation.findNavController(v).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
