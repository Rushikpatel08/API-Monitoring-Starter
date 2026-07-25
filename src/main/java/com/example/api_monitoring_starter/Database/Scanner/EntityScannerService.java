package com.example.api_monitoring_starter.Database.Scanner;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;

import java.util.Set;

public class EntityScannerService {

    private final EntityManager entityManager;

    public EntityScannerService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String getEntityName(String tableName) {

        Set<EntityType<?>> entities =
                entityManager
                        .getMetamodel()
                        .getEntities();

        for (EntityType<?> entity : entities) {

            Class<?> javaType = entity.getJavaType();

            jakarta.persistence.Table table =
                    javaType.getAnnotation(jakarta.persistence.Table.class);

            if (table != null) {

                String entityTableName = table.name();

                if (entityTableName.equalsIgnoreCase(tableName)) {
                    return entity.getName();
                }
            }
        }

        return null;
    }
}