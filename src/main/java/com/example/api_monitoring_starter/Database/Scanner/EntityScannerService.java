package com.example.api_monitoring_starter.Database.Scanner;

import org.springframework.stereotype.Service;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


public class EntityScannerService {


    private final Map<String,String> entityMapping =
            new HashMap<>();


    public EntityScannerService() {

        scanEntities();

    }



    private void scanEntities() {


        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);


        scanner.addIncludeFilter(
                new AnnotationTypeFilter(Entity.class)
        );


        // Scan application models
        scanner.findCandidateComponents(
                        "com.microlearning.api.model"
                )
                .forEach(bean -> {


                    try {

                        Class<?> clazz =
                                Class.forName(bean.getBeanClassName());


                        Table table =
                                clazz.getAnnotation(Table.class);


                        if(table != null) {


                            String tableName =
                                    table.name().toUpperCase();


                            entityMapping.put(
                                    tableName,
                                    clazz.getSimpleName()
                            );

                        }

                    } catch(Exception e) {

                        e.printStackTrace();

                    }


                });


    }



    public String getEntityName(String tableName){

        return entityMapping.get(
                tableName.toUpperCase()
        );

    }


}