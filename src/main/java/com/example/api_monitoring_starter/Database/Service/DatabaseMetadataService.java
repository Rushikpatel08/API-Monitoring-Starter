package com.example.api_monitoring_starter.Database.Service;

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