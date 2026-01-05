package com.example.inventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class ImagePreviewActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private List<String> mImagePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        // 获取图片路径列表
        mImagePaths = getIntent().getStringArrayListExtra("imagePaths");
        int position = getIntent().getIntExtra("position", 0);

        if (mImagePaths == null || mImagePaths.isEmpty()) {
            Toast.makeText(this, "暂无图片可预览", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化ViewPager
        mViewPager = findViewById(R.id.view_pager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, mImagePaths);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(position);

        // 点击图片关闭预览
        mViewPager.setOnClickListener(v -> finish());
    }
}