package com.monitoring.api_monitoring_starter.Database.Provider;


import org.springframework.stereotype.Component;

import java.sql.*;


@Component
public class MySQLStorageProvider
        implements DatabaseStorageProvider {


    @Override
    public boolean supports(String databaseName) {

        return databaseName
                .toLowerCase()
                .contains("mysql");
    }


    @Override
    public Long getDatabaseSize(Connection connection)
            throws SQLException {


        String sql =
                """
                SELECT 
                COALESCE(
                SUM(data_length + index_length),
                0
                )
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                """;


        try(PreparedStatement ps =
                    connection.prepareStatement(sql)) {


            ResultSet rs =
                    ps.executeQuery();


            if(rs.next()) {

                return rs.getLong(1);
            }

        }

        return 0L;
    }
}