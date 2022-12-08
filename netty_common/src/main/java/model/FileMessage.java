package model;

import java.io.File;
import java.util.Arrays;

public class FileMessage extends AbstractMessage {
    private String currentPath;
    private byte[] bytes;
    private String filename;

    public FileMessage(String currentPath, String filename) {
        this.currentPath = currentPath;
        this.filename = filename;
    }

    public FileMessage(String path) {
        this.currentPath = path;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "currentPath='" + currentPath + '\'' +
                ", bytes total =" + bytes.length +
                ", filename='" + filename + '\'' +
                '}';
    }
}