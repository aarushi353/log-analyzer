package com.aiproject.loganalyzer.controller;

import com.aiproject.loganalyzer.dto.LogRequest;
import com.aiproject.loganalyzer.dto.LogResponse;
import com.aiproject.loganalyzer.service.LogAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    private final LogAnalysisService logAnalysisService;

    @PostMapping("/analyze")
    public LogResponse analyze(@Valid @RequestBody LogRequest request) {
        return logAnalysisService.analyze(request);
    }
}
