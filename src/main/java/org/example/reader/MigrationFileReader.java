package org.example.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MigrationFileReader {
    private static final String MIGRATIONS_DIRECTORY = "src/main/resources/db.changelog/versions";

    public List<MigrationFile> getMigrationFiles() {

        File dir = new File(MIGRATIONS_DIRECTORY);

        if (dir.exists() && dir.isDirectory()) {
            return Arrays.stream(dir.listFiles())
                    .filter(file -> file.isFile() && file.getName().endsWith(".sql"))
                    .map(file -> {
                        String fileName = file.getName();
                        String filePath = file.getAbsolutePath();
                        String fileContents = readFileContent(filePath);
                        return new MigrationFile(fileName, filePath, fileContents);
                    })
                    .sorted((file1, file2) -> {
                        int number1 = Integer.parseInt(file1.getFileName().split("-")[0]);
                        int number2 = Integer.parseInt(file2.getFileName().split("-")[0]);
                        return Integer.compare(number1, number2);
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String readFileContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }

    public  String readSqlFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new RuntimeException(filePath + " does not exist");
        }
        return Files.readString(path);
    }
}
