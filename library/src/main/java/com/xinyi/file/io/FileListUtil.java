package com.xinyi.file.io;

import com.xinyi.file.entity.FileEntity;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 文件列表工具类，提供读取指定目录下的所有文件和文件夹信息以及文本行读取功能
 *
 * @author 新一
 * @since 2025/3/17 16:11
 */
public final class FileListUtil {

    /** 标准的日期格式 */
    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String TAG = FileListUtil.class.getSimpleName();

    private FileListUtil() { }

    /**
     * 获取指定文件夹中所有指定类型的文件。
     *
     * @param folder 需要检查的文件夹（可以是文件夹或单个文件）
     * @param fileExtension 需要筛选的文件后缀（如 ".jar", ".zip" 等）
     * @return 包含所有指定类型文件的列表，如果没有找到符合条件的文件则返回空列表
     */
    public static List<File> getFilesWithExtension(File folder, String fileExtension) {
        List<File> filesWithExtension = new ArrayList<>();

        // 检查 folder 是否存在且是有效的文件夹或文件
        if (folder != null && folder.exists()) {
            if (folder.isDirectory()) {
                // 获取目录中的所有文件
                File[] files = folder.listFiles();
                if (files != null) {
                    // 遍历目录中的文件，筛选出符合扩展名条件的文件
                    for (File file : files) {
                        if (file.getName().endsWith(fileExtension)) {
                            filesWithExtension.add(file);
                        }
                    }
                }
            } else if (folder.getName().endsWith(fileExtension)) {
                // 如果传入的是单个文件，并且符合扩展名条件，则直接添加到列表中
                filesWithExtension.add(folder);
            }
        }
        return filesWithExtension;
    }

    /**
     * 获取指定目录下符合后缀名条件的文件列表
     *
     * @param filePath 目录路径
     * @param fileExtension 文件后缀（例如 ".txt", ".java"）
     * @return 符合条件的文件路径列表 （不包含隐藏文件）
     */
    public static List<String> getFileList(String filePath, String fileExtension) {
        List<String> fileList = new ArrayList<>();
        File dir = FileBasicUtil.getFileByPath(filePath);
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.isHidden() && f.isFile() && f.getName().endsWith(fileExtension)) {
                        fileList.add(f.getAbsolutePath());
                    }
                }
            }
        }
        return fileList;
    }

    /**
     * 获取指定目录及其子目录中的所有文件和文件夹
     *
     * @param directoryPath 目录路径
     * @return 文件列表（包含目录和子目录下的所有文件）
     */
    public static List<File> getFilesInDir(File directoryPath) {
        List<File> fileList = new ArrayList<>();
        if (directoryPath == null || !directoryPath.exists()) {
            return fileList;
        }
        File[] files = directoryPath.listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.add(file);
                if (file.isDirectory()) {
                    fileList.addAll(getFilesInDir(file));
                }
            }
        }
        return fileList;
    }

    /**
     * 获取指定目录下的所有文件和文件夹信息列表。
     *
     * @param directoryPath 目录路径
     * @param includeHidden 是否包含隐藏文件或隐藏文件夹
     * @return 包含所有文件和文件夹的实体列表；如果目录无效，则返回空列表
     */
    public static List<FileEntity> getAllFileAndDirectoryList(String directoryPath, boolean includeHidden) throws IOException {
        List<FileEntity> entities = new ArrayList<>();
        File[] files = getFilesFromDirectory(directoryPath);
        for (File file : files) {
            // 如果为隐藏项且不包含隐藏文件，则跳过
            if (file.isHidden() && !includeHidden) {
                continue;
            }
            entities.add(convertToFileEntity(file));
        }
        return entities;
    }

    /**
     * 获取指定目录下的文件夹信息列表（封装为 FileEntity 对象）。
     *
     * @param directoryPath 目录路径
     * @param includeHidden 是否包含隐藏文件夹
     * @return 文件实体列表；若无或无效目录则返回空列表
     */
    public static List<FileEntity> getAllDirectoryList(String directoryPath, boolean includeHidden) throws IOException {
        List<FileEntity> entities = new ArrayList<>();
        File[] files = getFilesFromDirectory(directoryPath);
        for (File file : files) {
            // 只处理文件夹
            if (!file.isDirectory()) {
                continue;
            }
            if (file.isHidden() && !includeHidden) {
                continue;
            }
            entities.add(convertToFileEntity(file));
        }
        return entities;
    }

    /**
     * 获取指定目录下的所有文件信息列表。
     *
     * @param directoryPath 目录路径
     * @param includeHidden 是否包含隐藏文件
     * @return 仅包含文件的实体列表；如果目录无效，则返回空列表
     */
    public static List<FileEntity> getAllFileList(String directoryPath, boolean includeHidden) throws IOException {
        List<FileEntity> entities = new ArrayList<>();
        File[] files = getFilesFromDirectory(directoryPath);
        for (File file : files) {
            // 只处理文件
            if (!file.isFile()) {
                continue;
            }
            if (file.isHidden() && !includeHidden) {
                continue;
            }
            entities.add(convertToFileEntity(file));
        }
        return entities;
    }

    /**
     * 根据后缀获取指定目录下的所有文件信息列表。(不包含隐藏文件)
     *
     * @param directoryPath 目录路径
     * @param fileExtension 文件后缀（例如 ".txt", ".java"）
     */
    public static List<FileEntity> getAllFileListByExtension(String directoryPath, String fileExtension) throws IOException {
        List<FileEntity> entities = new ArrayList<>();
        File[] files = getFilesFromDirectory(directoryPath);
        for (File file : files) {
            // 只处理文件
            if (!file.isFile() || file.isHidden()) {
                continue;
            }
            if (!file.getName().endsWith(fileExtension)) {
                continue;
            }
            entities.add(convertToFileEntity(file));
        }
        return entities;
    }

    /**
     * 检查指定路径是否为有效目录，并返回对应的 File 对象。
     *
     * @param directoryPath 目录路径
     * @return 如果目录路径有效，则返回对应的 File 对象；否则返回 null，并记录错误日志
     */
    public static File getValidDirectory(String directoryPath) throws IOException {
        File dir = FileBasicUtil.getFileByPath(directoryPath);
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            throw new IOException("无效的目录路径: " + directoryPath);
        }
        return dir;
    }

    /**
     * 获取指定目录下所有的文件和文件夹（不进行任何过滤）。
     *
     * @param directoryPath 目录路径
     * @return 如果目录有效，则返回该目录下所有文件和文件夹的数组；否则返回空数组
     */
    public static File[] getFilesFromDirectory(String directoryPath) throws IOException {
        File dir = getValidDirectory(directoryPath);
        File[] files = dir.listFiles();
        return files != null ? files : new File[0];
    }

    /**
     * 将 File 对象转换为 FileEntity 实体对象。
     */
    public static FileEntity convertToFileEntity(File file) {
        return convertToFileEntity(file, new SimpleDateFormat(DATE_FORMAT, Locale.CHINA));
    }

    /**
     * 将 File 对象转换为 FileEntity 实体对象。
     *
     * @param file 文件对象
     * @param sdf  日期格式化对象
     * @return 对应的 FileEntity 对象
     */
    public static FileEntity convertToFileEntity(File file, SimpleDateFormat sdf) {
        long size = file.isDirectory() ? FileSizeUtil.getFileSizes(file) : file.length();
        String lastModified = sdf.format(new Date(file.lastModified()));
        return new FileEntity(file.getName(), size, file.getAbsolutePath(), lastModified, file.isDirectory());
    }
}