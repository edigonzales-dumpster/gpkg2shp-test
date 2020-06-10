package ch.so.agi;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDir extends SimpleFileVisitor<Path> {
    private static ZipOutputStream zos;

    private Path sourceDir;
    
    public ZipDir(Path sourceDir, ZipOutputStream zos) {
        this.sourceDir = sourceDir;
        this.zos = zos;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        String filePath = file.toFile().getAbsolutePath();
        if (filePath.endsWith(".shp") || filePath.endsWith(".shx") || filePath.endsWith(".dbf") || filePath.endsWith(".prj")) {
            try {
                Path targetFile = sourceDir.relativize(file);
                zos.putNextEntry(new ZipEntry(targetFile.toString()));
                byte[] bytes = Files.readAllBytes(file);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
        return FileVisitResult.CONTINUE;
    }

}
