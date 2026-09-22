package com.familyflow.infrastructure;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JsonStore {
    private final ObjectMapper mapper;
    private final Path dataDir;
    private final Path legacyDir;

    public JsonStore(ObjectMapper mapper,
                     @Value("${familyflow.data-dir}") String dataDir,
                     @Value("${familyflow.legacy-dir}") String legacyDir) {
        this.mapper = mapper;
        this.dataDir = Path.of(dataDir).toAbsolutePath().normalize();
        this.legacyDir = Path.of(legacyDir).toAbsolutePath().normalize();
    }

    public synchronized <T> List<T> read(String fileName, Class<T> type) {
        Path file = resolveAndMigrate(fileName);
        if (!Files.exists(file)) return new ArrayList<>();
        try {
            JavaType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return mapper.readValue(file.toFile(), listType);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Could not read " + fileName + ".", exception);
        }
    }

    public synchronized <T> void write(String fileName, List<T> values) {
        Path file = dataDir.resolve(fileName);
        Path temporary = dataDir.resolve(fileName + ".tmp");
        try {
            Files.createDirectories(dataDir);
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), values);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save " + fileName + ".", exception);
        }
    }

    private Path resolveAndMigrate(String fileName) {
        Path current = dataDir.resolve(fileName);
        Path legacy = legacyDir.resolve(fileName);
        if (Files.exists(current) || !Files.exists(legacy)) return current;
        try {
            Files.createDirectories(dataDir);
            Files.copy(legacy, current, StandardCopyOption.COPY_ATTRIBUTES);
            return current;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not migrate legacy " + fileName + ".", exception);
        }
    }
}
