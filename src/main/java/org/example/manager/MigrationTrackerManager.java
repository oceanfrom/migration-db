package org.example.manager;

import org.example.logger.MigrationLogger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MigrationTrackerManager {
    public void markMigrationAsApplied(Connection connection, String migrationName, boolean success, String message) {
        String query =
                "INSERT INTO applied_migrations (migration_name, status, applied_at) " +
                        "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (migration_name) DO UPDATE SET " +
                        "status = EXCLUDED.status, applied_at = CASE WHEN EXCLUDED.status = 'APPLIED' THEN CURRENT_TIMESTAMP ELSE NULL END;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, migrationName);
            preparedStatement.setString(2, success ? "APPLIED" : "FAILED");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            MigrationLogger.logError("Error marking migration as applied", e);
        }
    }

    public boolean isMigrationApplied(Connection connection, String migrationName) {
        String checkQuery =
                "SELECT COUNT(*) FROM applied_migrations " +
                        "WHERE migration_name = ? AND status = 'APPLIED'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkQuery)) {
            preparedStatement.setString(1, migrationName);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            MigrationLogger.logError("Error checking migration status", e);
            throw new RuntimeException("Error checking migration status", e);
        }
    }

    public void markMigrationAsRolledBack(Connection connection, String migrationName) {
        String updateQuery = "UPDATE applied_migrations SET rollbacked_on = CURRENT_TIMESTAMP WHERE migration_name = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, migrationName.trim());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            MigrationLogger.logError("Error when marking migration as rollback", e);
        }
    }

    public boolean isMigrationRolledBack(Connection connection, String migrationName) {
        String checkQuery =
                "SELECT COUNT(*) FROM applied_migrations " +
                        "WHERE migration_name = ? AND rollbacked_on IS NOT NULL";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setString(1, migrationName.trim());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            MigrationLogger.logError("Error checking if migration is rolled back", e);
            throw new RuntimeException("Error checking if migration is rolled back", e);
        }
    }
}
