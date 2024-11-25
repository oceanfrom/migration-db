package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.executor.MigrationExecutor;
import org.example.logger.MigrationLogger;
import org.example.manager.MigrationManager;
import org.example.reader.MigrationFile;
import org.example.reader.MigrationFileReader;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class MigrationRollbackService {
    private final MigrationManager manager;
    private final MigrationFileReader fileReader;
    private static final String ROLLBACK_DIR = "src/main/resources/db.changelog/rollback/";

    public void executeRollback(Connection connection, List<MigrationFile> files) {
        try {
            if (!manager.aquireLock(connection)) {
                MigrationLogger.logInfo("Migrations are already being performed in another process");
                return;
            }
            connection.setAutoCommit(false);

            List<MigrationFile> migrationsToRollback = files;
            for (MigrationFile rollbackFile : migrationsToRollback) {
                processRollback(connection, rollbackFile);
            }
            connection.commit();
        } catch (Exception ex) {
            handleRollbackError(connection, ex);
        } finally {
            manager.releaseLock(connection);
           resetAutoCommit(connection);
        }
    }

    public void processRollback(Connection connection, MigrationFile migrationFile) {
        if (manager.isMigrationRolledBack(connection, migrationFile.getFileName())) {
            MigrationLogger.logInfo("Migration " + migrationFile.getFileName() + " has already been rolled back, skipping...");
            return;
        }
        String rollbackFileName = migrationFile.getFileName().replaceFirst("([^/]+)(\\.sql)$", "$1-rollback$2");
        MigrationFile rollbackMigrationFile = findRollbackMigrationFile(rollbackFileName);
        if (rollbackMigrationFile != null) {
            MigrationExecutor.executeMigration(connection, rollbackMigrationFile);
            manager.markMigrationAsRolledBack(connection, migrationFile.getFileName());
            MigrationLogger.logInfo("Migration " + migrationFile.getFileName() + " rolled back successfully");
        } else {
            MigrationLogger.logInfo("Rollback file for migration " + migrationFile.getFileName() + " not found, skipping rollback.");
        }
    }

    private MigrationFile findRollbackMigrationFile(String rollbackFileName) {
        File rollbackFile = new File(ROLLBACK_DIR + rollbackFileName);
        if (rollbackFile.exists()) {
            String content = fileReader.readFileContent(String.valueOf(rollbackFile));
            return new MigrationFile(rollbackFileName, rollbackFile.getAbsolutePath(), content);
        }
        return null;
    }

    public void rollbackMigrationToVersion(Connection connection, List<MigrationFile> files, String version) {
        int target = -1;
        for (int i = files.size() - 1; i >= 0; i--) {
            if (files.get(i).getFileName().equals(version)) {
                target = i;
                break;
            }
        }
        if (target == -1) {
            throw new IllegalArgumentException("Migration " + version + " not found");
        }
        MigrationLogger.logInfo("Starting rollback to version: " + version);
        List<MigrationFile> rolledBackFiles = files.subList(target + 1, files.size());
        Collections.reverse(rolledBackFiles);
        executeRollback(connection, rolledBackFiles);
    }

    public void rollbackLastMigrationCount(Connection connection, List<MigrationFile> allMigrations, int count) {
        int lastRolledBackIndex = -1;
        for (int i = 0; i < allMigrations.size(); i++) {
            if (manager.isMigrationRolledBack(connection, allMigrations.get(i).getFileName())) {
                lastRolledBackIndex = i - 1;
                break;
            }
        }
        if (lastRolledBackIndex == -1) {
            lastRolledBackIndex = allMigrations.size() - 1;
        }
        int startIndex = Math.max(lastRolledBackIndex, 0);
        List<MigrationFile> migrationsToRollback = new ArrayList<>();
        int rollbackCount = 0;
        for (int i = startIndex; i >= 0 && rollbackCount < count; i--) {
            migrationsToRollback.add(allMigrations.get(i));
            rollbackCount++;
        }
        executeRollback(connection, migrationsToRollback);
    }

    private void handleRollbackError(Connection connection, Exception ex) {
        MigrationLogger.logError("Error occurred during rollback process", ex);
        try {
            connection.rollback();
        } catch (SQLException e) {
            MigrationLogger.logError("Failed to rollback transaction after migration failure", e);
        }
    }

    private void resetAutoCommit(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            MigrationLogger.logError("Error while restoring auto-commit mode", e);
        }
    }
}

