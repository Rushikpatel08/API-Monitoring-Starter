package com.example.api_monitoring_starter.Database.Service;


import com.example.api_monitoring_starter.Database.DTO.DatabaseDTO;
import com.example.api_monitoring_starter.Database.DTO.SchemaDTO;
import com.example.api_monitoring_starter.Database.DTO.TableDTO;
import com.example.api_monitoring_starter.Database.Scanner.EntityScannerService;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Service
public class DatabaseMetadataService {


    private final DataSource dataSource;

    private final EntityScannerService entityScannerService;



    public DatabaseMetadataService(
            DataSource dataSource,
            EntityScannerService entityScannerService
    ) {
        this.dataSource = dataSource;
        this.entityScannerService = entityScannerService;
    }




    public DatabaseDTO scanDatabase() throws Exception {


        DatabaseDTO database = new DatabaseDTO();


        try(Connection connection = dataSource.getConnection()) {


            DatabaseMetaData metadata =
                    connection.getMetaData();



            // Database information
            database.setDatabaseName(
                    metadata.getDatabaseProductName()
            );


            database.setDatabaseVersion(
                    metadata.getDatabaseProductVersion()
            );


            database.setDriverName(
                    metadata.getDriverName()
            );


            database.setUrl(
                    metadata.getURL()
            );



            List<SchemaDTO> schemas =
                    new ArrayList<>();



            try(ResultSet schemaResult =
                        metadata.getSchemas()) {



                while(schemaResult.next()) {


                    String schemaName =
                            schemaResult.getString(
                                    "TABLE_SCHEM"
                            );

                    if(isSystemSchema(schemaName)) {
                        continue;
                    }
                    /*
                     * Only include schemas
                     * having application tables
                     */
                    List<TableDTO> tables =
                            getTables(
                                    metadata,
                                    schemaName
                            );



                    if(!tables.isEmpty()) {


                        SchemaDTO schema =
                                new SchemaDTO();


                        schema.setSchemaName(
                                schemaName
                        );


                        schema.setTables(
                                tables
                        );


                        schemas.add(schema);

                    }

                }

            }



            database.setSchemas(schemas);


        }


        return database;

    }






    private List<TableDTO> getTables(
            DatabaseMetaData metadata,
            String schemaName
    ) throws SQLException {


        List<TableDTO> tables =
                new ArrayList<>();



        /*
         * Get supported object types dynamically
         */
        String[] tableTypes =
                getSupportedTableTypes(metadata);



        try(ResultSet result =
                    metadata.getTables(
                            null,
                            schemaName,
                            "%",
                            tableTypes
                    )) {



            while(result.next()) {


                String tableName =
                        result.getString(
                                "TABLE_NAME"
                        );


                TableDTO table =
                        new TableDTO();



                table.setTableName(tableName);



                table.setTableType(
                        result.getString(
                                "TABLE_TYPE"
                        )
                );



                table.setColumnCount(
                        getColumnCount(
                                metadata,
                                schemaName,
                                tableName
                        )
                );



                table.setPrimaryKeyCount(
                        getPrimaryKeyCount(
                                metadata,
                                schemaName,
                                tableName
                        )
                );



                table.setForeignKeyCount(
                        getForeignKeyCount(
                                metadata,
                                schemaName,
                                tableName
                        )
                );



                table.setEntityName(
                        entityScannerService
                                .getEntityName(tableName)
                );



                tables.add(table);

            }

        }



        return tables;

    }







    private String[] getSupportedTableTypes(
            DatabaseMetaData metadata
    ) throws SQLException {


        List<String> types =
                new ArrayList<>();


        try(ResultSet rs =
                    metadata.getTableTypes()) {



            while(rs.next()) {


                String type =
                        rs.getString("TABLE_TYPE");


                /*
                 * Include all database objects
                 * except temporary/system objects
                 */
                if(type != null) {

                    types.add(type);

                }

            }

        }


        return types.toArray(new String[0]);

    }







    private int getColumnCount(
            DatabaseMetaData metadata,
            String schema,
            String table
    ) throws SQLException {


        int count = 0;


        try(ResultSet rs =
                    metadata.getColumns(
                            null,
                            schema,
                            table,
                            "%"
                    )) {


            while(rs.next()) {

                count++;

            }

        }


        return count;

    }







    private int getPrimaryKeyCount(
            DatabaseMetaData metadata,
            String schema,
            String table
    ) throws SQLException {


        int count = 0;


        try(ResultSet rs =
                    metadata.getPrimaryKeys(
                            null,
                            schema,
                            table
                    )) {


            while(rs.next()) {

                count++;

            }

        }


        return count;

    }








    private int getForeignKeyCount(
            DatabaseMetaData metadata,
            String schema,
            String table
    ) throws SQLException {


        int count = 0;


        try(ResultSet rs =
                    metadata.getImportedKeys(
                            null,
                            schema,
                            table
                    )) {


            while(rs.next()) {

                count++;

            }

        }


        return count;

    }

    private boolean isSystemSchema(String schemaName) {


        if(schemaName == null) {
            return true;
        }


        String schema =
                schemaName.toUpperCase();


        return schema.equals("INFORMATION_SCHEMA")
                || schema.equals("PG_CATALOG")
                || schema.equals("SYS")
                || schema.equals("SYSTEM")
                || schema.equals("MYSQL")
                || schema.equals("PERFORMANCE_SCHEMA")
                || schema.startsWith("SYS_");

    }






    private boolean isValidSchema(
            DatabaseMetaData metadata,
            String schemaName
    ) throws SQLException {


        if(schemaName == null ||
                schemaName.trim().isEmpty()) {

            return false;
        }



        /*
         * Check whether schema actually contains objects
         */
        try(ResultSet tables =
                    metadata.getTables(
                            null,
                            schemaName,
                            "%",
                            null
                    )) {


            return tables.next();

        }

    }


}