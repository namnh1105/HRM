package com.hainam.worksphere.attendance.controller;

import com.hainam.worksphere.attendance.dto.request.CheckInRequest;
import com.hainam.worksphere.attendance.dto.request.CheckOutRequest;
import com.hainam.worksphere.attendance.dto.response.AttendanceResponse;
import com.hainam.worksphere.attendance.service.AttendanceService;
import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import com.hainam.worksphere.employee.service.EmployeeService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.dto.PaginatedApiResponse;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.shared.util.IpAddressUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance Management")
@SecurityRequirement(name = "Bearer Authentication")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;

    @PostMapping(value = "/check-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Check in with face verification")
    @RequirePermission(PermissionType.CREATE_ATTENDANCE)
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("photo") MultipartFile photo,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "note", required = false) String note,
            HttpServletRequest httpRequest
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        String ipAddress = IpAddressUtil.getClientIp(httpRequest);

        CheckInRequest request = CheckInRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .note(note)
                .build();

        AttendanceResponse response = attendanceService.checkIn(employee.getId(), request, photo, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Check-in thành công", response));
    }

    @PostMapping(value = "/check-out", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Check out with face verification")
    @RequirePermission(PermissionType.UPDATE_ATTENDANCE)
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("photo") MultipartFile photo,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "note", required = false) String note,
            HttpServletRequest httpRequest
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        String ipAddress = IpAddressUtil.getClientIp(httpRequest);

        CheckOutRequest request = CheckOutRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .note(note)
                .build();

        AttendanceResponse response = attendanceService.checkOut(employee.getId(), request, photo, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Check-out thành công", response));
    }

    @GetMapping("/me/today")
    @Operation(summary = "Get current employee's today attendance")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<ApiResponse<AttendanceResponse>> getTodayAttendance(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        Optional<AttendanceResponse> response = attendanceService.getTodayAttendance(employee.getId());
        return ResponseEntity.ok(ApiResponse.success(response.orElse(null)));
    }

    @GetMapping("/me/history")
    @Operation(summary = "Get current employee's attendance history")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<PaginatedApiResponse<AttendanceResponse>> getMyAttendanceHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        Page<AttendanceResponse> response = attendanceService.getAttendanceHistory(employee.getId(), startDate, endDate, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}/history")
    @Operation(summary = "Get employee's attendance history (admin)")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<PaginatedApiResponse<AttendanceResponse>> getEmployeeAttendanceHistory(
            @PathVariable UUID employeeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<AttendanceResponse> response = attendanceService.getAttendanceHistory(employeeId, startDate, endDate, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get all attendances by store")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<PaginatedApiResponse<AttendanceResponse>> getAttendancesByStore(
            @PathVariable UUID storeId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<AttendanceResponse> response = attendanceService.getAttendancesByStore(storeId, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}/history")
    @Operation(summary = "Get attendance history by store and date range")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<PaginatedApiResponse<AttendanceResponse>> getAttendancesByStoreAndDateRange(
            @PathVariable UUID storeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<AttendanceResponse> response = attendanceService.getAttendancesByStoreAndDateRange(storeId, startDate, endDate, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}/today")
    @Operation(summary = "Get today's attendances by store")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<PaginatedApiResponse<AttendanceResponse>> getAttendancesByStoreToday(
            @PathVariable UUID storeId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<AttendanceResponse> response = attendanceService.getAttendancesByStoreAndDate(storeId, LocalDate.now(), pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get attendance statistics")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<ApiResponse<ResourceStatsResponse>> getAttendanceStats() {
        ResourceStatsResponse stats = attendanceService.getAttendanceStats();
        return ResponseEntity.ok(ApiResponse.success("Attendance statistics retrieved successfully", stats));
    }
}
