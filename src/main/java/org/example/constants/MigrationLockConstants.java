package org.example.constants;

public class MigrationLockConstants {
    public static final String LOCK_ID = "migration_lock";
    public static final String LOCK_QUERY = "SELECT pg_advisory_lock(12345)";
    public static final String UNLOCK_QUERY = "SELECT pg_advisory_unlock(12345)";

    public static final String INSERT_OR_UPDATE_LOCK_QUERY =
            "INSERT INTO migration_lock (lock_id, locked_at) " +
                    "VALUES (?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (lock_id) DO UPDATE SET locked_at = CURRENT_TIMESTAMP, released_at = NULL";

    public static final String UPDATE_RELEASED_AT_QUERY =
            "UPDATE migration_lock SET released_at = CURRENT_TIMESTAMP WHERE lock_id = ?";
}
