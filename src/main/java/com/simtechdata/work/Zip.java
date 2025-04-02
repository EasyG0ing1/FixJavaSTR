package com.simtechdata.work;

import com.simtechdata.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {

    public static void zip(List<File> files, File zipFile, String rootPathString) throws IOException {
        if (zipFile.exists()) {
            Log.showLn("Zip file already exists, exiting\n");
            return;
        }
        Path rootPath = Paths.get(rootPathString);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            byte[] buffer = new byte[1024];
            for (File file : files) {
                Path filePath = file.toPath();
                if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                    String   entryName = rootPath.relativize(filePath).toString().replace(File.separatorChar, '/');
                    ZipEntry entry     = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    try (FileInputStream fis = new FileInputStream(file)) {
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    public static void unzip(String zipFilePathString, String unzipDirectory) throws IOException {
        File zipFile   = new File(zipFilePathString);
        Path unzipPath = Paths.get(unzipDirectory);
        Log.showLn("\nUnzipping files from: " + zipFile.getName() + "\n");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            int      count = 0;
            while ((entry = zis.getNextEntry()) != null) {
                Path file     = Paths.get(entry.getName());
                Path filePath = unzipPath.resolve(file);
                unzipFile(zis, filePath);
                zis.closeEntry();
                count++;
            }
            Log.show("\nFiles unzipped to: " + unzipPath);
            Log.showLn("\nNumber of files unzipped: " + count);
        }
    }

    private static void unzipFile(ZipInputStream zis, Path filePath) throws IOException {
        Log.showLn(filePath.toAbsolutePath().toString());
        byte[] buffer     = new byte[8192];
        Path   parentPath = filePath.toAbsolutePath().getParent();
        if (parentPath != null && !Files.exists(parentPath)) {
            Files.createDirectories(parentPath);
        }
        File outputFile = filePath.toFile();
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            int length;
            while ((length = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

}
