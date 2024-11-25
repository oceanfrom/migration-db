package org.example.constants;

public class MigrationTrackerConstants {

    public static final String INSERT_OR_UPDATE_MIGRATION_QUERY =
            "INSERT INTO applied_migrations (migration_name, status, applied_at) " +
                    "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (migration_name) DO UPDATE SET " +
                    "status = EXCLUDED.status, applied_at = CASE WHEN EXCLUDED.status = 'APPLIED' THEN CURRENT_TIMESTAMP ELSE NULL END;";

    public static final String CHECK_MIGRATION_APPLIED_QUERY =
            "SELECT COUNT(*) FROM applied_migrations " +
                    "WHERE migration_name = ? AND status = 'APPLIED'";

    public static final String UPDATE_ROLLEDBACK_QUERY =
            "UPDATE applied_migrations SET rollbacked_on = CURRENT_TIMESTAMP WHERE migration_name = ?";

    public static final String CHECK_MIGRATION_ROLLEDBACK_QUERY =
            "SELECT COUNT(*) FROM applied_migrations " +
                    "WHERE migration_name = ? AND rollbacked_on IS NOT NULL";
}
