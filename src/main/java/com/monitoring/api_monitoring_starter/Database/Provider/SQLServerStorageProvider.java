package com.monitoring.api_monitoring_starter.Database.Provider;


import org.springframework.stereotype.Component;

import java.sql.*;


@Component
public class SQLServerStorageProvider
        implements DatabaseStorageProvider {



    @Override
    public boolean supports(String databaseName){

        return databaseName
                .toLowerCase()
                .contains("sql server");

    }



    @Override
    public Long getDatabaseSize(Connection connection)
            throws SQLException{


        String sql =
                """
                SELECT 
                SUM(size)*8192
                FROM sys.database_files
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