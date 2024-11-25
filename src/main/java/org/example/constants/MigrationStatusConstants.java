package org.example.constants;

public class MigrationStatusConstants {
    public static final String GET_MIGRATION_STATUS_QUERY = "SELECT migration_name, rollbacked_on FROM applied_migrations ORDER BY applied_at ASC";
}
