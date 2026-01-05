package com.example.inventory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 图片选择/拍照工具类
 */
public class ImageUtils {
    private static final String TAG = "ImageUtils";
    // 请求码
    public static final int REQUEST_CODE_CAMERA = 1002;
    public static final int REQUEST_CODE_GALLERY = 1003;
    // 临时拍照文件路径
    private static String mTempPhotoPath;

    /**
     * 打开相机拍照
     */
    public static void openCamera(Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // 创建临时文件
            File photoFile = createImageFile(activity);
            if (photoFile != null) {
                mTempPhotoPath = photoFile.getAbsolutePath();
                // 获取文件Uri（适配Android 7.0+）
                Uri photoURI = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".fileprovider",
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
            }
        }
    }

    /**
     * 打开图库选择图片
     */
    public static void openGallery(Activity activity) {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        activity.startActivityForResult(pickPhotoIntent, REQUEST_CODE_GALLERY);
    }

    /**
     * 创建临时图片文件
     */
    private static File createImageFile(Activity activity) {
        // 生成唯一文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        // 获取存储目录
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            // 创建临时文件
            File imageFile = File.createTempFile(
                    imageFileName,  /* 前缀 */
                    ".jpg",         /* 后缀 */
                    storageDir      /* 目录 */
            );
            return imageFile;
        } catch (IOException e) {
            Log.e(TAG, "创建图片文件失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 处理相机返回结果，获取图片路径
     */
    public static String handleCameraResult() {
        return mTempPhotoPath;
    }

    /**
     * 处理图库返回结果，保存图片到本地并返回路径
     */
    public static String handleGalleryResult(Activity activity, Uri uri) {
        try {
            // 从Uri获取Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            // 创建保存文件
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";
            File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(storageDir, imageFileName);
            // 保存Bitmap到文件
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "保存图库图片失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 根据路径加载图片（压缩，避免OOM）
     */
    public static Bitmap loadImage(String path, int reqWidth, int reqHeight) {
        // 先获取图片尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // 计算压缩比例
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 加载压缩后的图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 计算图片压缩比例
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}