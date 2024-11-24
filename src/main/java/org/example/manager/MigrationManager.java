package org.example.manager;

import lombok.RequiredArgsConstructor;
import org.example.reader.MigrationFile;

import java.sql.Connection;

@RequiredArgsConstructor
public class MigrationManager {
    private final MigrationTableManager tableManager;
    private final MigrationTrackerManager trackerManager;
    private final MigrationLockManager lockManager;

    public void createTablesIfNotExist(Connection connection) {
        tableManager.createTablesIfNotExist(connection);
    }

    public void markMigrationAsApplied(Connection connection, String migrationName, boolean success, String message) {
        trackerManager.markMigrationAsApplied(connection, migrationName, success, message);
    }

    public boolean isMigrationApplied(Connection connection, String migrationName) {
        return trackerManager.isMigrationApplied(connection, migrationName);
    }

    public boolean aquireLock(Connection connection) {
        return lockManager.acquireLock(connection);
    }

    public void releaseLock(Connection connection){
        lockManager.releaseLock(connection);
    }
}
