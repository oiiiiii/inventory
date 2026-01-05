package com.example.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 图片预览适配器（支持删除）
 */
public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder> {
    private Context mContext;
    private List<String> mImagePaths;
    private OnImageDeleteListener mDeleteListener;
    private OnImageClickListener mClickListener;

    // 图片点击/删除监听
    public interface OnImageDeleteListener {
        void onDelete(int position);
    }

    public interface OnImageClickListener {
        void onClick(int position);
    }

    public ImagePreviewAdapter(Context context, List<String> imagePaths) {
        this.mContext = context;
        this.mImagePaths = imagePaths;
    }

    public void setOnImageDeleteListener(OnImageDeleteListener listener) {
        this.mDeleteListener = listener;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.mClickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String path = mImagePaths.get(position);
        // 加载图片（压缩）
        holder.ivImage.setImageBitmap(ImageUtils.loadImage(path, 200, 200));

        // 点击图片
        holder.ivImage.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onClick(position);
            }
        });

        // 删除图片
        holder.btnDelete.setOnClickListener(v -> {
            if (mDeleteListener != null) {
                mDeleteListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImagePaths.size();
    }

    // 刷新数据
    public void refreshData(List<String> imagePaths) {
        this.mImagePaths = imagePaths;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageButton btnDelete;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}