package com.rahil.filescanner.data;

public class FileData implements Comparable<FileData> {
    private long size;
    private String fileName;

    public FileData(long size, String fileName) {
        this.size = size;
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    @Override
    public int compareTo(FileData file) {
        return new Long(size).compareTo(file.getSize());
    }
}
