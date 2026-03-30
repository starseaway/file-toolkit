package com.xinyi.file.io;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 资源文件访问工具类
 *
 * <p>
 *   提供访问应用内 assets 和 raw 目录的资源文件的功能。
 *   支持复制 assets 资源到 filesDir 或 cacheDir 目录。
 * </p>
 *
 * @author 新一
 * @since 2025/3/25 14:17
 */
public class ResourceUtil {

    /**
     * 从 assets 目录复制文件到目标目录
     *
     * @param context 应用上下文
     * @param assetName assets 文件名
     * @param targetDir 目标目录（例如：context.getFilesDir()）
     * @return 目标文件对象
     * @throws IOException 复制失败时抛出异常
     */
    public static boolean copyAssetToDir(Context context, String assetName, File targetDir) throws IOException {
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("无法创建目标目录: " + targetDir.getAbsolutePath());
        }
        File outFile = new File(targetDir, assetName);
        if (!outFile.exists()) {
            try (InputStream is = context.getAssets().open(assetName);
                 FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
            }
        }
        return true;
    }

    /**
     * 读取 assets 目录下的文本文件内容
     *
     * @param context 应用上下文
     * @param fileName assets 目录下的文件名
     * @return 文件内容字符串
     * @throws IOException 读取失败时抛出异常
     */
    public static String readAssetText(Context context, String fileName) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStream is = context.getAssets().open(fileName)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                builder.append(new String(buffer, 0, length));
            }
        }
        return builder.toString();
    }

    /**
     * 读取 raw 资源文件内容
     *
     * @param context 应用上下文
     * @param resId raw 资源 ID
     * @return 文件内容字符串
     * @throws IOException 读取失败时抛出异常
     */
    public static String readRawText(Context context, int resId) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStream is = context.getResources().openRawResource(resId)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                builder.append(new String(buffer, 0, length));
            }
        }
        return builder.toString();
    }

    /**
     * 检查 assets 目录下的文件是否存在
     *
     * @param context 应用上下文
     * @param fileName assets 目录下的文件名
     * @return 如果文件存在返回 true，否则返回 false
     */
    public static boolean isAssetExists(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        try (InputStream ignored = assetManager.open(fileName)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}