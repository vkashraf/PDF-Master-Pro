package com.example.ui.premium;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.databinding.FragmentPremiumBinding;
import com.example.ui.viewmodel.PdfViewModel;

public class PremiumFragment extends Fragment {

    private FragmentPremiumBinding binding;
    private PdfViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPremiumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        updateProStatusUI();

        binding.btnActivatePro.setOnClickListener(v -> {
            viewModel.setProUnlocked(true);
            updateProStatusUI();
            Toast.makeText(requireContext(), "🎉 Pro License Activated Offline!", Toast.LENGTH_LONG).show();
        });
    }

    private void updateProStatusUI() {
        if (viewModel.isPro()) {
            binding.tvProStatus.setVisibility(View.VISIBLE);
            binding.tvProStatus.setText("★ PRO LICENSE ACTIVE (LIFETIME OFFLINE) ★");
            binding.btnActivatePro.setText("PRO ACTIVE");
            binding.btnActivatePro.setEnabled(false);
        } else {
            binding.tvProStatus.setVisibility(View.GONE);
            binding.btnActivatePro.setText("Activate Pro (Offline)");
            binding.btnActivatePro.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
