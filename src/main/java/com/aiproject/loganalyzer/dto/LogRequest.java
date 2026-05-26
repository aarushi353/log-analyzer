package com.aiproject.loganalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogRequest {

    @NotBlank(message = "Logs cannot be empty")
    private String logs;
}
