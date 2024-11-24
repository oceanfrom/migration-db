package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.executor.MigrationExecutor;
import org.example.manager.MigrationManager;
import org.example.reader.MigrationFile;
import org.example.logger.MigrationLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class MigrationService {
    private final MigrationManager manager;

    public void runMigrations(Connection connection, List<MigrationFile> migrations) {
        try {
            manager.createTablesIfNotExist(connection);
            connection.setAutoCommit(false);

            for (MigrationFile file : migrations) {
                processMigration(connection, file);
            }

            connection.commit();
        } catch (Exception ex) {
            try {
                connection.rollback();
                MigrationLogger.logError("Transaction rolled back due to migration failure", ex);
            } catch (SQLException rlbk) {
                MigrationLogger.logError("Error during rollback after failure", rlbk);
            }
        } finally {
            resetAutoCommit(connection);
        }
    }

    private void processMigration(Connection connection, MigrationFile file) {
        String migrationName = file.getFileName();
        try {
            if (manager.isMigrationApplied(connection, migrationName)) {
                MigrationLogger.logInfo("Migration already applied: " + migrationName);
            } else {
                MigrationLogger.logInfo("Applying migration: " + migrationName);
                MigrationExecutor.executeMigration(connection, file);
                manager.markMigrationAsApplied(connection, migrationName, true, "Applied successfully");
            }
        } catch (Exception ex) {
            MigrationLogger.logError("Error during migration execution: " + migrationName, ex);
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
