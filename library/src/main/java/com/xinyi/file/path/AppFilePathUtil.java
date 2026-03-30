package com.xinyi.file.path;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.Manifest;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.io.File;

/**
 * 获取应用文件路径的工具类
 *
 * <p> 本类提供了获取应用内外部存储、缓存目录、数据库目录等多种路径的方法 </p>
 *
 * <p> 注意：某些路径需要在 Android 6.0 及以上版本中申请相应权限 </p>
 *
 * @author 新一
 * @since 2025/3/18 10:13
 */
public class AppFilePathUtil {

    /**
     * 获取路径：/data/user/0/应用包名/files
     * 该目录是应用的文件存储目录，应用被卸载时，该目录一同被系统删除。
     * 不会因为系统内存不足而被清空。
     * 默认存在，默认具备读写权限（6.0系统可以不用向用户申请）。
     */
    public static String getFileDir(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    /**
     * 获取路径：/data/user/0/应用包名/cache
     * 该目录是应用的文件缓存目录，应用被卸载时，该目录一同被系统删除。
     * 在系统内存紧张时，系统会自动清理该目录下的文件。
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     */
    public static String getCacheDir(Context context) {
        return context.getCacheDir().getAbsolutePath();
    }

    /**
     * 获取路径：/storage/emulated/0/Android/obb/应用包名
     * 该目录通常用于存放游戏数据包等OBB文件。
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     */
    public static String getObbDir(Context context) {
        return context.getObbDir().getAbsolutePath();
    }

    /**
     * 获取路径：/data/user/0/应用包名/code_cache
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String getCodeCacheDir(Context context) {
        return context.getCodeCacheDir().getAbsolutePath();
    }

    /**
     * 获取路径：/storage/emulated/0/Android/data/应用包名/files/Download
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     *
     * @param pathName 文件夹名称（例如 Environment.DIRECTORY_DOWNLOADS）
     */
    @Nullable
    public static String getExternalFilesDir(Context context, String pathName) {
        File externalFilesDir = context.getExternalFilesDir(pathName);
        if (externalFilesDir == null) {
            return null;
        }
        return externalFilesDir.getAbsolutePath();
    }

    /**
     * 获取路径：/storage/emulated/0/Android/data/应用包名/cache
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     */
    @Nullable
    public static String getExternalCacheDir(Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) {
            return null;
        }
        return externalCacheDir.getAbsolutePath();
    }

    /**
     * 获取路径：/data/user/0/应用包名/databases/文件名
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     *
     * @param pathName 数据库文件名
     */
    public static String getDatabasePath(Context context, String pathName) {
        return context.getDatabasePath(pathName).getAbsolutePath();
    }

    /**
     * 获取路径：/data/user/0/应用包名/app_文件名
     *
     * <p> 默认存在，可读写（6.0系统可以不用向用户申请） </p>
     *
     * @param dirName 子目录名
     * @param mode 模式（例如 Context.MODE_PRIVATE）
     */
    public static String getDir(Context context, String dirName, int mode) {
        return context.getDir(dirName, mode).getAbsolutePath();
    }

    /**
     * 获取路径：/data/app/应用包名-1/base.apk
     * 获取应用的 APK 文件路径。
     */
    public static String getPackageCodePath(Context context) {
        return context.getPackageCodePath();
    }

    /**
     * 获取路径：/storage/emulated/0
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     */
    public static String getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 获取路径：/storage/emulated/0/Download（以下载目录为例）
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     *
     * @param pathName 文件夹名称
     */
    public static String getExternalStoragePublicDirectory(String pathName) {
        return Environment.getExternalStoragePublicDirectory(pathName).getAbsolutePath();
    }

    /**
     * 获取路径：/data/cache
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     */
    public static String getDownloadCacheDirectory() {
        return Environment.getDownloadCacheDirectory().getAbsolutePath();
    }

    /**
     * 获取路径：/data/user/应用包名/files/download
     * 该目录是应用的文件存储目录，应用被卸载时，该目录一同被系统删除。
     *
     * <p> 默认不存在，可读写（6.0系统可以不用向用户申请） </p>
     *
     * @param pathName 文件夹名称
     */
    public static String getFileStreamPath(Context context, String pathName) {
        return context.getFileStreamPath(pathName).getAbsolutePath();
    }

    /**
     * 检查是否具有外部存储的读写权限
     * @param context 上下文
     * @return 是否具有外部存储的读写权限
     */
    public static boolean hasExternalStoragePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 获取路径：/storage/emulated/0/Music
     * 获取外部存储中的音乐文件夹路径
     */
    public static String getExternalStorageMusicDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
    }

    /**
     * 获取路径：/storage/emulated/0/Pictures
     * 获取外部存储中的图片文件夹路径
     */
    public static String getExternalStoragePicturesDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    }

    /**
     * 获取路径：/storage/emulated/0/Movies
     * 获取外部存储中的视频文件夹路径
     */
    public static String getExternalStorageMoviesDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
    }
}