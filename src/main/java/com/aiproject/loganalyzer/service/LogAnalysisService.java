package com.aiproject.loganalyzer.service;

import com.aiproject.loganalyzer.dto.LogRequest;
import com.aiproject.loganalyzer.dto.LogResponse;

public interface LogAnalysisService {
    LogResponse analyze(LogRequest logs);
}
