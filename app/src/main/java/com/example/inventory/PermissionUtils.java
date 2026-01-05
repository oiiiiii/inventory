package com.example.inventory;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限申请工具类
 */
public class PermissionUtils {
    // 权限请求码
    public static final int REQUEST_CODE_PERMISSIONS = 1001;
    // 需要申请的权限
    public static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * 检查并申请权限
     */
    public static boolean checkAndRequestPermissions(Activity activity) {
        List<String> missingPermissions = new ArrayList<>();
        // 检查每个权限
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        // 申请缺失的权限
        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    missingPermissions.toArray(new String[0]),
                    REQUEST_CODE_PERMISSIONS
            );
            return false;
        }
        return true;
    }

    /**
     * 检查权限申请结果
     */
    public static boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}