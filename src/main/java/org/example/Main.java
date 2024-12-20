package org.example;

import org.example.utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = ConnectionUtils.getDataSource().getConnection()) {
            MigrationTool tool = new MigrationTool();
            //tool.runMigrations(connection);
            tool.rollbackMigrationToVersion(connection, "0002-insert.sql");
            //tool.rollbackLastMigrationCount(connection, 4);
            tool.getStatus(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            ConnectionUtils.closePool();
        }
    }
}
