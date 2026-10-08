package com.hainam.worksphere.attendance.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import com.hainam.worksphere.employee.service.EmployeeService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.util.FaceApiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/face")
@RequiredArgsConstructor
@Tag(name = "Face Registration")
@SecurityRequirement(name = "Bearer Authentication")
public class FaceController {

    private final FaceApiClient faceApiClient;
    private final EmployeeService employeeService;

    @GetMapping("/status")
    @Operation(summary = "Get current employee's face registration status")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFaceStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        Map<String, Object> status = faceApiClient.getFaceStatus(employee.getId().toString());
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PutMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register face with video")
    @RequirePermission(PermissionType.CREATE_ATTENDANCE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerFace(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("video") MultipartFile video
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        Map<String, Object> result = faceApiClient.registerFace(video, employee.getId().toString());
        return ResponseEntity.ok(ApiResponse.success("Đăng ký khuôn mặt đang được xử lý", result));
    }

    @GetMapping("/register/status/{jobId}")
    @Operation(summary = "Get face registration job status")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRegistrationJobStatus(
            @PathVariable String jobId
    ) {
        Map<String, Object> status = faceApiClient.getRegistrationJobStatus(jobId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
