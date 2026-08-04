package com.monitoring.api_monitoring_starter.Database.Provider;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseStorageProvider {

    boolean supports(String databaseName);

    Long getDatabaseSize(Connection connection)
            throws SQLException;
}