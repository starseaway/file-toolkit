package com.xinyi.file.entity;

import androidx.annotation.NonNull;

/**
 * 文件实体类，用于封装文件或文件夹的基本信息
 *
 * @author 新一
 * @since 2025/3/17 14:52
 */
public class FileEntity {

    /** 文件名 */
    private String name;

    /** 文件大小 */
    private long size;

    /** 文件路径 */
    private String path;

    /** 文件最后修改时间 */
    private String lastModified;

    /** 是否是文件夹 */
    private boolean isDirectory;

    public FileEntity() { }

    public FileEntity(String name, long size, String path, String lastModified, boolean isDirectory) {
        this.name = name;
        this.size = size;
        this.path = path;
        this.lastModified = lastModified;
        this.isDirectory = isDirectory;
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getLastModified() { return lastModified; }
    public void setLastModified(String lastModified) { this.lastModified = lastModified; }

    public boolean isDirectory() { return isDirectory; }
    public void setDirectory(boolean directory) { isDirectory = directory; }

    @NonNull
    @Override
    public String toString() {
        return "FileEntity{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", path='" + path + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", isDirectory=" + isDirectory +
                '}';
    }
}