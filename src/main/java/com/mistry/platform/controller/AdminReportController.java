package com.mistry.platform.controller;

import com.mistry.platform.dto.AdminActionLogResponse;
import com.mistry.platform.dto.ReportPeriod;
import com.mistry.platform.dto.SummaryReportResponse;
import com.mistry.platform.entity.AdminActionCategory;
import com.mistry.platform.exception.InvalidReportRequestException;
import com.mistry.platform.service.AdminActionLogService;
import com.mistry.platform.service.AdminReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService reportService;
    private final AdminActionLogService actionLogService;

    public AdminReportController(AdminReportService reportService, AdminActionLogService actionLogService) {
        this.reportService = reportService;
        this.actionLogService = actionLogService;
    }

    @GetMapping("/summary")
    public SummaryReportResponse summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.summary(ReportPeriod.of(from, to));
    }

    @GetMapping("/admin-actions")
    public List<AdminActionLogResponse> adminActions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) AdminActionCategory category) {
        return actionLogService.getLog(ReportPeriod.of(from, to), category);
    }

    @ExceptionHandler(InvalidReportRequestException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRange(InvalidReportRequestException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleBadParameter(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of("message",
                "Invalid value for '" + e.getName()
                        + "'. Dates look like 2026-10-31; category is VERIFICATION, SUSPENSION or SETTINGS."));
    }
}
