package org.example.manager;

import org.example.logger.MigrationLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MigrationLockManager {

    private static final String LOCK_ID = "migration_lock";

    public boolean acquireLock(Connection connection) {
        try {
            String lockQuery = "SELECT pg_advisory_lock(12345)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(lockQuery)) {
                preparedStatement.executeQuery();
            }

            String updateLockQuery = "INSERT INTO migration_lock (lock_id, locked_at) " +
                    "VALUES (?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (lock_id) DO UPDATE SET locked_at = CURRENT_TIMESTAMP, released_at = NULL";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateLockQuery)) {
                preparedStatement.setString(1, LOCK_ID);
                preparedStatement.executeUpdate();
            }
            return true;

        } catch (SQLException e) {
            MigrationLogger.logError("Error acquiring lock", e);
            throw new RuntimeException("Error acquiring lock", e);
        }
    }

    public void releaseLock(Connection connection) {
        try {
            String unlockQuery = "SELECT pg_advisory_unlock(12345)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(unlockQuery)) {
                preparedStatement.executeQuery();
            }

            String updateLockQuery = "UPDATE migration_lock SET released_at = CURRENT_TIMESTAMP WHERE lock_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateLockQuery)) {
                preparedStatement.setString(1, LOCK_ID);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            MigrationLogger.logError("Error releasing lock", e);
        }
    }
}
