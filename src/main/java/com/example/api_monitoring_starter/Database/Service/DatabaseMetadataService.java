package com.example.api_monitoring_starter.Database.Service;

import com.example.api_monitoring_starter.Database.DTO.ColumnDTO;
import com.example.api_monitoring_starter.Database.DTO.DatabaseDTO;
import com.example.api_monitoring_starter.Database.DTO.SchemaDTO;
import com.example.api_monitoring_starter.Database.DTO.TableDTO;
import com.example.api_monitoring_starter.Database.Scanner.EntityScannerService;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
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

        try (Connection connection =
                     dataSource.getConnection()) {

            DatabaseMetaData metadata =
                    connection.getMetaData();

            // -----------------------------------------
            // DATABASE INFORMATION
            // -----------------------------------------

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

            // -----------------------------------------
            // SCAN TABLES DYNAMICALLY
            // -----------------------------------------

            List<SchemaDTO> schemas =
                    scanSchemas(metadata);

            database.setSchemas(schemas);
        }

        return database;
    }

    /**
     * Dynamically scans catalogs/schemas/tables.
     *
     * No MySQL/PostgreSQL/Oracle/SQL Server
     * specific branching is required here.
     */
    private List<SchemaDTO> scanSchemas(
            DatabaseMetaData metadata
    ) throws SQLException {

        /*
         * LinkedHashMap preserves the order returned
         * by the database driver.
         *
         * Key:
         * catalog + schema
         */
        Map<String, SchemaDTO> schemaMap =
                new LinkedHashMap<>();

        String[] tableTypes =
                getSupportedTableTypes(metadata);

        try (ResultSet result =
                     metadata.getTables(
                             null,
                             null,
                             "%",
                             tableTypes
                     )) {

            while (result.next()) {

                String catalog =
                        result.getString("TABLE_CAT");

                String schemaName =
                        result.getString("TABLE_SCHEM");

                String tableName =
                        result.getString("TABLE_NAME");

                String tableType =
                        result.getString("TABLE_TYPE");

                if (tableName == null) {
                    continue;
                }

                /*
                 * Skip system objects.
                 */
                if (isSystemObject(
                        catalog,
                        schemaName,
                        tableName
                )) {
                    continue;
                }

                String schemaKey =
                        createSchemaKey(
                                catalog,
                                schemaName
                        );

                SchemaDTO schema =
                        schemaMap.get(schemaKey);

                if (schema == null) {

                    schema =
                            new SchemaDTO();

                    schema.setSchemaName(
                            determineDisplaySchemaName(
                                    catalog,
                                    schemaName
                            )
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

                schema.getTables().add(table);
            }
        }

        return new ArrayList<>(
                schemaMap.values()
        );
    }

    /**
     * Builds table information using standard JDBC metadata.
     */
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

        table.setEntityName(
                entityScannerService
                        .getEntityName(tableName)
        );

        return table;
    }

    /**
     * Get every table type supported by the database driver.
     *
     * This prevents hardcoding:
     *
     * TABLE
     * VIEW
     * BASE TABLE
     * etc.
     */
    private String[] getSupportedTableTypes(
            DatabaseMetaData metadata
    ) throws SQLException {

        List<String> types =
                new ArrayList<>();

        try (ResultSet rs =
                     metadata.getTableTypes()) {

            while (rs.next()) {

                String type =
                        rs.getString("TABLE_TYPE");

                if (type != null) {
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

        try (ResultSet rs =
                     metadata.getColumns(
                             catalog,
                             schema,
                             table,
                             "%"
                     )) {

            while (rs.next()) {
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

        try (ResultSet rs =
                     metadata.getPrimaryKeys(
                             catalog,
                             schema,
                             table
                     )) {

            while (rs.next()) {
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

        try (ResultSet rs =
                     metadata.getImportedKeys(
                             catalog,
                             schema,
                             table
                     )) {

            while (rs.next()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Creates a unique schema key.
     *
     * Some databases use catalog as the database,
     * while others use schema.
     */
    private String createSchemaKey(
            String catalog,
            String schema
    ) {

        return normalize(catalog)
                + "::"
                + normalize(schema);
    }

    /**
     * Determines what should be displayed in the UI.
     */
    private String determineDisplaySchemaName(
            String catalog,
            String schema
    ) {

        if (schema != null && !schema.isBlank()) {
            return schema;
        }

        if (catalog != null && !catalog.isBlank()) {
            return catalog;
        }

        return "DEFAULT";
    }

    /**
     * Filters common system objects.
     *
     * This does not determine how the database is scanned.
     * It only prevents internal database objects from
     * cluttering the Database Explorer.
     */
    private boolean isSystemObject(
            String catalog,
            String schema,
            String table
    ) {

        return isSystemName(catalog)
                || isSystemName(schema)
                || isSystemName(table);
    }

    private boolean isSystemName(
            String value
    ) {

        if (value == null) {
            return false;
        }

        String name =
                value.trim().toUpperCase();

        return name.equals("INFORMATION_SCHEMA")
                || name.equals("PG_CATALOG")
                || name.equals("PERFORMANCE_SCHEMA")
                || name.equals("SYS")
                || name.equals("SYSTEM")
                || name.equals("SYSCAT")
                || name.equals("SYSIBM")
                || name.equals("MYSQL")
                || name.startsWith("SYS_");
    }
    public List<ColumnDTO> getTableDetails(
            String catalog,
            String schema,
            String table
    ) throws SQLException {

        List<ColumnDTO> columns = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {

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

            try (ResultSet rs =
                         metadata.getColumns(
                                 catalog,
                                 schema,
                                 table,
                                 "%"
                         )) {

                while (rs.next()) {

                    ColumnDTO column =
                            new ColumnDTO();

                    String columnName =
                            rs.getString("COLUMN_NAME");

                    column.setColumnName(columnName);

                    column.setDataType(
                            rs.getString("TYPE_NAME")
                    );

                    column.setSqlType(
                            getSqlTypeName(
                                    rs.getInt("DATA_TYPE")
                            )
                    );

                    column.setSize(
                            rs.getInt("COLUMN_SIZE")
                    );

                    column.setDecimalDigits(
                            rs.getInt("DECIMAL_DIGITS")
                    );

                    column.setNullable(
                            "YES".equalsIgnoreCase(
                                    rs.getString("IS_NULLABLE")
                            )
                    );

                    column.setDefaultValue(
                            rs.getString("COLUMN_DEF")
                    );

                    column.setAutoIncrement(
                            "YES".equalsIgnoreCase(
                                    rs.getString("IS_AUTOINCREMENT")
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

                    columns.add(column);
                }
            }
        }

        return columns;
    }
    private String getSqlTypeName(int sqlType) {

        switch (sqlType) {

            case Types.BIGINT:
                return "BIGINT";

            case Types.INTEGER:
                return "INTEGER";

            case Types.SMALLINT:
                return "SMALLINT";

            case Types.TINYINT:
                return "TINYINT";

            case Types.VARCHAR:
                return "VARCHAR";

            case Types.CHAR:
                return "CHAR";

            case Types.LONGVARCHAR:
                return "LONGVARCHAR";

            case Types.DATE:
                return "DATE";

            case Types.TIME:
                return "TIME";

            case Types.TIMESTAMP:
                return "TIMESTAMP";

            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP WITH TIME ZONE";

            case Types.BOOLEAN:
                return "BOOLEAN";

            case Types.DECIMAL:
                return "DECIMAL";

            case Types.NUMERIC:
                return "NUMERIC";

            case Types.DOUBLE:
                return "DOUBLE";

            case Types.FLOAT:
                return "FLOAT";

            case Types.REAL:
                return "REAL";

            case Types.BINARY:
                return "BINARY";

            case Types.VARBINARY:
                return "VARBINARY";

            case Types.LONGVARBINARY:
                return "LONGVARBINARY";

            case Types.BLOB:
                return "BLOB";

            case Types.CLOB:
                return "CLOB";

            case Types.LONGNVARCHAR:
                return "LONGNVARCHAR";

            case Types.NVARCHAR:
                return "NVARCHAR";

            case Types.NCHAR:
                return "NCHAR";

            case Types.SQLXML:
                return "SQLXML";

            default:
                return "UNKNOWN";
        }
    }
    private Set<String> getPrimaryKeyColumns(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String table
    ) throws SQLException {

        Set<String> columns = new HashSet<>();

        try (ResultSet rs =
                     metadata.getPrimaryKeys(
                             catalog,
                             schema,
                             table
                     )) {

            while (rs.next()) {

                String column =
                        rs.getString("COLUMN_NAME");

                if (column != null) {
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

        Set<String> columns = new HashSet<>();

        try (ResultSet rs =
                     metadata.getImportedKeys(
                             catalog,
                             schema,
                             table
                     )) {

            while (rs.next()) {

                String column =
                        rs.getString("FKCOLUMN_NAME");

                if (column != null) {
                    columns.add(
                            normalize(column)
                    );
                }
            }
        }

        return columns;
    }
    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replace("\"", "")
                .replace("`", "")
                .replace("[", "]")
                .toLowerCase();
    }
}