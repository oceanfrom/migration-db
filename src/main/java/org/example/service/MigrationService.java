package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.executor.MigrationExecutor;
import org.example.manager.MigrationManager;
import org.example.model.MigrationFile;
import org.example.logger.MigrationLogger;
import org.example.report.MigrationReport;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class MigrationService {
    private final MigrationManager manager;
    private final MigrationReport report;


    public void runMigrations(Connection connection, List<MigrationFile> migrations) {
        try {
            manager.createTablesIfNotExist(connection);
            if(!manager.aquireLock(connection)) {
                MigrationLogger.logInfo("Migrations are already being performed in another process");
                return;
            }
            connection.setAutoCommit(false);

            for (MigrationFile file : migrations) {
                processMigration(connection, file);
            }
            connection.commit();
        } catch (Exception ex) {
           handleRollbackError(connection, ex);
        } finally {
            manager.releaseLock(connection);
            generateReport();
            resetAutoCommit(connection);
        }
    }

    private void processMigration(Connection connection, MigrationFile file) {
        String migrationName = file.getFileName();
        try {
            if (manager.isMigrationApplied(connection, migrationName)) {
                MigrationLogger.logInfo("Migration already applied: " + migrationName);
            } else {
                MigrationExecutor.executeMigration(connection, file);
                report.addMigrationResult(migrationName, true, "Applied successfully");
                manager.markMigrationAsApplied(connection, migrationName, true, "Applied successfully");
            }
        } catch (Exception ex) {
            report.addMigrationResult(migrationName, false, "Not applied: " + migrationName);

        }
    }

    private void handleRollbackError(Connection connection, Exception ex) {
        MigrationLogger.logError("Error occurred during rollback process", ex);
        try {
            connection.rollback();
        } catch (SQLException e) {
            MigrationLogger.logError("Failed to rollback transaction after migration failure", e);
        }
    }

    private void generateReport() {
        try {
            report.generateJSONReport("migration_report.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
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
