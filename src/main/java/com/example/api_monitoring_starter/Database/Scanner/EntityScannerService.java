package com.example.api_monitoring_starter.Database.Scanner;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EntityScannerService {

    private final EntityManagerFactory entityManagerFactory;

    private final Map<String, String> entityMappings = new HashMap<>();

    public EntityScannerService(
            EntityManagerFactory entityManagerFactory
    ) {
        this.entityManagerFactory = entityManagerFactory;

        loadEntityMappings();
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