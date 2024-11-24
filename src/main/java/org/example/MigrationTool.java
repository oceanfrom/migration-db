package org.example;


import org.example.executor.MigrationExecutor;
import org.example.manager.MigrationManager;
import org.example.manager.MigrationTableManager;
import org.example.manager.MigrationTrackerManager;
import org.example.reader.MigrationFile;
import org.example.reader.MigrationFileReader;
import org.example.service.MigrationService;
import org.example.utils.ConnectionUtils;
import org.example.utils.MigrationStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MigrationTool {

    public static void main(String[] args) {

        var fileReader = new MigrationFileReader();
        var trackManager = new MigrationTrackerManager();
        var tableManager = new MigrationTableManager(fileReader);
        var manager = new MigrationManager(tableManager, trackManager);

        try (Connection connection = ConnectionUtils.getConnection()) {
            MigrationFileReader migrationFileReader = new MigrationFileReader();
            List<MigrationFile> migrations = migrationFileReader.getMigrationFiles();
            var migrationSerivce = new MigrationService(manager);
            var status = new MigrationStatus();
           migrationSerivce.runMigrations(connection,migrations);
            status.info(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection", e);
        }
    }
}
