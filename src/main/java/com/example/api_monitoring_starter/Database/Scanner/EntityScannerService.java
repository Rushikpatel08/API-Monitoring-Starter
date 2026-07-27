package com.example.api_monitoring_starter.Database.Scanner;

import com.example.api_monitoring_starter.Database.DTO.ColumnValidationDTO;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.Column;

import jakarta.validation.constraints.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class EntityScannerService {

    private final EntityManagerFactory entityManagerFactory;

    private final Map<String, String> entityMappings = new HashMap<>();

    private final Map<String, Map<String, ColumnValidationDTO>> columnMappings =
            new HashMap<>();

    public EntityScannerService(
            EntityManagerFactory entityManagerFactory
    ) {
        this.entityManagerFactory = entityManagerFactory;

        loadEntityMappings();
    }

    public ColumnValidationDTO getColumnInfo(
            String tableName,
            String columnName
    ) {

        Map<String, ColumnValidationDTO> columns =
                columnMappings.get(
                        normalize(tableName)
                );

        if (columns == null) {
            return null;
        }

        return columns.get(
                normalize(columnName)
        );
    }
    /**
     * Discover all JPA entities dynamically.
     */
    private void loadEntityMappings() {

        Metamodel metamodel =
                entityManagerFactory.getMetamodel();

        Set<EntityType<?>> entities =
                metamodel.getEntities();

        for (EntityType<?> entity : entities) {

            Class<?> javaType =
                    entity.getJavaType();

            String entityName =
                    entity.getName();

            Table table =
                    javaType.getAnnotation(Table.class);

            if (table != null && !table.name().isBlank()) {

                String tableName =
                        table.name();

                entityMappings.put(
                        normalize(tableName),
                        entityName
                );

            }

            /*
             * Also support entities without @Table.
             *
             * Example:
             *
             * @Entity
             * public class Mobile {}
             *
             * JPA defaults the table name from the entity mapping.
             */
            entityMappings.putIfAbsent(
                    normalize(entityName),
                    entityName
            );

            entityMappings.putIfAbsent(
                    normalize(javaType.getSimpleName()),
                    entityName
            );
            Map<String, ColumnValidationDTO> columns = new HashMap<>();

            for (Field field : javaType.getDeclaredFields()) {

                ColumnValidationDTO dto =
                        new ColumnValidationDTO();

                Column column =
                        field.getAnnotation(Column.class);

                if (column != null) {
                    dto.setNullable(column.nullable());
                }

                List<String> validations =
                        dto.getValidations();

                if (field.isAnnotationPresent(NotNull.class))
                    validations.add("@NotNull");

                if (field.isAnnotationPresent(NotBlank.class))
                    validations.add("@NotBlank");

                if (field.isAnnotationPresent(NotEmpty.class))
                    validations.add("@NotEmpty");

                if (field.isAnnotationPresent(Email.class))
                    validations.add("@Email");

                Size size =
                        field.getAnnotation(Size.class);

                if (size != null) {
                    validations.add(
                            "@Size(" +
                                    size.min() +
                                    "," +
                                    size.max() +
                                    ")"
                    );
                }

                Pattern pattern =
                        field.getAnnotation(Pattern.class);

                if (pattern != null) {
                    validations.add("@Pattern");
                }

                Min min =
                        field.getAnnotation(Min.class);

                if (min != null) {
                    validations.add("@Min(" + min.value() + ")");
                }

                Max max =
                        field.getAnnotation(Max.class);

                if (max != null) {
                    validations.add("@Max(" + max.value() + ")");
                }

                String columnName = field.getName();

                if (column != null && !column.name().isBlank()) {
                    columnName = column.name();
                }

                columns.put(
                        normalize(columnName),
                        dto
                );
            }

            String tableKey =
                    table != null && !table.name().isBlank()
                            ? normalize(table.name())
                            : normalize(entityName);

            columnMappings.put(
                    tableKey,
                    columns
            );
        }
    }

    /**
     * Find JPA entity associated with a database table.
     */
    public String getEntityName(String tableName) {

        if (tableName == null) {
            return null;
        }

        return entityMappings.get(
                normalize(tableName)
        );
    }

    /**
     * Normalize database identifiers so that:
     *
     * MOBILE
     * mobile
     * Mobile
     *
     * can all match.
     */
    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replace("\"", "")
                .replace("`", "")
                .replace("[", "")
                .replace("]", "")
                .toLowerCase();
    }
}