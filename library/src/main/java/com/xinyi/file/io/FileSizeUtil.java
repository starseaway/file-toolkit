package com.xinyi.file.io;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 文件大小工具类，计算文件/目录的大小并进行格式化输出
 *
 * @author 新一
 * @since 2025/3/17 15:11
 */
public final class FileSizeUtil {

    private FileSizeUtil() { }

    /**
     * 递归计算目录下所有文件的大小（单位：字节）
     *
     * @param file 文件或目录
     * @return 总字节数
     */
    public static long getFileSizes(File file) {
        if (file == null || !file.exists()) return 0;
        long size = 0;
        if (file.isFile()) {
            return file.length();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    size += getFileSizes(f);
                }
            }
        }
        return size;
    }

    /**
     * 格式化文件大小，自动转换单位（B, KB, MB, GB）
     *
     * @param size 字节数
     * @return 格式化后的字符串
     */
    public static String formatFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (size < 1024) {
            return df.format((double) size) + "B";
        } else if (size < 1048576) {
            return df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            return df.format((double) size / 1048576) + "MB";
        } else {
            return df.format((double) size / 1073741824) + "GB";
        }
    }

    /**
     * 获取指定文件的格式化大小（传入文件路径）
     *
     * @param filePath 文件路径
     * @return 格式化后的大小字符串
     */
    public static String getFileFormatSize(String filePath) {
        File file = FileBasicUtil.getFileByPath(filePath);
        if (file == null || !file.exists() || !file.isFile()) return "0B";
        return formatFileSize(file.length());
    }

    /**
     * 获取指定目录的格式化大小（传入目录路径）
     *
     * @param directoryPath 目录路径
     * @return 格式化后的大小字符串
     */
    public static String getDirectoryFormatSize(String directoryPath) {
        File dir = FileBasicUtil.getFileByPath(directoryPath);
        long size = getFileSizes(dir);
        return formatFileSize(size);
    }
}
