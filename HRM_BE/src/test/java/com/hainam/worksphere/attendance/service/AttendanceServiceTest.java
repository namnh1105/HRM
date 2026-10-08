package com.hainam.worksphere.attendance.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.attendance.domain.Attendance;
import com.hainam.worksphere.attendance.domain.AttendanceStatus;
import com.hainam.worksphere.attendance.dto.request.CheckInRequest;
import com.hainam.worksphere.attendance.dto.request.CheckOutRequest;
import com.hainam.worksphere.attendance.dto.response.AttendanceResponse;
import com.hainam.worksphere.attendance.mapper.AttendanceMapper;
import com.hainam.worksphere.attendance.repository.AttendanceRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.util.FaceApiClient;
import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.repository.EmployeeWorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("AttendanceService Tests")
class AttendanceServiceTest extends BaseUnitTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeWorkShiftRepository employeeWorkShiftRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @Mock
    private FaceApiClient faceApiClient;

    @Mock
    private MultipartFile mockPhoto;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;
    private WorkShift testWorkShift;
    private Attendance testAttendance;
    private AttendanceResponse testAttendanceResponse;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        testEmployee = TestFixtures.createTestEmployee();
        employeeId = testEmployee.getId();

        // Create work shift that always covers the current time (±2 hours)
        LocalTime now = LocalTime.now();
        testWorkShift = WorkShift.builder()
                .name("Test Shift")
                .code("TEST")
                .startTime(now.minusHours(2))
                .endTime(now.plusHours(2))
                .breakDuration(1.0)
                .totalHours(4.0)
                .isActive(true)
                .isNightShift(false)
                .isDeleted(false)
                .build();
        testWorkShift.setId(UUID.randomUUID());

        testAttendance = TestFixtures.createTestAttendance();
        testAttendance.setEmployee(testEmployee);
        testAttendance.setWorkShift(testWorkShift);

        testAttendanceResponse = AttendanceResponse.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .employeeName(testEmployee.getFullName())
                .workDate(LocalDate.now())
                .checkInTime(LocalTime.of(8, 0))
                .status(AttendanceStatus.PRESENT)
                .workingHours(8.0)
                .build();
    }

    // ── Check-in tests ──────────────────────────────────────────────

    @Test
    @DisplayName("Should throw ValidationException when face does not match")
    void shouldThrowWhenFaceDoesNotMatch() {
        // Given
        CheckInRequest request = CheckInRequest.builder()
                .latitude(10.762622)
                .longitude(106.660172)
                .note("On time")
                .build();

        when(faceApiClient.verifyFace(mockPhoto, employeeId.toString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> attendanceService.checkIn(employeeId, request, mockPhoto, TEST_IP_ADDRESS))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Khuôn mặt không khớp");

        verify(faceApiClient).verifyFace(mockPhoto, employeeId.toString());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when no shift assigned for today")
    void shouldThrowWhenNoShiftAssigned() {
        // Given
        CheckInRequest request = CheckInRequest.builder().build();

        when(faceApiClient.verifyFace(mockPhoto, employeeId.toString())).thenReturn(true);
        when(employeeWorkShiftRepository.findActiveByEmployeeIdAndDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> attendanceService.checkIn(employeeId, request, mockPhoto, TEST_IP_ADDRESS))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("không có ca làm việc");

        verify(faceApiClient).verifyFace(mockPhoto, employeeId.toString());
    }

    @Test
    @DisplayName("Should throw ValidationException when already checked in for this shift")
    void shouldThrowWhenAlreadyCheckedInForShift() {
        // Given
        CheckInRequest request = CheckInRequest.builder().build();

        EmployeeWorkShift assignment = EmployeeWorkShift.builder()
                .workShift(testWorkShift)
                .build();

        Attendance existingAttendance = Attendance.builder()
                .employee(testEmployee)
                .workDate(LocalDate.now())
                .checkInTime(LocalTime.of(8, 0))
                .workShift(testWorkShift)
                .isDeleted(false)
                .status(AttendanceStatus.PRESENT)
                .build();


        when(faceApiClient.verifyFace(mockPhoto, employeeId.toString())).thenReturn(true);
        when(employeeWorkShiftRepository.findActiveByEmployeeIdAndDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(List.of(assignment));
        when(attendanceRepository.findActiveByEmployeeIdAndWorkDateAndWorkShift(
                eq(employeeId), any(LocalDate.class), eq(testWorkShift.getId())))
                .thenReturn(Optional.of(existingAttendance));

        // When & Then
        assertThatThrownBy(() -> attendanceService.checkIn(employeeId, request, mockPhoto, TEST_IP_ADDRESS))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("đã check-in");
    }

    // ── Check-out tests ─────────────────────────────────────────────

    @Test
    @DisplayName("Should throw ValidationException when face does not match on checkout")
    void shouldThrowWhenFaceDoesNotMatchOnCheckout() {
        // Given
        CheckOutRequest request = CheckOutRequest.builder().build();

        when(faceApiClient.verifyFace(mockPhoto, employeeId.toString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> attendanceService.checkOut(employeeId, request, mockPhoto, TEST_IP_ADDRESS))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Khuôn mặt không khớp");

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw when no open attendance found for checkout")
    void shouldThrowWhenNoOpenAttendanceForCheckout() {
        // Given
        CheckOutRequest request = CheckOutRequest.builder().build();

        when(faceApiClient.verifyFace(mockPhoto, employeeId.toString())).thenReturn(true);
        when(attendanceRepository.findOpenAttendances(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> attendanceService.checkOut(employeeId, request, mockPhoto, TEST_IP_ADDRESS))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should checkout successfully with single open attendance (fallback)")
    void shouldCheckoutWithSingleOpenAttendance() {
        // Given
        CheckOutRequest request = CheckOutRequest.builder()
                .latitude(10.762622)
                .longitude(106.660172)
                .note("Done")
                .build();

        Attendance openAttendance = Attendance.builder()
                .id(UUID.randomUUID())
                .employee(testEmployee)
                .workDate(LocalDate.now())
                .checkInTime(LocalTime.of(8, 0))
                .status(AttendanceStatus.PRESENT)
                .lateMinutes(0)
                .workShift(testWorkShift)
                .isDeleted(false)
                .build();

        when(faceApiClient.verifyFace(mockPhoto, employeeId.toString())).thenReturn(true);
        when(attendanceRepository.findOpenAttendances(eq(employeeId), any(LocalDate.class)))
                .thenReturn(List.of(openAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(openAttendance);
        when(attendanceMapper.toAttendanceResponse(openAttendance)).thenReturn(testAttendanceResponse);

        // When
        AttendanceResponse result = attendanceService.checkOut(employeeId, request, mockPhoto, TEST_IP_ADDRESS);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(openAttendance.getCheckOutTime()).isNotNull(),
            () -> assertThat(openAttendance.getCheckOutIp()).isEqualTo(TEST_IP_ADDRESS),
            () -> assertThat(openAttendance.getWorkingHours()).isNotNull(),
            () -> verify(attendanceRepository).save(any(Attendance.class)),
            () -> verify(attendanceMapper).toAttendanceResponse(openAttendance)
        );
    }

    // ── Query tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should get today attendance successfully")
    void shouldGetTodayAttendanceSuccessfully() {
        // Given
        when(attendanceRepository.findActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.of(testAttendance));
        when(attendanceMapper.toAttendanceResponse(testAttendance)).thenReturn(testAttendanceResponse);

        // When
        Optional<AttendanceResponse> result = attendanceService.getTodayAttendance(employeeId);

        // Then
        assertAll(
            () -> assertThat(result).isPresent(),
            () -> assertThat(result.get().getEmployeeId()).isEqualTo(employeeId),
            () -> verify(attendanceRepository).findActiveByEmployeeIdAndWorkDate(eq(employeeId), any(LocalDate.class)),
            () -> verify(attendanceMapper).toAttendanceResponse(testAttendance)
        );
    }

    @Test
    @DisplayName("Should get attendance history successfully")
    void shouldGetAttendanceHistorySuccessfully() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        Attendance attendance1 = TestFixtures.createTestAttendance();
        attendance1.setEmployee(testEmployee);
        Attendance attendance2 = TestFixtures.createTestAttendance();
        attendance2.setEmployee(testEmployee);

        List<Attendance> attendances = Arrays.asList(attendance1, attendance2);
        Page<Attendance> attendancePage = new PageImpl<>(attendances, pageable, attendances.size());

        when(attendanceRepository.findActiveByEmployeeIdAndWorkDateBetween(eq(employeeId), eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(attendancePage);
        when(attendanceMapper.toAttendanceResponse(any(Attendance.class))).thenReturn(testAttendanceResponse);

        // When
        Page<AttendanceResponse> result = attendanceService.getAttendanceHistory(employeeId, startDate, endDate, pageable);

        // Then
        assertAll(
            () -> assertThat(result.getContent()).hasSize(2),
            () -> verify(attendanceRepository).findActiveByEmployeeIdAndWorkDateBetween(eq(employeeId), eq(startDate), eq(endDate), eq(pageable)),
            () -> verify(attendanceMapper, times(2)).toAttendanceResponse(any(Attendance.class))
        );
    }
}
