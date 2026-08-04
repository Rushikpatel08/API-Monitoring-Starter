package com.monitoring.api_monitoring_starter.Database.Provider;


import org.springframework.stereotype.Component;

import java.sql.Connection;


@Component
public class H2StorageProvider
        implements DatabaseStorageProvider {



    @Override
    public boolean supports(String databaseName){

        return databaseName
                .toLowerCase()
                .contains("h2");

    }



    @Override
    public Long getDatabaseSize(Connection connection){

        return 0L;

    }

}