package com.monitoring.api_monitoring_starter.Database.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ColumnValidationDTO {

    private boolean nullable = true;

    private List<String> validations = new ArrayList<>();

}