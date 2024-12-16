package com.cc.sync.appconfig.observers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Observer implementation for log level updates.
 */


@Slf4j
@Component
public class LogLevelObserver {
    
    @Override
    public void onAppConfigUpdate(String configKey, Object newValue) {
        if ("logLevels".equals(configKey)) {
            if (newValue instanceof String) {
                String logLevel = (String) newValue;
                switch (logLevel.toUpperCase()) {
                    case "INFO":
                        configureLogLevelToInfo();
                        break;
                    case "WARN":
                        configureLogLevelToWarn();
                        break;
                    case "DEBUG":
                        configureLogLevelToDebug();
                        break;
                    default:
                        log.warn("Unknown log level: {}. No changes applied.", logLevel);
                }
            } else {
                log.error("Invalid value type for logLevels. Expected a String but received: {}", newValue.getClass().getSimpleName());
            }
        }
    }

    private void configureLogLevelToInfo() {
        log.info("Log level updated to INFO");
        // Implement logic to update the application's logging framework to INFO level
    }

    private void configureLogLevelToWarn() {
        log.info("Log level updated to WARN");
        // Implement logic to update the application's logging framework to WARN level
    }

    private void configureLogLevelToDebug() {
        log.info("Log level updated to DEBUG");
        // Implement logic to update the application's logging framework to DEBUG level
    }
}
