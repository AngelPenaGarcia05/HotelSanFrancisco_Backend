package com.sanfrancisco.api.modules.auditoria.controller;

import com.sanfrancisco.api.modules.auditoria.dto.request.AuditoriaFilterRequest;
import com.sanfrancisco.api.modules.auditoria.dto.response.RegistroAuditoriaResponse;
import com.sanfrancisco.api.modules.auditoria.service.AuditoriaService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<RegistroAuditoriaResponse>> search(
            AuditoriaFilterRequest filter,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.search(filter, pageable)));
    }
}
