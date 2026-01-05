package com.example.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

/**
 * 图片预览ViewPager适配器
 */
public class ImagePagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<String> mImagePaths;

    public ImagePagerAdapter(Context context, List<String> imagePaths) {
        this.mContext = context;
        this.mImagePaths = imagePaths;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_pager, container, false);
        ImageView ivImage = view.findViewById(R.id.iv_image);
        // 加载原图（压缩避免OOM）
        ivImage.setImageBitmap(ImageUtils.loadImage(mImagePaths.get(position), 1080, 1920));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImagePaths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}