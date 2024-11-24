package org.example.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MigrationLogger {
    private static final Logger logger = LogManager.getLogger(MigrationLogger.class);

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logError(String message, Exception e) {
        logger.error(message, e);
    }
}
