package com.example.ui.imagetopdf;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.example.databinding.FragmentImageToPdfBinding;
import com.example.engine.PdfEngine;
import com.example.ui.adapters.ImageItemAdapter;
import com.example.ui.viewmodel.PdfViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ImageToPdfFragment extends Fragment implements ImageItemAdapter.OnImageRemoveListener {

    private FragmentImageToPdfBinding binding;
    private PdfViewModel viewModel;
    private final List<String> selectedImagePaths = new ArrayList<>();
    private ImageItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentImageToPdfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PdfViewModel.class);

        adapter = new ImageItemAdapter(selectedImagePaths, this);
        binding.rvPickedImages.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPickedImages.setAdapter(adapter);

        binding.btnAddImages.setOnClickListener(v -> createDemoImageAndAdd());

        binding.btnConvertToPdf.setOnClickListener(v -> convertToPdf());
    }

    private void createDemoImageAndAdd() {
        try {
            File cacheDir = requireContext().getCacheDir();
            File imageFile = new File(cacheDir, "sample_img_" + System.currentTimeMillis() + ".png");
            
            Bitmap bitmap = Bitmap.createBitmap(800, 1000, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);

            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#FF3B30"));
            paint.setTextSize(48f);
            canvas.drawText("PDF Master Pro Document #" + (selectedImagePaths.size() + 1), 60f, 200f, paint);

            paint.setColor(Color.DKGRAY);
            paint.setTextSize(32f);
            canvas.drawText("Created Offline on Android", 60f, 300f, paint);

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }

            selectedImagePaths.add(imageFile.getAbsolutePath());
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Image Added (" + selectedImagePaths.size() + ")", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error creating image", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertToPdf() {
        if (selectedImagePaths.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least 1 image", Toast.LENGTH_SHORT).show();
            return;
        }

        File outputDir = new File(requireContext().getFilesDir(), "PDFs");
        File outputFile = new File(outputDir, "Converted_" + System.currentTimeMillis() + ".pdf");

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = PdfEngine.convertImagesToPdf(selectedImagePaths, outputFile, 20);
            if (success && isAdded()) {
                PdfDocumentEntity doc = new PdfDocumentEntity(
                        outputFile.getAbsolutePath(),
                        outputFile.getName(),
                        outputFile.length(),
                        selectedImagePaths.size(),
                        System.currentTimeMillis(),
                        false,
                        false,
                        "",
                        "CONVERTED"
                );
                viewModel.insertOrUpdateDocument(doc);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "PDF Created Successfully!", Toast.LENGTH_LONG).show();
                    viewModel.setSelectedPdf(doc);
                    View v = getView();
                    if (v != null) {
                        Navigation.findNavController(v).popBackStack();
                    }
                });
            }
        });
    }

    @Override
    public void onRemove(int position) {
        if (position >= 0 && position < selectedImagePaths.size()) {
            selectedImagePaths.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
