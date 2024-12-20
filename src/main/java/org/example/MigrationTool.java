package org.example;

import lombok.RequiredArgsConstructor;
import org.example.dependency.DependencyFactory;
import org.example.logger.MigrationLogger;
import org.example.model.MigrationFile;
import org.example.reader.MigrationFileReader;
import org.example.utils.ConnectionUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class MigrationTool {
    private final MigrationFileReader fileReader = new MigrationFileReader();

    public void runMigrations(Connection connection) {
        var migrationService = DependencyFactory.createMigrationService();
        List<MigrationFile> migrations = fileReader.getMigrationFiles();
        migrationService.runMigrations(connection, migrations);
    }

    public void rollbackLastMigrationCount(Connection connection, int numberOfMigrationsToRollBack) {
        var rollbackService = DependencyFactory.createRollbackService();
        List<MigrationFile> migrations = fileReader.getMigrationFiles();
        rollbackService.rollbackLastMigrationCount(connection, migrations, numberOfMigrationsToRollBack);
    }

    public void rollbackMigrationToVersion(Connection connection, String version) {
        var rollbackService = DependencyFactory.createRollbackService();
        List<MigrationFile> migrations = fileReader.getMigrationFiles();
        rollbackService.rollbackMigrationToVersion(connection, migrations, version);
    }

    public void getStatus(Connection connection) {
        var status = DependencyFactory.createMigrationStatus();
        status.info(connection);
    }

    public static void main(String[] args) {
        try (Connection connection = ConnectionUtils.getDataSource().getConnection()) {

            var migrationTool = new MigrationTool();

            if (args.length == 0) {
                MigrationLogger.logInfo("No command provided. Available commands: migrate, rollback, rollback-version, status");
                return;
            }

            String command = args[0];

            switch (command) {
                case "migrate":
                    migrationTool.runMigrations(connection);
                    break;
                case "rollback":
                    if (args.length < 2) {
                        MigrationLogger.logInfo("Please provide a number for rollback count.");
                        return;
                    }
                    try {
                        int count = Integer.parseInt(args[1]);
                        migrationTool.rollbackLastMigrationCount(connection, count);
                    } catch (NumberFormatException e) {
                        MigrationLogger.logError("Invalid number for rollback count: " + args[1], e);
                    }
                    break;
                case "rollback-version":
                    if (args.length < 2) {
                        MigrationLogger.logInfo("Please provide a version for rollback.");
                        return;
                    }
                    migrationTool.rollbackMigrationToVersion(connection, args[1]);
                    break;
                case "status":
                    migrationTool.getStatus(connection);
                    break;
                default:
                    MigrationLogger.logInfo("Unknown command: " + command);
                    MigrationLogger.logInfo("Available commands: migrate, rollback, rollback-version, status");
            }
        } catch (SQLException e) {
            MigrationLogger.logError("Failed to establish database connection", e);
        } finally {
            ConnectionUtils.closePool();
        }
    }
}