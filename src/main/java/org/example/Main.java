package org.example;

import org.example.utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            Connection connection = ConnectionUtils.getConnection();
            MigrationTool tool = new MigrationTool(connection);
            //    tool.runMigrations();
            // tool.rollbackMigrationToVersion("0002-insert.sql");
            tool.rollbackLastMigrationCount(1);
            tool.getStatus();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
