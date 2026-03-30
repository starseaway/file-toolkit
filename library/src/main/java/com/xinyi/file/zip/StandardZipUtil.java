package com.xinyi.file.zip;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 基于 java.util.zip 的标准压缩和解压功能，不支持加密
 *
 * @author 新一
 * @since 2023/3/17
 */
public class StandardZipUtil {

    /**
     * 批量压缩多个文件或目录到一个 ZIP 文件中
     *
     * @param resFiles 待压缩的文件或目录集合
     * @param zipFile 生成的 ZIP 文件
     * @param comment ZIP 文件的注释（可为 null 或空字符串）
     * @return {@code true}：压缩成功；{@code false}：压缩失败
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static boolean zipFiles(Collection<File> resFiles, File zipFile, String comment) throws IOException {
        if (resFiles == null || zipFile == null) {
            return false;
        }
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            // 遍历集合中的每个文件或目录
            for (File resFile : resFiles) {
                if (!zipFile(resFile, "", zos, comment)) {
                    return false;
                }
            }
            return true;
        } finally {
            if (zos != null) {
                zos.finish();
                closeIO(zos);
            }
        }
    }

    /**
     * 批量压缩文件（无注释版本）
     *
     * @param resFiles 待压缩的文件或目录集合
     * @param zipFile  生成的 ZIP 文件
     * @return {@code true}：压缩成功；{@code false}：压缩失败
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static boolean zipFiles(Collection<File> resFiles, File zipFile) throws IOException {
        return zipFiles(resFiles, zipFile, null);
    }

    /**
     * 压缩单个文件或目录到 ZIP 文件中
     *
     * @param resFile 待压缩的文件或目录
     * @param zipFile 生成的 ZIP 文件
     * @param comment ZIP 文件的注释（可为 null 或空字符串）
     * @return {@code true}：压缩成功；{@code false}：压缩失败
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static boolean zipFile(File resFile, File zipFile, String comment) throws IOException {
        if (resFile == null || zipFile == null) {
            return false;
        }
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            return zipFile(resFile, "", zos, comment);
        } finally {
            if (zos != null) {
                zos.finish();
                closeIO(zos);
            }
        }
    }

    /**
     * 压缩单个文件或目录（无注释版本）
     *
     * @param resFile 待压缩的文件或目录
     * @param zipFile 生成的 ZIP 文件
     * @return {@code true}：压缩成功；{@code false}：压缩失败
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static boolean zipFile(File resFile, File zipFile) throws IOException {
        return zipFile(resFile, zipFile, null);
    }

    /**
     * 递归压缩文件或目录到指定的 ZipOutputStream 中
     *
     * @param resFile  待压缩的文件或目录
     * @param rootPath 相对于 ZIP 根目录的路径
     * @param zos      Zip 输出流
     * @param comment  ZIP 文件注释（可为 null或空字符串）
     * @return {@code true}：压缩成功；{@code false}：压缩失败
     * @throws IOException 当 IO 出错时抛出异常
     */
    private static boolean zipFile(File resFile, String rootPath, ZipOutputStream zos, String comment) throws IOException {
        // 更新当前路径
        String currentPath = rootPath + (rootPath.isEmpty() ? "" : File.separator) + resFile.getName();
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            if (fileList == null || fileList.length == 0) {
                // 空文件夹：添加一个目录条目
                ZipEntry entry = new ZipEntry(currentPath + "/");
                if (comment != null && !comment.isEmpty()) {
                    entry.setComment(comment);
                }
                zos.putNextEntry(entry);
                zos.closeEntry();
            } else {
                // 遍历子文件或目录
                for (File file : fileList) {
                    if (!zipFile(file, currentPath, zos, comment)) {
                        return false;
                    }
                }
            }
        } else {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(resFile));
                ZipEntry entry = new ZipEntry(currentPath);
                if (comment != null && !comment.isEmpty()) {
                    entry.setComment(comment);
                }
                zos.putNextEntry(entry);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            } finally {
                closeIO(is);
            }
        }
        return true;
    }

    /**
     * 解压 ZIP 文件到当前目录
     */
    public static boolean unzipFile(String zipFile) throws IOException {
        File file = new File(zipFile);
        return unzipFile(file, file.getParentFile());
    }

    /**
     * 解压 ZIP 文件到当前目录
     */
    public static boolean unzipFile(File zipFile) throws IOException {
        return unzipFile(zipFile, zipFile.getParentFile());
    }

    /**
     * 解压 ZIP 文件到指定目录
     *
     * @param zipFile ZIP 文件对象
     * @param destDir 目标解压目录
     * @return {@code true}：解压成功；{@code false}：解压失败
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static boolean unzipFile(File zipFile, File destDir) throws IOException {
        if (zipFile == null || destDir == null) {
            return false;
        }
        // 确保目标目录存在
        if (!destDir.exists() && !destDir.mkdirs()) {
            return false;
        }
        ZipFile zf = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File outFile = new File(destDir, entry.getName());
            if (entry.isDirectory()) {
                if (!outFile.exists() && !outFile.mkdirs()) {
                    return false;
                }
            } else {
                // 确保父目录存在
                File parent = outFile.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    return false;
                }
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new BufferedInputStream(zf.getInputStream(entry));
                    os = new BufferedOutputStream(new FileOutputStream(outFile));
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                } finally {
                    closeIO(is, os);
                }
            }
        }
        return true;
    }

    /**
     * 批量解压多个 ZIP 文件到指定目录
     *
     * @param zipFiles ZIP 文件集合
     * @param destDir  目标解压目录
     * @return {@code true}：全部解压成功；{@code false}：任一文件解压失败
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static boolean unzipFiles(Collection<File> zipFiles, File destDir) throws IOException {
        if (zipFiles == null || destDir == null) {
            return false;
        }
        for (File zipFile : zipFiles) {
            if (!unzipFile(zipFile, destDir)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取 ZIP 文件中的所有文件路径列表
     *
     * @param zipFile ZIP 文件对象
     * @return 包含 ZIP 内所有文件路径的列表
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static List<String> getFilesPath(File zipFile) throws IOException {
        List<String> paths = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = getEntries(zipFile);
        while (entries.hasMoreElements()) {
            paths.add(entries.nextElement().getName());
        }
        return paths;
    }

    /**
     * 获取 ZIP 文件中的所有注释列表
     *
     * @param zipFile ZIP 文件对象
     * @return 包含 ZIP 内所有文件注释的列表
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static List<String> getComments(File zipFile) throws IOException {
        List<String> comments = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = getEntries(zipFile);
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            comments.add(entry.getComment());
        }
        return comments;
    }

    /**
     * 获取 ZIP 文件中的 ZipEntry 枚举对象
     *
     * @param zipFile ZIP 文件对象
     * @return ZipEntry 枚举
     * @throws IOException 当 IO 出错时抛出异常
     */
    public static Enumeration<? extends ZipEntry> getEntries(File zipFile) throws IOException {
        if (zipFile == null) {
            return null;
        }
        try (ZipFile tryZipFile = new ZipFile(zipFile)) {
            return tryZipFile.entries();
        }
    }

    /**
     * 关闭一个或多个 Closeable 流
     *
     * @param closeables 需要关闭的 Closeable 对象数组
     */
    private static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    Log.e(StandardZipUtil.class.getSimpleName(), "closeIO: ", e);
                }
            }
        }
    }
}