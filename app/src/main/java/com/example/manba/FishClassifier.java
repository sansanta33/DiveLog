package com.example.manba;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FishClassifier {

    private final Interpreter interpreter;
    private final List<String> labels;

    public FishClassifier(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
        labels = loadLabels(context);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor =
                context.getAssets().openFd("model_unquant.tflite");

        FileInputStream inputStream =
                new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
        );
    }

    private List<String> loadLabels(Context context) throws IOException {

        List<String> result = new ArrayList<>();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                context.getAssets().open("labels.txt")
                        )
                );

        String line;

        while ((line = reader.readLine()) != null) {

            String[] parts = line.split(" ", 2);

            if (parts.length == 2) {
                result.add(parts[1]);
            } else {
                result.add(line);
            }
        }

        reader.close();

        return result;
    }

    public String classify(Bitmap bitmap) {

        Bitmap resized =
                Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        ByteBuffer input =
                ByteBuffer.allocateDirect(4 * 224 * 224 * 3);

        input.order(ByteOrder.nativeOrder());

        int[] pixels = new int[224 * 224];

        resized.getPixels(
                pixels,
                0,
                224,
                0,
                0,
                224,
                224
        );

        int index = 0;

        for (int y = 0; y < 224; y++) {

            for (int x = 0; x < 224; x++) {

                int pixel = pixels[index++];

                input.putFloat(((pixel >> 16) & 0xFF) / 255f);
                input.putFloat(((pixel >> 8) & 0xFF) / 255f);
                input.putFloat((pixel & 0xFF) / 255f);
            }
        }

        float[][] output = new float[1][labels.size()];

        interpreter.run(input, output);

        int bestIndex = 0;
        float bestScore = output[0][0];

        for (int i = 1; i < output[0].length; i++) {

            if (output[0][i] > bestScore) {

                bestScore = output[0][i];
                bestIndex = i;
            }
        }

        String label = labels.get(bestIndex);

        switch (label) {

            case "clownfish":
                return "小丑鱼";

            case "lionfish":
                return "狮子鱼";

            case "manta":
                return "蝠鲼（Manta）";

            case "sunfish":
                return "翻车鱼";

            default:
                return label;
        }
    }
}