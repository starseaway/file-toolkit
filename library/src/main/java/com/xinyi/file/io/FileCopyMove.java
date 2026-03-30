package com.xinyi.file.io;

import android.util.Log;
import java.io.*;

/**
 * 文件复制与移动工具类，提供文件/目录的复制与移动操作
 *
 * @author 新一
 * @since 2025/3/17 10:31
 */
public final class FileCopyMove {

    private static final String TAG = FileCopyMove.class.getSimpleName();

    private FileCopyMove() { }

    /**
     * 内部基础方法：复制或移动文件或目录
     *
     * @param srcFile 源文件或目录
     * @param destFile 目标文件或目录
     * @param isMove 是否为移动操作（true：移动，false：复制）
     * @return true: 操作成功；false: 操作失败
     */
    public static boolean copyOrMove(File srcFile, File destFile, boolean isMove) {
        if (srcFile == null || destFile == null) {
            return false;
        }
        if (!srcFile.exists()) {
            return false;
        }
        if (srcFile.isDirectory()) {
            return copyOrMoveDir(srcFile, destFile, isMove);
        } else {
            return copyOrMoveFile(srcFile, destFile, isMove);
        }
    }

    /**
     * 复制或移动目录（递归处理子文件）
     *
     * @param srcDir 源目录
     * @param destDir 目标目录
     * @param isMove 是否为移动操作
     * @return true: 成功；false: 失败
     */
    public static boolean copyOrMoveDir(File srcDir, File destDir, boolean isMove) {
        if (srcDir == null || destDir == null) {
            return false;
        }
        String srcPath = srcDir.getAbsolutePath() + File.separator;
        String destPath = destDir.getAbsolutePath() + File.separator;
        if (destPath.contains(srcPath)) {
            Log.e(TAG, "Destination directory is inside the source directory.");
            return false;
        }
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }
        if (!destDir.exists() && !destDir.mkdirs()) {
            return false;
        }

        File[] files = srcDir.listFiles();
        if (files != null) {
            for (File file : files) {
                File target = new File(destPath + file.getName());
                if (file.isFile()) {
                    if (!copyOrMoveFile(file, target, isMove)) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    if (!copyOrMoveDir(file, target, isMove)) {
                        return false;
                    }
                }
            }
        }
        if (isMove) {
            return FileOperation.deleteFileOrDirectory(srcDir);
        }
        return true;
    }

    /**
     * 复制或移动文件
     *
     * @param srcFile 源文件
     * @param destFile 目标文件
     * @param isMove 是否为移动操作
     * @return true: 成功；false: 失败
     */
    public static boolean copyOrMoveFile(File srcFile, File destFile, boolean isMove) {
        if (srcFile == null || destFile == null) {
            return false;
        }
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        if (destFile.exists()) return false;
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            return false;
        }
        boolean result;
        try (InputStream is = new FileInputStream(srcFile)) {
            result = FileIOUtil.writeFileFromIS(destFile, is, false);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (result && isMove) {
            return FileOperation.deleteFile(srcFile.getAbsolutePath());
        }
        return result;
    }

    /**
     * 复制目录（传入路径）
     *
     * @param srcDirPath 源目录路径
     * @param destDirPath 目标目录路径
     * @return true: 复制成功；false: 复制失败
     */
    public static boolean copyDir(String srcDirPath, String destDirPath) {
        File srcDir = FileBasicUtil.getFileByPath(srcDirPath);
        File destDir = FileBasicUtil.getFileByPath(destDirPath);
        return copyOrMove(srcDir, destDir, false);
    }

    /**
     * 复制文件（传入路径）
     *
     * @param srcFilePath 源文件路径
     * @param destFilePath 目标文件路径
     * @return true: 复制成功；false: 复制失败
     */
    public static boolean copyFile(String srcFilePath, String destFilePath) {
        File srcFile = FileBasicUtil.getFileByPath(srcFilePath);
        File destFile = FileBasicUtil.getFileByPath(destFilePath);
        return copyOrMove(srcFile, destFile, false);
    }

    /**
     * 移动目录（传入路径）
     *
     * @param srcDirPath 源目录路径
     * @param destDirPath 目标目录路径
     * @return true: 移动成功；false: 移动失败
     */
    public static boolean moveDir(String srcDirPath, String destDirPath) {
        File srcDir = FileBasicUtil.getFileByPath(srcDirPath);
        File destDir = FileBasicUtil.getFileByPath(destDirPath);
        return copyOrMove(srcDir, destDir, true);
    }

    /**
     * 移动文件（传入路径）
     *
     * @param srcFilePath 源文件路径
     * @param destFilePath 目标文件路径
     * @return true: 移动成功；false: 移动失败
     */
    public static boolean moveFile(String srcFilePath, String destFilePath) {
        File srcFile = FileBasicUtil.getFileByPath(srcFilePath);
        File destFile = FileBasicUtil.getFileByPath(destFilePath);
        return copyOrMove(srcFile, destFile, true);
    }
}