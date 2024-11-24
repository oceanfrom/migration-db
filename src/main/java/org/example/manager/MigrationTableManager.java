package org.example.manager;

import lombok.RequiredArgsConstructor;
import org.example.logger.MigrationLogger;
import org.example.reader.MigrationFileReader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@RequiredArgsConstructor
public class MigrationTableManager {
    private final MigrationFileReader reader;

    public  void createTablesIfNotExist(Connection connection) {
        try {
            String createTablesQuery = reader.readSqlFile("src/main/resources/sql/create_tables.sql");

            try(Statement statement = connection.createStatement();) {

                connection.setAutoCommit(false);
                statement.execute(createTablesQuery);
                connection.commit();
            }
        } catch (IOException e) {
            MigrationLogger.logError("Error reading SQL file to create tables",e);
        } catch (SQLException e) {
            MigrationLogger.logError("Error when executing SQL query to create tables", e);
        }
    }
}
