package com.example.api_monitoring_starter.Database.Provider;


import org.springframework.stereotype.Component;

import java.sql.*;


@Component
public class OracleStorageProvider
        implements DatabaseStorageProvider {



    @Override
    public boolean supports(String databaseName){

        return databaseName
                .toLowerCase()
                .contains("oracle");

    }



    @Override
    public Long getDatabaseSize(Connection connection)
            throws SQLException{


        String sql =
                """
                SELECT 
                NVL(SUM(bytes),0)
                FROM user_segments
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