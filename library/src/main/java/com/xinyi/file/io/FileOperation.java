package com.xinyi.file.io;

import static com.xinyi.file.io.FileBasicUtil.isSpace;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 文件操作类，提供文件的创建、重命名和删除等常见操作
 *
 * @author 新一
 * @since 2025/3/17 11:01
 */
public final class FileOperation {

    private FileOperation() { }

    /**
     * 创建一个文件
     *
     * @param path 文件路径
     * @return true: 文件存在或创建成功；false: 创建失败
     */
    public static boolean createFile(String path) throws IOException {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        return createFile(file);
    }

    /**
     * 创建一个文件
     *
     * @param file 文件对象
     */
    public static boolean createFile(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException exception) {
            throw new IOException("创建文件失败: " + exception.getMessage(), exception);
        }
    }

    /**
     * 如果目录不存在，请创建一个目录，否则什么都不做。
     *
     * @param path 文件路径
     */
    public static boolean createOrExistsDir(String path) {
        if (path == null) {
            return false;
        }
        File dir = new File(path);
        return dir.exists() ? dir.isDirectory() : dir.mkdirs();
    }

    /**
     * 如果目录不存在，请创建一个目录，否则什么都不做。
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 确保指定路径的文件或文件夹存在，如果不存在则创建。
     *
     * @param path 文件或文件夹的路径
     * @return true 如果路径已存在或成功创建，false 如果创建失败
     */
    public static boolean ensurePathExists(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("路径不能为空");
        }

        File file = new File(path);

        if (file.exists()) {
            return true; // 路径已存在，直接返回
        }

        // **这里的关键改动**
        if (path.endsWith("/") || path.endsWith(File.separator)) {
            // 如果路径以 / 结尾，假定它是文件夹
            return file.mkdirs();
        } else {
            // 假定路径是文件
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                return false; // 创建父级目录失败
            }
            return file.createNewFile(); // 创建文件
        }
    }

    /**
     * 判断文件是否存在，若不存在则尝试创建
     *
     * @param path 文件路径
     */
    public static boolean createOrExistsFile(String path) throws IOException {
        return createOrExistsFile(new File(path));
    }

    /**
     * 判断文件是否存在，若不存在则尝试创建
     *
     * @param file 文件对象
     * @return true: 文件存在或创建成功；false: 不存在或创建失败
     */
    public static boolean createOrExistsFile(File file) throws IOException {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return file.isFile();
        }
        return createFile(file);
    }

    /**
     * 根据当前日期和文件序号生成新的文件，确保文件名不重复。
     *
     * <p>
     *   该方法生成一个新文件，并根据当前日期和文件序号来确保文件名唯一。文件后缀可以自定义。
     *   它会首先创建一个以当前日期为名称的子目录，如果该目录不存在，则会创建该目录。然后，它会
     *   依次尝试生成文件名，直到找到一个不存在的文件名为止。
     * </p>
     *
     * @param dirPath 文件存放的目录。
     * @param filePrefix 文件名前缀，用于区分不同文件名。
     * @param fileExtension 文件的后缀（扩展名），如 ".txt", ".log" 等。
     * @return 新的文件对象。
     * @throws IOException 如果创建文件失败。
     */
    public static File createDateNewFile(String dirPath, String filePrefix, String fileExtension) throws IOException {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
        File dateDir = new File(dirPath, date); // 获取当天的子目录
        if (!dateDir.exists() && !dateDir.mkdirs()) {
            throw new IOException("无法创建目录: " + dateDir.getAbsolutePath());
        }

        int index = 1; // 从1开始
        File newFile;
        do {
            // 生成文件名，格式为：文件前缀_日期_序号.后缀
            String fileName = String.format(Locale.CHINA, "%s_%s_%d%s", filePrefix, date, index, fileExtension);
            newFile = new File(dateDir, fileName);
            index++;
        } while (newFile.exists()); // 递增索引，直到找到不存在的文件名

        if (!newFile.createNewFile()) {
            throw new IOException("无法创建文件: " + newFile.getAbsolutePath());
        }

        return newFile;
    }

    /**
     * 创建并清空目标目录
     *
     * @param dirPath 目标目录路径
     * @throws IOException 当创建或清空目录失败时抛出异常
     */
    public static void createAndCleanDir(String dirPath) throws IOException {
        // 校验路径非空
        validatePath(dirPath, "目录路径为空");
        File dir = new File(dirPath);
        if (dir.exists()) {
            // 递归删除目录下的所有文件和子目录
            deleteRecursively(dir);
            // 重新创建目录
            if (!dir.mkdirs()) {
                throw new IOException("无法重新创建目录：" + dirPath);
            }
        } else {
            // 直接创建目录（包括必要的父目录）
            if (!dir.mkdirs()) {
                throw new IOException("无法创建目录：" + dirPath);
            }
        }
    }

    /**
     * 创建并清空目标文件
     *
     * @param filePath 目标文件路径
     * @throws IOException 当创建或清空文件失败时抛出异常
     */
    public static void createAndCleanFile(String filePath) throws IOException {
        // 校验路径非空
        validatePath(filePath, "文件路径为空");
        File file = new File(filePath);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("无法删除文件：" + filePath);
            }
        }
        // 确保父目录存在
        ensureParentDirExists(file);
        if (!file.createNewFile()) {
            throw new IOException("无法创建文件：" + filePath);
        }
    }

    /**
     * 校验给定的路径是否为空或全空白字符，如果为空则抛出 IllegalArgumentException
     *
     * @param path        路径字符串
     * @param errorMsg    错误信息
     */
    public static void validatePath(String path, String errorMsg) {
        if (isSpace(path)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * 确保指定文件的父目录存在，如果不存在则创建
     *
     * @param file 文件
     * @throws IOException 无法创建父目录时抛出异常
     */
    public static void ensureParentDirExists(File file) throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("无法创建父目录：" + parentDir.getAbsolutePath());
            }
        }
    }

    /**
     * 删除文件，并在父目录为空时删除父目录
     */
    public static void deleteFileAndParentDirectory(String filePath) throws IOException {
        File file = new File(filePath);
        // 先删除日志文件
        deleteRecursively(file);

        // 获取父目录
        File parentFile = file.getParentFile();
        if (parentFile != null && parentFile.isDirectory()) {
            String[] files = parentFile.list(); // 可能返回 null
            if (files != null && files.length == 0) {
                // 只有当父目录为空时才删除
                FileOperation.deleteRecursively(parentFile);
            }
        }
    }

    /**
     * 递归删除指定文件或目录。如果是目录，则删除其下所有内容后再删除目录本身。
     *
     * @param file 文件或目录
     * @throws IOException 删除失败时抛出异常
     */
    public static void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("无法删除：" + file.getAbsolutePath());
        }
    }

    /**
     * 重命名文件或文件夹
     *
     * @param sourcePath 源路径+文件名
     * @param goalPath 目标路径+文件名
     * @return true: 重命名成功；false: 失败
     */
    public static boolean rename(String sourcePath, String goalPath) {
        File source = FileBasicUtil.getFileByPath(sourcePath);
        File goal = FileBasicUtil.getFileByPath(goalPath);
        if (source == null || goal == null || !source.exists()) {
            return false;
        }
        return source.renameTo(goal);
    }

    /**
     * 删除文件（非目录）
     *
     * @param filePath 路径+文件名
     * @return true: 删除成功；false: 删除失败
     */
    public static boolean deleteFile(String filePath) {
        File file = FileBasicUtil.getFileByPath(filePath);
        return deleteFile(file);
    }

    /**
     * 删除文件（非目录）
     *
     * @param file 文件对象
     * @return true: 删除成功；false: 删除失败
     */
    public static boolean deleteFile(File file) {
        return file != null && file.exists() && file.isFile() && file.delete();
    }

    /**
     * 删除文件或目录（递归删除目录下所有文件）
     *
     * @param file 文件或目录
     * @return true: 删除成功；false: 删除失败
     */
    public static boolean deleteFileOrDirectory(File file) {
        if (file == null || !file.exists()) return false;
        if (file.isFile()) return file.delete();
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles != null) {
                for (File child : childFiles) {
                    deleteFileOrDirectory(child);
                }
            }
            return file.delete();
        }
        return false;
    }
}