package com.monitoring.api_monitoring_starter.Database.Service;

import com.monitoring.api_monitoring_starter.Database.DTO.ColumnDTO;
import com.monitoring.api_monitoring_starter.Database.DTO.ColumnValidationDTO;
import com.monitoring.api_monitoring_starter.Database.DTO.DatabaseDTO;
import com.monitoring.api_monitoring_starter.Database.DTO.SchemaDTO;
import com.monitoring.api_monitoring_starter.Database.DTO.TableDTO;
import com.monitoring.api_monitoring_starter.Database.Provider.DatabaseStorageProvider;
import com.monitoring.api_monitoring_starter.Database.Scanner.EntityScannerService;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class DatabaseMetadataService {


    private final DataSource dataSource;


    private final EntityScannerService entityScannerService;


    private final List<DatabaseStorageProvider> storageProviders;



    public DatabaseMetadataService(
            DataSource dataSource,
            EntityScannerService entityScannerService,
            List<DatabaseStorageProvider> storageProviders
    ) {

        this.dataSource = dataSource;
        this.entityScannerService = entityScannerService;
        this.storageProviders = storageProviders;

    }



    public DatabaseDTO scanDatabase() throws Exception {


        DatabaseDTO database = new DatabaseDTO();



        try(Connection connection =
                    dataSource.getConnection()) {



            DatabaseMetaData metadata =
                    connection.getMetaData();



            /*
             * Database information
             */

            database.setDatabaseName(
                    metadata.getDatabaseProductName()
            );


            database.setDatabaseVersion(
                    metadata.getDatabaseProductVersion()
            );


            database.setDatabaseProduct(
                    metadata.getDatabaseProductName()
            );


            database.setDriverName(
                    metadata.getDriverName()
            );


            database.setDriverVersion(
                    metadata.getDriverVersion()
            );


            database.setJdbcVersion(
                    metadata.getJDBCMajorVersion()
                            + "."
                            + metadata.getJDBCMinorVersion()
            );


            database.setUrl(
                    metadata.getURL()
            );


            database.setUsername(
                    metadata.getUserName()
            );


            database.setCatalog(
                    connection.getCatalog()
            );


            database.setCurrentSchema(
                    connection.getSchema()
            );


            database.setAutoCommit(
                    connection.getAutoCommit()
            );


            database.setReadOnly(
                    connection.isReadOnly()
            );


            database.setTransactionIsolation(
                    getIsolationLevel(
                            connection.getTransactionIsolation()
                    )
            );



            /*
             * Scan schemas/tables
             */

            List<SchemaDTO> schemas =
                    scanSchemas(metadata);



            database.setSchemas(
                    schemas
            );



            int tableCount = 0;
            int viewCount = 0;



            for(SchemaDTO schema : schemas){


                for(TableDTO table :
                        schema.getTables()){



                    if("VIEW".equalsIgnoreCase(
                            table.getTableType()
                    )){


                        viewCount++;


                    }
                    else {


                        tableCount++;


                    }

                }

            }



            database.setSchemaCount(
                    schemas.size()
            );


            database.setTableCount(
                    tableCount
            );


            database.setViewCount(
                    viewCount
            );



            loadDatabaseStorage(
                    connection,
                    database
            );


        }


        return database;

    }





    private String getIsolationLevel(
            int level
    ){


        return switch(level){


            case Connection.TRANSACTION_NONE ->
                    "NONE";


            case Connection.TRANSACTION_READ_UNCOMMITTED ->
                    "READ_UNCOMMITTED";


            case Connection.TRANSACTION_READ_COMMITTED ->
                    "READ_COMMITTED";


            case Connection.TRANSACTION_REPEATABLE_READ ->
                    "REPEATABLE_READ";


            case Connection.TRANSACTION_SERIALIZABLE ->
                    "SERIALIZABLE";


            default ->
                    "UNKNOWN";

        };


    }





    private void loadDatabaseStorage(
            Connection connection,
            DatabaseDTO database
    ){


        try {


            String databaseName =
                    connection.getMetaData()
                            .getDatabaseProductName();



            DatabaseStorageProvider provider =
                    storageProviders.stream()
                            .filter(
                                    p -> p.supports(databaseName)
                            )
                            .findFirst()
                            .orElse(null);



            if(provider == null){

                return;

            }



            Long size =
                    provider.getDatabaseSize(
                            connection
                    );



            database.setTotalSizeBytes(
                    size
            );


        }
        catch(Exception e){

            e.printStackTrace();

        }


    }
    private List<SchemaDTO> scanSchemas(
            DatabaseMetaData metadata
    ) throws SQLException {


        Map<String, SchemaDTO> schemaMap =
                new LinkedHashMap<>();


        String[] tableTypes =
                getSupportedTableTypes(metadata);



        try(ResultSet rs =
                    metadata.getTables(
                            null,
                            null,
                            "%",
                            tableTypes
                    )) {



            while(rs.next()) {



                String catalog =
                        rs.getString("TABLE_CAT");


                String schemaName =
                        rs.getString("TABLE_SCHEM");


                String tableName =
                        rs.getString("TABLE_NAME");


                String tableType =
                        rs.getString("TABLE_TYPE");



                if(tableName == null){
                    continue;
                }



                /*
                 * Some databases:
                 *
                 * PostgreSQL -> schema
                 * MySQL -> catalog(database)
                 * SQL Server -> schema
                 */

                String displaySchema =
                        determineDisplaySchemaName(
                                catalog,
                                schemaName
                        );



                /*
                 * Remove internal database objects
                 */

                if(isSystemObject(
                        catalog,
                        schemaName,
                        tableName
                )) {

                    continue;

                }



                String schemaKey =
                        normalize(displaySchema);



                SchemaDTO schema =
                        schemaMap.get(schemaKey);



                if(schema == null){


                    schema = new SchemaDTO();


                    schema.setSchemaName(
                            displaySchema
                    );


                    schema.setTables(
                            new ArrayList<>()
                    );


                    schemaMap.put(
                            schemaKey,
                            schema
                    );

                }



                TableDTO table =
                        buildTable(
                                metadata,
                                catalog,
                                schemaName,
                                tableName,
                                tableType
                        );



                schema.getTables()
                        .add(table);

            }


        }


        return new ArrayList<>(
                schemaMap.values()
        );

    }







    private TableDTO buildTable(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String tableName,
            String tableType
    ) throws SQLException {


        TableDTO table =
                new TableDTO();



        table.setTableName(
                tableName
        );


        table.setTableType(
                tableType
        );



        table.setColumnCount(
                getColumnCount(
                        metadata,
                        catalog,
                        schema,
                        tableName
                )
        );



        table.setPrimaryKeyCount(
                getPrimaryKeyCount(
                        metadata,
                        catalog,
                        schema,
                        tableName
                )
        );



        table.setForeignKeyCount(
                getForeignKeyCount(
                        metadata,
                        catalog,
                        schema,
                        tableName
                )
        );



        /*
         * Connect database table
         * with JPA Entity
         */

        table.setEntityName(
                entityScannerService
                        .getEntityName(
                                tableName
                        )
        );



        return table;

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
            String catalog,
            String schema,
            String table
    ) throws SQLException {


        int count = 0;



        try(ResultSet rs =
                    metadata.getColumns(
                            catalog,
                            schema,
                            table,
                            "%"
                    )) {



            while(rs.next()){

                count++;

            }

        }



        return count;

    }







    private int getPrimaryKeyCount(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String table
    ) throws SQLException {


        int count = 0;



        try(ResultSet rs =
                    metadata.getPrimaryKeys(
                            catalog,
                            schema,
                            table
                    )) {



            while(rs.next()){

                count++;

            }

        }



        return count;

    }







    private int getForeignKeyCount(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String table
    ) throws SQLException {


        int count = 0;



        try(ResultSet rs =
                    metadata.getImportedKeys(
                            catalog,
                            schema,
                            table
                    )) {



            while(rs.next()){

                count++;

            }

        }



        return count;

    }







    private String determineDisplaySchemaName(
            String catalog,
            String schema
    ){


        if(schema != null &&
                !schema.isBlank()) {


            return schema;

        }



        if(catalog != null &&
                !catalog.isBlank()) {


            return catalog;

        }



        return "DEFAULT";

    }
    public List<ColumnDTO> getTableDetails(
            String catalog,
            String schema,
            String table
    ) throws SQLException {


        List<ColumnDTO> columns =
                new ArrayList<>();



        try(Connection connection =
                    dataSource.getConnection()) {



            DatabaseMetaData metadata =
                    connection.getMetaData();



            Set<String> primaryKeys =
                    getPrimaryKeyColumns(
                            metadata,
                            catalog,
                            schema,
                            table
                    );



            Set<String> foreignKeys =
                    getForeignKeyColumns(
                            metadata,
                            catalog,
                            schema,
                            table
                    );



            try(ResultSet rs =
                        metadata.getColumns(
                                catalog,
                                schema,
                                table,
                                "%"
                        )) {



                while(rs.next()) {



                    ColumnDTO column =
                            new ColumnDTO();



                    String columnName =
                            rs.getString(
                                    "COLUMN_NAME"
                            );



                    column.setColumnName(
                            columnName
                    );



                    column.setDataType(
                            rs.getString(
                                    "TYPE_NAME"
                            )
                    );



                    column.setSqlType(
                            getSqlTypeName(
                                    rs.getInt(
                                            "DATA_TYPE"
                                    )
                            )
                    );



                    column.setSize(
                            rs.getInt(
                                    "COLUMN_SIZE"
                            )
                    );



                    column.setDecimalDigits(
                            rs.getInt(
                                    "DECIMAL_DIGITS"
                            )
                    );



                    column.setNullable(
                            "YES".equalsIgnoreCase(
                                    rs.getString(
                                            "IS_NULLABLE"
                                    )
                            )
                    );



                    column.setDefaultValue(
                            rs.getString(
                                    "COLUMN_DEF"
                            )
                    );



                    column.setAutoIncrement(
                            "YES".equalsIgnoreCase(
                                    rs.getString(
                                            "IS_AUTOINCREMENT"
                                    )
                            )
                    );



                    column.setPrimaryKey(
                            primaryKeys.contains(
                                    normalize(columnName)
                            )
                    );



                    column.setForeignKey(
                            foreignKeys.contains(
                                    normalize(columnName)
                            )
                    );



                    /*
                     * Add JPA validation information
                     */

                    ColumnValidationDTO validation =
                            entityScannerService
                                    .getColumnInfo(
                                            table,
                                            columnName
                                    );



                    if(validation != null){


                        column.setNullable(
                                validation.isNullable()
                        );


                        column.setValidations(
                                validation.getValidations()
                        );

                    }



                    columns.add(column);


                }


            }


        }



        return columns;

    }








    private String getSqlTypeName(
            int sqlType
    ){


        return switch(sqlType){


            case Types.BIGINT ->
                    "BIGINT";


            case Types.INTEGER ->
                    "INTEGER";


            case Types.SMALLINT ->
                    "SMALLINT";


            case Types.TINYINT ->
                    "TINYINT";


            case Types.VARCHAR ->
                    "VARCHAR";


            case Types.CHAR ->
                    "CHAR";


            case Types.LONGVARCHAR ->
                    "LONGVARCHAR";


            case Types.DATE ->
                    "DATE";


            case Types.TIME ->
                    "TIME";


            case Types.TIMESTAMP ->
                    "TIMESTAMP";


            case Types.TIMESTAMP_WITH_TIMEZONE ->
                    "TIMESTAMP_WITH_TIMEZONE";


            case Types.BOOLEAN ->
                    "BOOLEAN";


            case Types.DECIMAL ->
                    "DECIMAL";


            case Types.NUMERIC ->
                    "NUMERIC";


            case Types.DOUBLE ->
                    "DOUBLE";


            case Types.FLOAT ->
                    "FLOAT";


            case Types.REAL ->
                    "REAL";


            case Types.BINARY ->
                    "BINARY";


            case Types.VARBINARY ->
                    "VARBINARY";


            case Types.BLOB ->
                    "BLOB";


            case Types.CLOB ->
                    "CLOB";


            case Types.NVARCHAR ->
                    "NVARCHAR";


            case Types.NCHAR ->
                    "NCHAR";


            case Types.SQLXML ->
                    "SQLXML";


            default ->
                    "UNKNOWN";

        };


    }









    private Set<String> getPrimaryKeyColumns(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String table
    ) throws SQLException {



        Set<String> columns =
                new HashSet<>();



        try(ResultSet rs =
                    metadata.getPrimaryKeys(
                            catalog,
                            schema,
                            table
                    )) {



            while(rs.next()) {


                String column =
                        rs.getString(
                                "COLUMN_NAME"
                        );


                if(column != null){

                    columns.add(
                            normalize(column)
                    );

                }

            }


        }



        return columns;

    }








    private Set<String> getForeignKeyColumns(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String table
    ) throws SQLException {



        Set<String> columns =
                new HashSet<>();



        try(ResultSet rs =
                    metadata.getImportedKeys(
                            catalog,
                            schema,
                            table
                    )) {



            while(rs.next()) {


                String column =
                        rs.getString(
                                "FKCOLUMN_NAME"
                        );


                if(column != null){


                    columns.add(
                            normalize(column)
                    );


                }


            }


        }



        return columns;

    }








    private boolean isSystemObject(
            String catalog,
            String schema,
            String table
    ){


        return isSystemName(catalog)
                ||
                isSystemName(schema)
                ||
                isSystemName(table);

    }








    private boolean isSystemName(
            String value
    ){


        if(value == null){

            return false;

        }



        String name =
                value.trim()
                        .toLowerCase();



        return name.equals(
                "information_schema"
        )
                ||
                name.equals(
                        "performance_schema"
                )
                ||
                name.equals(
                        "mysql"
                )
                ||
                name.equals(
                        "sys"
                )
                ||
                name.equals(
                        "pg_catalog"
                )
                ||
                name.equals(
                        "system"
                )
                ||
                name.startsWith(
                        "sys_"
                );

    }








    private String normalize(
            String value
    ){


        if(value == null){

            return "";

        }



        return value
                .trim()
                .replace("\"","")
                .replace("`","")
                .replace("[","")
                .replace("]","")
                .toLowerCase();

    }


}