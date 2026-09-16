package com.bankapi.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Sets up file-based logging once, at startup. Call AppLogging.configure()
 * from Main before anything else runs.
 *
 * Java's built-in logging API doesn't have a level literally called
 * "ERROR" - its closest equivalent is SEVERE. Everywhere the spec says
 * "log at ERROR", this app calls logger.severe(...); everywhere it says
 * "log at INFO", it calls logger.info(...). Both end up in the same file.
 */
public final class AppLogging {

    private static final String LOG_FILE = "bank-of-cli.log";
    private static boolean configured = false;

    private AppLogging() {
    }

    public static synchronized void configure() {
        if (configured) {
            return;
        }
        try {
            Logger rootLogger = Logger.getLogger("");
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.INFO);
            configured = true;
        } catch (IOException e) {
            throw new IllegalStateException("Could not set up log file " + LOG_FILE, e);
        }
    }
}