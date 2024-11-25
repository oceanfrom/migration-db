package org.example.manager;

import org.example.constants.MigrationTrackerConstants;
import org.example.logger.MigrationLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MigrationTrackerManager {

    public void markMigrationAsApplied(Connection connection, String migrationName, boolean success, String message) {
        String query = MigrationTrackerConstants.INSERT_OR_UPDATE_MIGRATION_QUERY;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, migrationName);
            preparedStatement.setString(2, success ? "APPLIED" : "FAILED");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            MigrationLogger.logError("Error marking migration as applied", e);
        }
    }

    public boolean isMigrationApplied(Connection connection, String migrationName) {
        String checkQuery = MigrationTrackerConstants.CHECK_MIGRATION_APPLIED_QUERY;
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
        String updateQuery = MigrationTrackerConstants.UPDATE_ROLLEDBACK_QUERY;
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, migrationName.trim());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            MigrationLogger.logError("Error when marking migration as rollback", e);
        }
    }

    public boolean isMigrationRolledBack(Connection connection, String migrationName) {
        String checkQuery = MigrationTrackerConstants.CHECK_MIGRATION_ROLLEDBACK_QUERY;
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkQuery)) {
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
