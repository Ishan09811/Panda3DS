package com.panda3ds.pandroid.utils;

import android.net.Uri;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {


    public static void extract(String zipFile, String outputFolder, String outputName) throws Exception {
        String path = outputFolder;
        if (FileUtils.exists(path+"/"+outputName)) {
            throw new IllegalArgumentException("File already exists in output location");
        }

        byte[] buffer = new byte[1024*256];
        int bufferLen = 0;

        ZipInputStream in = new ZipInputStream(FileUtils.getInputStream(zipFile));
        ZipEntry entry = in.getNextEntry();
        while (entry != null) {
            if (!entry.isDirectory()) {
                String[] dir = entry.getName().split("/");
                String parent;
                {
                    StringBuilder builder = new StringBuilder();
                    for (int i = dir[0].length() == 0 ? 1 : 0; i < dir.length - 1; i++) {
                        builder.append(dir[i]).append("/");
                    }
                    parent = builder.toString();
                }
                if (parent.length() > 0) {
                    mkdirs(path, parent);
                }
                if (parent.length() > 0) {
                    parent = "/" + parent;
                }
                String name = dir[dir.length-1];
                FileUtils.createFile(path+parent, name);
                OutputStream out = FileUtils.getOutputStream(path+parent+"/"+name);
                while ((bufferLen = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bufferLen);
                }
                out.flush();
                out.close();
            }
            entry = in.getNextEntry();
        }
        in.close();
    }

    private static void mkdirs(String path, String subs) {
        for (String segment: subs.split("/")) {
            if (!FileUtils.exists(path+"/"+segment)) {
                FileUtils.createDir(path, segment);
            }
            path = path + "/"+segment;
        }
    }
}
