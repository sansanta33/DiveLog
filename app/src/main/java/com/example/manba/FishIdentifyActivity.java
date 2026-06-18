package com.example.manba;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.MediaStore;

public class FishIdentifyActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private TextView tvResult;
    private TextView tvDescription;
    private FishClassifier classifier;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    this::handleImage
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_identify);

        imagePreview = findViewById(R.id.imagePreview);
        tvResult = findViewById(R.id.tvResult);

        Button btnSelect =
                findViewById(R.id.btnSelectImage);

        try {
            classifier = new FishClassifier(this);
        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("模型加载失败");
        }

        btnSelect.setOnClickListener(v ->
                imagePicker.launch("image/*"));
        //鱼类介绍方法
        tvDescription = findViewById(R.id.tvDescription);
    }

    private void handleImage(Uri uri) {

        if (uri == null) return;

        try {

            Bitmap bitmap =
                    MediaStore.Images.Media.getBitmap(
                            getContentResolver(),
                            uri
                    );

            imagePreview.setImageBitmap(bitmap);

            if (classifier != null) {

                String result =
                        classifier.classify(bitmap);

                tvResult.setText(
                        "识别结果：\n\n" + result
                );

                tvResult.setText(
                        "识别结果\n\n" + result
                );

                tvDescription.setText(
                        getDescription(result)
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            tvResult.setText(
                    "识别失败：" + e.getMessage()
            );
        }
    }

    private String getDescription(String fishName) {

        switch (fishName) {

            case "小丑鱼":
                return "小丑鱼生活在海葵附近，与海葵形成共生关系，是最受欢迎的热带观赏鱼之一。";

            case "狮子鱼":
                return "狮子鱼拥有醒目的鳍刺和条纹外观，鳍刺含有毒素，需要避免接触。";

            case "蝠鲼（Manta）":
                return "蝠鲼是世界最大的鳐鱼之一，翼展可超过7米，以浮游生物为食。";

            case "翻车鱼":
                return "翻车鱼是世界上最重的硬骨鱼之一，体型独特，经常出现在远洋海域。";

            default:
                return "";
        }
    }
}