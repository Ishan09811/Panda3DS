package com.panda3ds.pandroid.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipBuilder {
    private final String outputPath, outputName;
    private ZipOutputStream zip;
    private OutputStream output;

    public ZipBuilder(String path, String name) {
        outputPath = path;
        outputName = name;
    }

    public void begin() throws Exception {
        String path = outputPath + "/" + outputName;
        if (FileUtils.exists(path)) {
            FileUtils.delete(path);
            FileUtils.createFile(outputPath, outputName);
        }
        this.output = FileUtils.getOutputStream(path);
        this.zip = new ZipOutputStream(output);
        zip.setLevel(ZipOutputStream.DEFLATED);
    }

    public void append(String path) throws Exception {
        append(path, "/");
    }

    private void append(String path, String parent) throws Exception {
        String name = FileUtils.getName(path);
        if (FileUtils.isDirectory(path)) {
            for (String child : FileUtils.listFiles(path)) {
                append(path + "/" + child, parent + "/" + name);
            }
        } else {
            ZipEntry entry = new ZipEntry((parent + "/" + name).replaceAll("//", "/"));
            entry.setTime(FileUtils.getLastModified(path));
            entry.setSize(FileUtils.getLength(path));
            zip.putNextEntry(entry);
            InputStream input = FileUtils.getInputStream(path);
            byte[] buffer = new byte[1024 * 64];
            int len;
            while ((len = input.read(buffer)) != -1) {
                zip.write(buffer, 0, len);
            }
            input.close();
            zip.flush();
            zip.closeEntry();
        }
    }

    public void end() throws Exception {
        zip.flush();
        zip.close();
        output.close();
    }
}
