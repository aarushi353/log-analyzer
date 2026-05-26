package com.aiproject.loganalyzer.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class LogResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String anomalySummary;
    private List<String> rootCause;
    private List<String> recommendation;
}
