package org.example;

import org.example.dependency.DependencyFactory;
import org.example.logger.MigrationLogger;
import org.example.model.MigrationFile;
import org.example.reader.MigrationFileReader;
import org.example.utils.ConnectionUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MigrationTool {

    public void runMigrations() {
        try (Connection connection = ConnectionUtils.getConnection()) {
            var migrationService = DependencyFactory.createMigrationService();
            var fileReader = new MigrationFileReader();
            List<MigrationFile> migrations = fileReader.getMigrationFiles();
            migrationService.runMigrations(connection, migrations);
        } catch (SQLException e) {
            MigrationLogger.logError("Failed to establish database connection", e);
        }
    }

    public void rollbackLastMigrationCount(int numberOfMigrationsToRollBack) {
        try (Connection connection = ConnectionUtils.getConnection()) {
            var rollbackService = DependencyFactory.createRollbackService();
            var fileReader = new MigrationFileReader();
            List<MigrationFile> migrations = fileReader.getMigrationFiles();
            rollbackService.rollbackLastMigrationCount(connection, migrations, numberOfMigrationsToRollBack);
        } catch (SQLException e) {
            MigrationLogger.logError("Failed to establish database connection", e);
        }
    }

    public void rollbackMigrationToVersion(String version) {
        try (Connection connection = ConnectionUtils.getConnection()) {
            var rollbackService = DependencyFactory.createRollbackService();
            var fileReader = new MigrationFileReader();
            List<MigrationFile> migrations = fileReader.getMigrationFiles();
            rollbackService.rollbackMigrationToVersion(connection, migrations, version);
        } catch (SQLException e) {
            MigrationLogger.logError("Failed to establish database connection", e);
        }
    }

    public void getStatus() {
        try (Connection connection = ConnectionUtils.getConnection()) {
            var status = DependencyFactory.createMigrationStatus();
            status.info(connection);
        } catch (SQLException e) {
            MigrationLogger.logError("Failed to establish database connection", e);
        }
    }

    public static void main(String[] args) {
        MigrationTool migrationTool = new MigrationTool();

        if (args.length == 0) {
            MigrationLogger.logInfo("No command provided. Available commands: migrate, rollback, rollback-version, status");
            return;
        }

        String command = args[0];

        switch (command) {
            case "migrate":
                migrationTool.runMigrations();
                break;
            case "rollback":
                if (args.length < 2) {
                    MigrationLogger.logInfo("Please provide a number for rollback count.");
                    return;
                }
                try {
                    int count = Integer.parseInt(args[1]);
                    migrationTool.rollbackLastMigrationCount(count);
                } catch (NumberFormatException e) {
                    MigrationLogger.logError("Invalid number for rollback count: " + args[1], e);
                }
                break;
            case "rollback-version":
                if (args.length < 2) {
                    MigrationLogger.logInfo("Please provide a version for rollback.");
                    return;
                }
                migrationTool.rollbackMigrationToVersion(args[1]);
                break;
            case "status":
                migrationTool.getStatus();
                break;
            default:
                MigrationLogger.logInfo("Unknown command: " + command);
                MigrationLogger.logInfo("Available commands: migrate, rollback, rollback-version, status");
        }
    }
}
