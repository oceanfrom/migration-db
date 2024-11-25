package org.example.executor;

import org.example.logger.MigrationLogger;
import org.example.model.MigrationFile;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MigrationExecutor {

    public static void executeMigration(Connection connection, MigrationFile migrationFile) {
        String query = migrationFile.getContent();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            MigrationLogger.logError("Failed to execute migration file: " + migrationFile.getFileName(), e);
        } catch (Exception e) {
            MigrationLogger.logError("Unexpected error during migration execution: " + migrationFile.getFileName(), e);
        }
    }
}
