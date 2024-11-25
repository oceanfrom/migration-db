package org.example.utils;

import lombok.RequiredArgsConstructor;
import org.example.logger.MigrationLogger;
import org.example.manager.MigrationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class MigrationStatus {
    private final MigrationManager manager;
    private String query = "SELECT migration_name, rollbacked_on FROM applied_migrations ORDER BY applied_at ASC";

    public void info(Connection connection) {
        String lastMigration = null;
                if(!manager.aquireLock(connection)) {
                    MigrationLogger.logInfo("Failed to acquire lock for migration rollback");
                    return;
                }

        try {
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String migrationName = resultSet.getString("migration_name");
                String rollbackedOn = resultSet.getString("rollbacked_on");

                if (rollbackedOn != null) {
                    MigrationLogger.logInfo("Migration " + migrationName + " was rolled back on " + rollbackedOn);
                } else {
                    MigrationLogger.logInfo("Migration " + migrationName + " was applied successfully (no rollback).");
                    lastMigration = migrationName;
                }
            }
        } catch (SQLException e) {
            MigrationLogger.logError("Sqlexp error", e);
            return;
        } finally {
             manager.releaseLock(connection);
        }

        if (lastMigration != null) {
            System.out.println("\nLast active migration: " + lastMigration);
        } else {
            System.out.println("\nNo active migrations found.");
        }
    }

}
