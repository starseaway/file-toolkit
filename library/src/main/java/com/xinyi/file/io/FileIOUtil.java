package com.xinyi.file.io;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件 IO 工具类
 *
 * <p> 封装常用的输入/输出操作 </p>
 *
 * <p> 包括：</p>
 * <ul>
 *    <li> 1. InputStream 写入文件 </li>
 *    <li> 2. 字节数组/字符串写入文件 </li>
 *    <li> 3. 文件读取为字节数组/字符串 </li>
 *    <li> 4. 文件行读取与写入 </li>
 *    <li> 5. 文件/流拷贝 </li>
 * </ul>
 *
 * @author 新一
 * @since 2025/3/17 10:51
 */
public final class FileIOUtil {

    private FileIOUtil() { }

    /**
     * 将输入流写入文件（不追加）
     *
     * @param filePath 文件路径
     * @param is 输入流
     * @return true: 写入成功；false: 写入失败
     */
    public static boolean writeFileFromIS(String filePath, InputStream is) throws IOException {
        return writeFileFromIS(FileBasicUtil.getFileByPath(filePath), is, false);
    }

    /**
     * 将输入流写入文件
     *
     * @param filePath 文件路径
     * @param is 输入流
     * @param append 是否追加到文件末尾
     * @return true: 写入成功；false: 写入失败
     */
    public static boolean writeFileFromIS(String filePath, InputStream is, boolean append) throws IOException {
        return writeFileFromIS(FileBasicUtil.getFileByPath(filePath), is, append);
    }

    /**
     * 将输入流写入文件（不追加）
     *
     * @param file 文件对象
     * @param is 输入流
     * @return true: 写入成功；false: 写入失败
     */
    public static boolean writeFileFromIS(File file, InputStream is) throws IOException {
        return writeFileFromIS(file, is, false);
    }

    /**
     * 将输入流写入文件
     *
     * @param file 文件对象
     * @param is 输入流
     * @param append 是否追加到文件末尾
     * @return true: 写入成功；false: 写入失败
     */
    public static boolean writeFileFromIS(File file, InputStream is, boolean append) throws IOException {
        if (!FileOperation.createOrExistsFile(file) || is == null) {
            return false;
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            byte[] data = new byte[1024];
            int len;
            while ((len = is.read(data)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } finally {
            is.close();
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * 将字符串写入文件（UTF-8 编码，默认覆盖）
     *
     * @param file 文件对象
     * @param content 字符串
     */
    public static boolean writeFileFromString(File file, String content) throws IOException {
        return writeFileFromString(file, content, true);
    }

    /**
     * 将字符串写入文件，可选择是否覆盖
     *
     * @param file 文件对象
     * @param content 字符串
     * @param isCover 是否覆盖
     */
    public static boolean writeFileFromString(File file, String content, boolean isCover) throws IOException {
        if (file == null || content == null) return false;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, !isCover));
            os.write(content.getBytes(StandardCharsets.UTF_8));
            return true;
        } finally {
            closeIO(os);
        }
    }

    /**
     * 将字节数组写入文件，可选择是否追加
     *
     * @param file 文件对象
     * @param bytes 字节数组
     * @param append 是否追加
     */
    public static boolean writeFileFromBytes(File file, byte[] bytes, boolean append) throws IOException {
        if (file == null || bytes == null) return false;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            os.write(bytes);
            return true;
        } finally {
            closeIO(os);
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param file 文件对象
     * @return 文件内容
     */
    public static byte[] readFileToBytes(File file) throws IOException {
        if (file == null || !file.exists()) throw new FileNotFoundException("文件不存在: " + file);
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[(int) file.length()];
            int readLen = is.read(buffer);
            if (readLen != file.length()) {
                throw new IOException("读取长度与文件长度不匹配");
            }
            return buffer;
        }
    }

    /**
     * 读取文件内容为字符串（UTF-8 编码）
     *
     * @param file 文件对象
     * @return 文件内容
     */
    public static String readFileToString(File file) throws IOException {
        return readFileToString(file, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件内容为字符串（可指定编码）
     *
     * @param file 文件对象
     * @param charset 编码
     * @return 文件内容
     */
    public static String readFileToString(File file, Charset charset) throws IOException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("文件不存在: " + file);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        }
    }

    /**
     * 将文件按行读取到列表中
     *
     * @param file 文件对象
     * @return 文件行列表
     */
    public static List<String> readFileToLines(File file, Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();
        if (file == null || !file.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 拷贝文件（可选择是否覆盖）
     *
     * @param src 源文件
     * @param dest 目标文件
     * @param overwrite 是否覆盖
     * @return true: 拷贝成功；false: 拷贝失败
     */
    public static boolean copyFile(File src, File dest, boolean overwrite) throws IOException {
        if (src == null || !src.exists() || dest == null) return false;
        if (dest.exists() && !overwrite) return false;
        FileOperation.ensureParentDirExists(dest);
        try (InputStream is = new BufferedInputStream(new FileInputStream(src));
             OutputStream os = new BufferedOutputStream(new FileOutputStream(dest))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return true;
        }
    }

    /**
     * 拷贝流数据
     *
     * @param is 输入流
     * @param os 输出流
     * @return true: 拷贝成功；false: 拷贝失败
     */
    public static boolean copy(InputStream is, OutputStream os) throws IOException {
        if (is == null || os == null) return false;
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        return true;
    }

    /**
     * 将字符串列表写入文件（每行一条，UTF-8 编码，覆盖模式）
     *
     * @param file 文件对象
     * @param lines 字符串列表
     */
    public static boolean writeLinesToFile(File file, List<String> lines) throws IOException {
        return writeLinesToFile(file, lines, true, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串列表写入文件，可选择是否覆盖和编码
     *
     * @param file 文件对象
     * @param lines 字符串列表
     * @param isCover 是否覆盖
     * @param charset 编码
     */
    public static boolean writeLinesToFile(File file, List<String> lines, boolean isCover, Charset charset) throws IOException {
        if (file == null || lines == null) return false;
        OutputStream os = null;
        BufferedWriter writer = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, !isCover));
            writer = new BufferedWriter(new OutputStreamWriter(os, charset));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
            return true;
        } finally {
            closeIO(writer, os);
        }
    }


    /**
     * 关闭一个或多个 Closeable 流
     *
     * @param closeables 需要关闭的 Closeable 对象数组
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException exception) {
                    exception.printStackTrace(System.err);
                }
            }
        }
    }
}