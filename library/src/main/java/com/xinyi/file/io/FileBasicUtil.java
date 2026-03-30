package com.xinyi.file.io;

import androidx.annotation.Nullable;

import java.io.File;

/**
 * 基础文件工具类，提供文件对象获取、存在性检查、类型判断和扩展名获取等功能
 *
 * @author 新一
 * @since 2025/3/17 10:09
 */
public final class FileBasicUtil {

    private FileBasicUtil() { }

    /**
     * 根据文件路径获取 File 对象；若路径为空或全为空白，则返回 null
     *
     * @param filePath 文件路径
     * @return File 对象或 null
     */
    public static File getFileByPath(String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * 判断文件是否存在（传入 File 对象）
     *
     * @param file 文件对象
     * @return true 存在，false 不存在
     */
    public static boolean isFileExists(File file) {
        return file != null && file.exists();
    }

    /**
     * 判断文件是否存在（传入路径）
     *
     * @param filePath 文件路径
     * @return true 存在，false 不存在
     */
    public static boolean isFileExists(String filePath) {
        return isFileExists(getFileByPath(filePath));
    }

    /**
     * 判断是否为目录（传入 File 对象）
     *
     * @param file 文件对象
     * @return true 为目录，false 否
     */
    public static boolean isDir(File file) {
        return isFileExists(file) && file.isDirectory();
    }

    /**
     * 判断是否为目录（传入路径）
     *
     * @param dirPath 目录路径
     * @return true 为目录，false 否
     */
    public static boolean isDir(String dirPath) {
        return isDir(getFileByPath(dirPath));
    }

    /**
     * 判断是否为文件（传入 File 对象）
     *
     * @param file 文件对象
     * @return true 为文件，false 否
     */
    public static boolean isFile(File file) {
        return isFileExists(file) && file.isFile();
    }

    /**
     * 判断是否为文件（传入路径）
     *
     * @param filePath 文件路径
     * @return true 为文件，false 否
     */
    public static boolean isFile(String filePath) {
        return isFile(getFileByPath(filePath));
    }

    /**
     * 是否隐藏文件
     *
     * @param file 文件
     */
    public static boolean isHidden(File file) {
        try {
            return file.isHidden() || file.getName().startsWith(".");
        } catch (Throwable throwable) {
            return false;
        }
    }

    /**
     * 判断字符串是否为 null 或全为空白字符
     *
     * @param s 待判断字符串
     * @return true: null 或全空白；false: 否
     */
    public static boolean isSpace(String s) {
        if (s == null) {
            return true;
        }
        for (int i = 0, len = s.length(); i < len; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取文件扩展名（不包含点）
     *
     * @param filename 文件名
     * @return 扩展名字符串，若无返回空字符串
     */
    public static String getExtensionName(String filename) {
        if (filename != null && !filename.isEmpty()) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1 && dot < filename.length() - 1) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    /**
     * 返回不带扩展名的文件名称
     *
     * @param filePath 文件路径
     * @return 文件名
     */
    @Nullable
    public static String getFileNameNoExtension(final String filePath) {
        if (isSpace(filePath)) {
            return null;
        }
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }
}