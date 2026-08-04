package com.monitoring.api_monitoring_starter.Database.Provider;


import org.springframework.stereotype.Component;

import java.sql.*;


@Component
public class PostgreSQLStorageProvider
        implements DatabaseStorageProvider {


    @Override
    public boolean supports(String databaseName){

        return databaseName
                .toLowerCase()
                .contains("postgres");

    }



    @Override
    public Long getDatabaseSize(Connection connection)
            throws SQLException{


        String sql =
                """
                SELECT pg_database_size(
                current_database()
                )
                """;


        try(PreparedStatement ps =
                    connection.prepareStatement(sql)){


            ResultSet rs =
                    ps.executeQuery();


            if(rs.next()){

                return rs.getLong(1);

            }


        }


        return 0L;

    }

}