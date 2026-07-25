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



            boolean mysql =
                    metadata.getDatabaseProductName()
                            .equalsIgnoreCase("MySQL");



            List<SchemaDTO> schemas =
                    new ArrayList<>();



            ResultSet schemaResult;


            if(mysql){

                // MySQL databases are catalogs
                schemaResult =
                        metadata.getCatalogs();

            }
            else{

                // H2, PostgreSQL etc.
                schemaResult =
                        metadata.getSchemas();

            }



            try(schemaResult){


                while(schemaResult.next()) {


                    String schemaName;


                    if(mysql){

                        schemaName =
                                schemaResult.getString(
                                        "TABLE_CAT"
                                );

                    }
                    else{

                        schemaName =
                                schemaResult.getString(
                                        "TABLE_SCHEM"
                                );

                    }



                    if(isSystemSchema(schemaName)) {
                        continue;
                    }



                    List<TableDTO> tables =
                            getTables(
                                    metadata,
                                    schemaName,
                                    mysql
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



            database.setSchemas(
                    schemas
            );


        }


        return database;

    }







    private List<TableDTO> getTables(
            DatabaseMetaData metadata,
            String schemaName,
            boolean mysql
    ) throws SQLException {


        List<TableDTO> tables =
                new ArrayList<>();



        String[] tableTypes =
                getSupportedTableTypes(metadata);




        ResultSet result;



        if(mysql){


            result =
                    metadata.getTables(
                            schemaName,
                            null,
                            "%",
                            tableTypes
                    );

        }
        else{


            result =
                    metadata.getTables(
                            null,
                            schemaName,
                            "%",
                            tableTypes
                    );

        }



        try(result){


            while(result.next()) {


                String tableName =
                        result.getString(
                                "TABLE_NAME"
                        );



                TableDTO table =
                        new TableDTO();



                table.setTableName(
                        tableName
                );



                table.setTableType(
                        result.getString(
                                "TABLE_TYPE"
                        )
                );



                table.setColumnCount(
                        getColumnCount(
                                metadata,
                                schemaName,
                                tableName,
                                mysql
                        )
                );



                table.setPrimaryKeyCount(
                        getPrimaryKeyCount(
                                metadata,
                                schemaName,
                                tableName,
                                mysql
                        )
                );



                table.setForeignKeyCount(
                        getForeignKeyCount(
                                metadata,
                                schemaName,
                                tableName,
                                mysql
                        )
                );



                table.setEntityName(
                        entityScannerService
                                .getEntityName(
                                        tableName
                                )
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
                    metadata.getTableTypes()){


            while(rs.next()){


                String type =
                        rs.getString(
                                "TABLE_TYPE"
                        );


                if(type != null){

                    types.add(type);

                }

            }

        }


        return types.toArray(
                new String[0]
        );

    }









    private int getColumnCount(
            DatabaseMetaData metadata,
            String schema,
            String table,
            boolean mysql
    ) throws SQLException {


        int count = 0;



        ResultSet rs;



        if(mysql){

            rs =
                    metadata.getColumns(
                            schema,
                            null,
                            table,
                            "%"
                    );

        }
        else{

            rs =
                    metadata.getColumns(
                            null,
                            schema,
                            table,
                            "%"
                    );

        }




        try(rs){


            while(rs.next()){

                count++;

            }

        }


        return count;

    }









    private int getPrimaryKeyCount(
            DatabaseMetaData metadata,
            String schema,
            String table,
            boolean mysql
    ) throws SQLException {


        int count = 0;



        ResultSet rs;



        if(mysql){

            rs =
                    metadata.getPrimaryKeys(
                            schema,
                            null,
                            table
                    );

        }
        else{

            rs =
                    metadata.getPrimaryKeys(
                            null,
                            schema,
                            table
                    );

        }




        try(rs){


            while(rs.next()){

                count++;

            }

        }



        return count;

    }









    private int getForeignKeyCount(
            DatabaseMetaData metadata,
            String schema,
            String table,
            boolean mysql
    ) throws SQLException {


        int count = 0;



        ResultSet rs;



        if(mysql){


            rs =
                    metadata.getImportedKeys(
                            schema,
                            null,
                            table
                    );

        }
        else{


            rs =
                    metadata.getImportedKeys(
                            null,
                            schema,
                            table
                    );

        }




        try(rs){


            while(rs.next()){

                count++;

            }

        }



        return count;

    }









    private boolean isSystemSchema(
            String schemaName
    ) {


        if(schemaName == null){
            return true;
        }



        String schema =
                schemaName.toUpperCase();



        return schema.equals("INFORMATION_SCHEMA")
                || schema.equals("PG_CATALOG")
                || schema.equals("MYSQL")
                || schema.equals("PERFORMANCE_SCHEMA")
                || schema.equals("SYS")
                || schema.equals("SYSTEM")
                || schema.startsWith("SYS_");

    }

}