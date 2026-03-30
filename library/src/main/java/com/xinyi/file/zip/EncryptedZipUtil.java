package com.xinyi.file.zip;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于 Zip4j 2.x 最新版本提供加密压缩、分卷压缩和带进度回调的解压操作
 *
 * <p> 支持：</p>
 * <ul>
 *     <li> 1. 普通加密压缩（压缩文件或目录，可选择是否保留目录结构） </li>
 *     <li> 2. 分卷压缩（支持设置分卷大小，单位为字节）</li>
 *     <li> 3. 解压操作（支持加密包解压，并通过 Handler 回调解压进度） </li>
 * </ul>
 *
 * @author 新一
 * @since 2023/4/20 16:26
 */
public class EncryptedZipUtil {

    /**
     * 使用加密方式压缩文件或目录
     *
     * @param src 待压缩的文件或目录路径
     * @param dest 目标压缩文件存放路径（可以是目录或完整文件路径）
     * @param isCreateDir 是否保留源目录结构（true：保留目录结构；false：仅压缩目录下文件）
     * @param passwd 压缩密码，若为空则不加密
     * @return 压缩后生成的 ZIP 文件路径；若失败返回 null
     */
    public static String zipEncrypt(String src, String dest, boolean isCreateDir, String passwd) {
        File srcFile = new File(src);
        dest = buildDestinationZipFilePath(srcFile, dest);
        // 根据是否需要加密构造 ZipFile 对象
        ZipFile zipFile = TextUtils.isEmpty(passwd) ? new ZipFile(dest) : new ZipFile(dest, passwd.toCharArray());

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        if (!TextUtils.isEmpty(passwd)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
            parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        }
        try {
            if (srcFile.isDirectory()) {
                if (!isCreateDir) {
                    // 仅压缩目录下的文件，不保留目录结构
                    File[] subFiles = srcFile.listFiles();
                    if (subFiles != null && subFiles.length > 0) {
                        List<File> filesToAdd = new ArrayList<>();
                        Collections.addAll(filesToAdd, subFiles);
                        zipFile.addFiles(filesToAdd, parameters);
                    }
                    return dest;
                }
                // 保留目录结构
                zipFile.addFolder(srcFile, parameters);
            } else {
                zipFile.addFile(srcFile, parameters);
            }
            return dest;
        } catch (ZipException exception) {
            exception.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * 使用加密方式压缩文件或目录，并支持分卷压缩
     *
     * @param src 待压缩的文件或目录路径
     * @param dest 目标压缩文件存放路径（可以是目录或完整文件路径）
     * @param isCreateDir 是否保留源目录结构（true：保留目录结构；false：仅压缩目录下文件）
     * @param passwd 压缩密码，若为空则不加密
     * @param splitLength 分卷大小（单位：字节），例如 64*1024 表示每 64KB 一个分卷
     * @return 压缩后生成的 ZIP 文件路径；若失败返回 null
     */
    public static String zipEncryptSplit(String src, String dest, boolean isCreateDir, String passwd, long splitLength) {
        File srcFile = new File(src);
        dest = buildDestinationZipFilePath(srcFile, dest);
        ZipFile zipFile = TextUtils.isEmpty(passwd) ? new ZipFile(dest) : new ZipFile(dest, passwd.toCharArray());

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        if (!TextUtils.isEmpty(passwd)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
            parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        }
        try {
            if (srcFile.isDirectory()) {
                if (!isCreateDir) {
                    File[] subFiles = srcFile.listFiles();
                    if (subFiles != null && subFiles.length > 0) {
                        List<File> filesToAdd = new ArrayList<>();
                        Collections.addAll(filesToAdd, subFiles);
                        zipFile.createSplitZipFile(filesToAdd, parameters, true, splitLength);
                    }
                    return dest;
                }
                zipFile.createSplitZipFileFromFolder(srcFile, parameters, true, splitLength);
            } else {
                zipFile.createSplitZipFile(Collections.singletonList(srcFile), parameters, true, splitLength);
            }
            return dest;
        } catch (ZipException exception) {
            exception.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * 带进度回调的解压操作（支持加密压缩包）
     *
     * <p> 解压过程中会通过 Handler 发送以下状态消息：</p>
     * <ul>
     *    <li> {@link CompressStatus#START} 解压开始 </li>
     *    <li> {@link CompressStatus#HANDLING} 正在解压，Bundle 中包含键 CompressKeys.PERCENT 表示进度百分比 </li>
     *    <li> {@link CompressStatus#COMPLETED} 解压完成 </li>
     *    <li> {@link CompressStatus#ERROR} 解压出错，Bundle 中包含键 CompressKeys.ERROR 表示错误信息 </li>
     *    <li> CompressStatus.:  </li>
     * </ul>
     *
     * @param zipFile 待解压的 ZIP 文件对象
     * @param dest 目标解压目录路径
     * @param passwd 解压密码（如有）；如果为空，则不传密码
     * @param charset ZIP 文件编码（例如 "UTF-8"）
     * @param handler 用于接收解压进度状态的 Handler
     * @param isDeleteZipFile 解压完成后是否删除原 ZIP 文件
     */
    public static void unzip(File zipFile, String dest, String passwd,
                             String charset, final Handler handler, final boolean isDeleteZipFile) {
        try {
            ZipFile zFile = TextUtils.isEmpty(passwd) ? new ZipFile(zipFile) : new ZipFile(zipFile, passwd.toCharArray());
            // 设置字符集（Zip4j 2.x 支持使用 Charset 对象）
            zFile.setCharset(Charset.forName(charset));
            if (!zFile.isValidZipFile()) {
                throw new ZipException("压缩文件不合法，可能已损坏.");
            }
            File destDir = new File(dest);
            if (!destDir.exists() && !destDir.mkdirs()) {
                throw new ZipException("无法创建解压目标目录.");
            }
            final ProgressMonitor progressMonitor = zFile.getProgressMonitor();
            // 开启新线程监控解压进度，并通过 Handler 回调状态
            Thread progressThread = new Thread(() -> {
                Bundle bundle;
                Message msg;
                try {
                    int percentDone;
                    if (handler == null) return;
                    handler.sendEmptyMessage(CompressStatus.START);
                    do {
                        Thread.sleep(1000);
                        percentDone = progressMonitor.getPercentDone();
                        bundle = new Bundle();
                        bundle.putInt(CompressKeys.PERCENT, percentDone);
                        msg = new Message();
                        msg.what = CompressStatus.HANDLING;
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } while (percentDone < 100);
                    handler.sendEmptyMessage(CompressStatus.COMPLETED);
                } catch (InterruptedException exception) {
                    bundle = new Bundle();
                    bundle.putString(CompressKeys.ERROR, exception.getMessage());
                    msg = new Message();
                    msg.what = CompressStatus.ERROR;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    exception.printStackTrace(System.err);
                } finally {
                    if (isDeleteZipFile) {
                        zipFile.deleteOnExit();
                    }
                }
            });
            progressThread.start();
            // 在当前线程进行解压操作（extractAll 为阻塞方法）
            zFile.extractAll(dest);
        } catch (ZipException exception) {
            exception.printStackTrace(System.err);
        }
    }

    /**
     * 根据源文件和目标参数构建最终的 ZIP 文件存放路径
     * 如果 destParam 为空，则生成默认文件名；如果 destParam 为目录，则在目录下生成以源文件名命名的 ZIP 文件。
     *
     * @param srcFile 待压缩的源文件或目录
     * @param destParam 目标路径参数（可以为完整文件路径或仅为目录）
     * @return 构建后的 ZIP 文件完整路径
     */
    private static String buildDestinationZipFilePath(File srcFile, String destParam) {
        if (destParam == null || destParam.isEmpty()) {
            if (srcFile.isDirectory()) {
                destParam = srcFile.getParent() + File.separator + srcFile.getName() + ".zip";
            } else {
                String fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
                destParam = srcFile.getParent() + File.separator + fileName + ".zip";
            }
        } else {
            createDestDirectoryIfNecessary(destParam);
            if (destParam.endsWith(File.separator)) {
                String fileName;
                if (srcFile.isDirectory()) {
                    fileName = srcFile.getName();
                } else {
                    fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
                }
                destParam += fileName + ".zip";
            }
        }
        return destParam;
    }

    /**
     * 如果目标路径的目录不存在，则创建该目录
     *
     * @param destParam 目标路径参数
     */
    private static void createDestDirectoryIfNecessary(String destParam) {
        File destDir;
        if (destParam.endsWith(File.separator)) {
            destDir = new File(destParam);
        } else {
            destDir = new File(destParam.substring(0, destParam.lastIndexOf(File.separator)));
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    /**
     * 内部类：压缩操作状态码，用于在进度回调中标识当前状态
     */
    public static class CompressStatus {

        /// 解压开始
        public static final int START = 0;
        /// 正在解压
        public static final int HANDLING = 1;
        /// 解压完成
        public static final int COMPLETED = 2;

        /// 解压出错
        public static final int ERROR = 3;
    }

    /**
     * 进度回调中 Bundle 的 Key 常量
     */
    public static class CompressKeys {
        public static final String PERCENT = "PERCENT";
        public static final String ERROR = "ERROR";
    }
}