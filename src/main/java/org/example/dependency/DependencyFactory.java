package org.example.dependency;

import org.example.manager.MigrationLockManager;
import org.example.manager.MigrationManager;
import org.example.manager.MigrationTableManager;
import org.example.manager.MigrationTrackerManager;
import org.example.reader.MigrationFileReader;
import org.example.report.MigrationReport;
import org.example.service.MigrationRollbackService;
import org.example.service.MigrationService;
import org.example.utils.MigrationStatus;

public class DependencyFactory {

    private static final MigrationFileReader fileReader = new MigrationFileReader();
    private static final MigrationTrackerManager trackManager = new MigrationTrackerManager();
    private static final MigrationTableManager tableManager = new MigrationTableManager(fileReader);
    private static final MigrationLockManager lockManager = new MigrationLockManager();
    private static final MigrationReport report = new MigrationReport();
    private static final MigrationManager manager = new MigrationManager(tableManager, trackManager, lockManager);

    public static MigrationService createMigrationService() {
        return new MigrationService(manager, report);
    }

    public static MigrationRollbackService createRollbackService() {
        return new MigrationRollbackService(manager, fileReader);
    }

    public static MigrationStatus createMigrationStatus() {
        return new MigrationStatus(manager);
    }
}
