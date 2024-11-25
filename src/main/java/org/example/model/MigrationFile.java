package org.example.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class MigrationFile {
    private final String fileName;
    private final String filePath;
    private final String content;

}
