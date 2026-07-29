package com.example.api_monitoring_starter.Database.Scanner;


import com.example.api_monitoring_starter.Database.DTO.ColumnValidationDTO;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import jakarta.validation.constraints.*;

import java.lang.reflect.Field;
import java.util.*;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;



@Lazy
public class EntityScannerService {


    private final EntityManagerFactory entityManagerFactory;


    private final Map<String,String> entityMappings =
            new HashMap<>();


    private final Map<String,Map<String,ColumnValidationDTO>> columnMappings =
            new HashMap<>();


    private boolean loaded = false;



    public EntityScannerService(
            EntityManagerFactory entityManagerFactory
    ){

        this.entityManagerFactory =
                entityManagerFactory;

    }



    private synchronized void initialize(){

        if(loaded){
            return;
        }


        loadEntityMappings();

        loaded = true;

    }




    private void loadEntityMappings(){


        Metamodel metamodel =
                entityManagerFactory
                        .getMetamodel();



        for(EntityType<?> entity :
                metamodel.getEntities()){


            Class<?> clazz =
                    entity.getJavaType();



            String entityName =
                    entity.getName();



            Table table =
                    clazz.getAnnotation(
                            Table.class
                    );



            String tableName =
                    table != null &&
                            !table.name().isBlank()
                            ?
                            table.name()
                            :
                            entityName;



            /*
             * Store different possible names
             */

            entityMappings.put(
                    normalize(tableName),
                    entityName
            );


            entityMappings.put(
                    normalize(clazz.getSimpleName()),
                    entityName
            );


            entityMappings.put(
                    normalize(entityName),
                    entityName
            );



            Map<String,ColumnValidationDTO> columns =
                    new HashMap<>();



            for(Field field :
                    clazz.getDeclaredFields()){


                ColumnValidationDTO dto =
                        new ColumnValidationDTO();



                Column column =
                        field.getAnnotation(
                                Column.class
                        );



                if(column != null){

                    dto.setNullable(
                            column.nullable()
                    );

                }



                List<String> validations =
                        dto.getValidations();



                if(field.isAnnotationPresent(NotNull.class))
                    validations.add("@NotNull");


                if(field.isAnnotationPresent(NotBlank.class))
                    validations.add("@NotBlank");


                if(field.isAnnotationPresent(NotEmpty.class))
                    validations.add("@NotEmpty");


                if(field.isAnnotationPresent(Email.class))
                    validations.add("@Email");



                Size size =
                        field.getAnnotation(
                                Size.class
                        );


                if(size != null){

                    validations.add(
                            "@Size("+
                                    size.min()+
                                    ","+
                                    size.max()+
                                    ")"
                    );

                }



                Min min =
                        field.getAnnotation(
                                Min.class
                        );


                if(min != null){

                    validations.add(
                            "@Min("+
                                    min.value()+
                                    ")"
                    );

                }



                Max max =
                        field.getAnnotation(
                                Max.class
                        );


                if(max != null){

                    validations.add(
                            "@Max("+
                                    max.value()+
                                    ")"
                    );

                }



                String fieldName =
                        field.getName();



                /*
                 * Store Java field name
                 */

                columns.put(
                        normalize(fieldName),
                        dto
                );



                /*
                 * Store database column name
                 */

                if(column != null &&
                        !column.name().isBlank()){


                    columns.put(
                            normalize(column.name()),
                            dto
                    );

                }

            }



            columnMappings.put(
                    normalize(tableName),
                    columns
            );


        }

    }




    public String getEntityName(
            String tableName
    ){

        initialize();


        return entityMappings.get(
                normalize(tableName)
        );

    }




    public ColumnValidationDTO getColumnInfo(
            String table,
            String column
    ){

        initialize();


        Map<String,ColumnValidationDTO> cols =
                columnMappings.get(
                        normalize(table)
                );


        if(cols == null){
            return null;
        }


        return cols.get(
                normalize(column)
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