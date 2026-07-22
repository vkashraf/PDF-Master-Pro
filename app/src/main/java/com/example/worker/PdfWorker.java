package com.example.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.engine.PdfEngine;

import java.io.File;

public class PdfWorker extends Worker {

    public static final String KEY_ACTION = "action";
    public static final String KEY_INPUT_PATH = "input_path";
    public static final String KEY_OUTPUT_PATH = "output_path";

    public PdfWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String action = getInputData().getString(KEY_ACTION);
        String inputPath = getInputData().getString(KEY_INPUT_PATH);
        String outputPath = getInputData().getString(KEY_OUTPUT_PATH);

        if (inputPath == null || outputPath == null) {
            return Result.failure();
        }

        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        boolean success = false;
        if ("compress".equals(action)) {
            success = PdfEngine.compressPdf(inputFile, outputFile, 0.6f);
        } else if ("rotate".equals(action)) {
            success = PdfEngine.rotatePdf(inputFile, outputFile, 90);
        }

        return success ? Result.success() : Result.failure();
    }
}
