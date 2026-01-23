package app.model;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class ModelRepository {
    private final Path root = Paths.get("models");

    public ModelRepository() {
        try {
            Files.createDirectories(root);
        } catch (IOException ignored) {}
    }

    public List<String> listModels() throws IOException {
        if (!Files.exists(root)) return List.of();
        try (var s = Files.list(root)) {
            return s.filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public String importModel(Path sourceObjFile) throws IOException {
        Path srcDir = sourceObjFile.getParent();
        String modelName = srcDir.getFileName().toString();
        Path destDir = root.resolve(modelName);

        if (Files.exists(destDir)) {
            modelName = modelName + "_" + System.currentTimeMillis();
            destDir = root.resolve(modelName);
        }

        copyDir(srcDir, destDir);
        return modelName;
    }

    public void deleteModel(String modelName) throws IOException {
        Path dir = root.resolve(modelName);
        if (!Files.exists(dir)) return;
        deleteDir(dir);
    }

    private static void copyDir(Path src, Path dst) throws IOException {
        Files.createDirectories(dst);
        try (var s = Files.walk(src)) {
            for (Path p : s.toList()) {
                Path rel = src.relativize(p);
                Path out = dst.resolve(rel);
                if (Files.isDirectory(p)) Files.createDirectories(out);
                else Files.copy(p, out, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void deleteDir(Path dir) throws IOException {
        try (var s = Files.walk(dir)) {
            for (Path p : s.sorted((a, b) -> b.compareTo(a)).toList()) {
                Files.deleteIfExists(p);
            }
        }
    }
}
