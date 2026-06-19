package com.sanfrancisco.api.modules.dashboard.controller;

import com.sanfrancisco.api.modules.dashboard.dto.response.DashboardResponse;
import com.sanfrancisco.api.modules.dashboard.service.DashboardService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<DashboardResponse> get() {
        return ApiResponse.ok(service.build());
    }
}
