package org.example.manager;

import org.example.constants.MigrationLockConstants;
import org.example.logger.MigrationLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MigrationLockManager {

    public boolean acquireLock(Connection connection) {
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(MigrationLockConstants.LOCK_QUERY)) {
                preparedStatement.executeQuery();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(MigrationLockConstants.INSERT_OR_UPDATE_LOCK_QUERY)) {
                preparedStatement.setString(1, MigrationLockConstants.LOCK_ID);
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
            try (PreparedStatement preparedStatement = connection.prepareStatement(MigrationLockConstants.UNLOCK_QUERY)) {
                preparedStatement.executeQuery();
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(MigrationLockConstants.UPDATE_RELEASED_AT_QUERY)) {
                preparedStatement.setString(1, MigrationLockConstants.LOCK_ID);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            MigrationLogger.logError("Error releasing lock", e);
        }
    }
}
