package com.hainam.worksphere.attendance.service;

import com.hainam.worksphere.attendance.domain.Attendance;
import com.hainam.worksphere.attendance.domain.AttendanceStatus;
import com.hainam.worksphere.attendance.dto.request.CheckInRequest;
import com.hainam.worksphere.attendance.dto.request.CheckOutRequest;
import com.hainam.worksphere.attendance.dto.response.AttendanceResponse;
import com.hainam.worksphere.attendance.mapper.AttendanceMapper;
import com.hainam.worksphere.attendance.repository.AttendanceRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.shared.exception.AttendanceNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.util.FaceApiClient;
import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.repository.EmployeeWorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkShiftRepository employeeWorkShiftRepository;
    private final AttendanceMapper attendanceMapper;
    private final FaceApiClient faceApiClient;

    /** Buffer cho phép checkin sớm / checkout trễ so với ca (phút) */
    private static final int CHECKIN_EARLY_BUFFER = 240; // 4 hours
    private static final int CHECKOUT_LATE_BUFFER = 240; // 4 hours

    // ── Check-in ────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = CacheConfig.ATTENDANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "ATTENDANCE")
    public AttendanceResponse checkIn(UUID employeeId, CheckInRequest request,
                                      MultipartFile photo, String ipAddress) {

        // 1. Verify face via internal Python API
        boolean faceMatched = faceApiClient.verifyFace(photo, employeeId.toString());
        if (!faceMatched) {
            throw new ValidationException("Khuôn mặt không khớp với nhân viên đã đăng ký");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 2. Find matching work shift for current time
        WorkShift matchingShift = findMatchingShift(employeeId, today, now);

        // 3. Check if already checked in for this shift
        Optional<Attendance> existing = attendanceRepository
                .findActiveByEmployeeIdAndWorkDateAndWorkShift(employeeId, today, matchingShift.getId());
        if (existing.isPresent() && existing.get().getCheckInTime() != null) {
            throw new ValidationException(
                    "Nhân viên đã check-in cho ca '" + matchingShift.getName() +
                    "' hôm nay lúc " + existing.get().getCheckInTime()
            );
        }

        // 4. Calculate late minutes
        int lateMinutes = calculateLateMinutes(now, matchingShift.getStartTime());
        AttendanceStatus status = lateMinutes > 0 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;

        // 5. Create or update attendance record
        Attendance attendance;
        if (existing.isPresent()) {
            attendance = existing.get();
            attendance.setCheckInTime(now);
            attendance.setCheckInIp(ipAddress);
            attendance.setCheckInLatitude(request.getLatitude());
            attendance.setCheckInLongitude(request.getLongitude());
            attendance.setLateMinutes(lateMinutes);
            attendance.setStatus(status);
        } else {
            // Fetch employee to get store
            Employee emp = employeeRepository.findActiveById(employeeId)
                    .orElse(Employee.builder().id(employeeId).build());

            attendance = Attendance.builder()
                    .employee(emp)
                    .workDate(today)
                    .checkInTime(now)
                    .checkInIp(ipAddress)
                    .checkInLatitude(request.getLatitude())
                    .checkInLongitude(request.getLongitude())
                    .lateMinutes(lateMinutes)
                    .status(status)
                    .workShift(matchingShift)
                    .store(emp.getStore())
                    .note(request.getNote())
                    .build();
        }

        Attendance saved = attendanceRepository.save(attendance);
        AuditContext.registerCreated(saved);
        return attendanceMapper.toAttendanceResponse(saved);
    }

    // ── Check-out ───────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = CacheConfig.ATTENDANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "ATTENDANCE")
    public AttendanceResponse checkOut(UUID employeeId, CheckOutRequest request,
                                       MultipartFile photo, String ipAddress) {

        // 1. Verify face
        boolean faceMatched = faceApiClient.verifyFace(photo, employeeId.toString());
        if (!faceMatched) {
            throw new ValidationException("Khuôn mặt không khớp với nhân viên đã đăng ký");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 2. Find open attendance records (checked in but not checked out)
        List<Attendance> openAttendances = attendanceRepository.findOpenAttendances(employeeId, today);
        if (openAttendances.isEmpty()) {
            throw AttendanceNotFoundException.byId(
                    "Không tìm thấy ca nào đã check-in mà chưa check-out hôm nay"
            );
        }

        // 3. Find the attendance whose shift matches current checkout time
        Attendance attendance = null;
        WorkShift workShift = null;

        for (Attendance att : openAttendances) {
            if (att.getWorkShift() != null) {
                WorkShift shift = att.getWorkShift();
                if (isTimeInShiftWindow(now, shift.getStartTime(), shift.getEndTime())) {
                    attendance = att;
                    workShift = shift;
                    break;
                }
            }
        }

        // Fallback: if only one open attendance, use it
        if (attendance == null && openAttendances.size() == 1) {
            attendance = openAttendances.get(0);
            workShift = attendance.getWorkShift();
        }

        if (attendance == null) {
            String shiftNames = openAttendances.stream()
                    .filter(a -> a.getWorkShift() != null)
                    .map(a -> a.getWorkShift().getName() + " (check-in: " + a.getCheckInTime() + ")")
                    .collect(Collectors.joining(", "));
            throw new ValidationException(
                    "Thời gian hiện tại không khớp với ca nào đang mở. Các ca đang chờ checkout: " + shiftNames
            );
        }

        AuditContext.snapshot(attendance);

        // 4. Calculate working hours and early leave
        int earlyLeaveMinutes = 0;
        double workingHours = calculateWorkingHours(attendance.getCheckInTime(), now, 1.0);

        if (workShift != null) {
            earlyLeaveMinutes = calculateEarlyLeaveMinutes(now, workShift.getEndTime());
            double breakDuration = workShift.getBreakDuration() != null ? workShift.getBreakDuration() : 1.0;
            workingHours = calculateWorkingHours(attendance.getCheckInTime(), now, breakDuration);

            // Update status
            if (attendance.getLateMinutes() > 0 && earlyLeaveMinutes > 0) {
                attendance.setStatus(AttendanceStatus.HALF_DAY);
            } else if (earlyLeaveMinutes > 0) {
                attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
            }
        }

        // 5. Update attendance record
        attendance.setCheckOutTime(now);
        attendance.setCheckOutIp(ipAddress);
        attendance.setCheckOutLatitude(request.getLatitude());
        attendance.setCheckOutLongitude(request.getLongitude());
        attendance.setEarlyLeaveMinutes(earlyLeaveMinutes);
        attendance.setWorkingHours(Math.round(workingHours * 100.0) / 100.0);

        if (request.getNote() != null) {
            attendance.setNote(request.getNote());
        }

        Attendance saved = attendanceRepository.save(attendance);
        AuditContext.registerUpdated(saved);
        return attendanceMapper.toAttendanceResponse(saved);
    }

    // ── Queries ─────────────────────────────────────────────────────

    @Cacheable(value = CacheConfig.ATTENDANCE_CACHE,
               key = "'history:' + #employeeId + ':' + #startDate + ':' + #endDate")
    public Page<AttendanceResponse> getAttendanceHistory(UUID employeeId,
                                                         LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return attendanceRepository.findActiveByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate, pageable)
                .map(attendanceMapper::toAttendanceResponse);
    }

    @Cacheable(value = CacheConfig.ATTENDANCE_CACHE, key = "'today:' + #employeeId")
    public Optional<AttendanceResponse> getTodayAttendance(UUID employeeId) {
        return attendanceRepository.findActiveByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .map(attendanceMapper::toAttendanceResponse);
    }

    public Page<AttendanceResponse> getAttendancesByStore(UUID storeId, Pageable pageable) {
        return attendanceRepository.findActiveByStoreId(storeId, pageable)
                .map(attendanceMapper::toAttendanceResponse);
    }

    public Page<AttendanceResponse> getAttendancesByStoreAndDateRange(UUID storeId,
                                                                       LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return attendanceRepository.findActiveByStoreIdAndWorkDateBetween(storeId, startDate, endDate, pageable)
                .map(attendanceMapper::toAttendanceResponse);
    }

    public Page<AttendanceResponse> getAttendancesByStoreAndDate(UUID storeId, LocalDate workDate, Pageable pageable) {
        return attendanceRepository.findActiveByStoreIdAndWorkDate(storeId, workDate, pageable)
                .map(attendanceMapper::toAttendanceResponse);
    }

    // ── Shift matching ──────────────────────────────────────────────

    private WorkShift findMatchingShift(UUID employeeId, LocalDate date, LocalTime currentTime) {
        List<EmployeeWorkShift> assignments = employeeWorkShiftRepository
                .findActiveByEmployeeIdAndDate(employeeId, date);

        if (assignments.isEmpty()) {
            throw new ValidationException("Nhân viên không có ca làm việc được phân công cho hôm nay");
        }

        // 1. Try finding a shift that strictly matches the window
        for (EmployeeWorkShift assignment : assignments) {
            WorkShift shift = assignment.getWorkShift();
            if (shift != null && Boolean.TRUE.equals(shift.getIsActive())
                    && !Boolean.TRUE.equals(shift.getIsDeleted())
                    && isTimeInShiftWindow(currentTime, shift.getStartTime(), shift.getEndTime())) {
                return shift;
            }
        }

        // 2. If no strict match but there is only ONE shift today, just allow it
        if (assignments.size() == 1) {
            WorkShift shift = assignments.get(0).getWorkShift();
            if (shift != null && Boolean.TRUE.equals(shift.getIsActive())
                    && !Boolean.TRUE.equals(shift.getIsDeleted())) {
                return shift;
            }
        }

        // 3. If multiple shifts, find the closest one
        WorkShift closestShift = null;
        long minDiff = Long.MAX_VALUE;
        for (EmployeeWorkShift assignment : assignments) {
            WorkShift shift = assignment.getWorkShift();
            if (shift != null && Boolean.TRUE.equals(shift.getIsActive())
                    && !Boolean.TRUE.equals(shift.getIsDeleted())) {
                long diffToStart = Math.abs(Duration.between(currentTime, shift.getStartTime()).toMinutes());
                long diffToEnd = Math.abs(Duration.between(currentTime, shift.getEndTime()).toMinutes());
                long min = Math.min(diffToStart, diffToEnd);
                if (min < minDiff) {
                    minDiff = min;
                    closestShift = shift;
                }
            }
        }
        
        if (closestShift != null) {
            return closestShift;
        }

        String shiftNames = assignments.stream()
                .map(EmployeeWorkShift::getWorkShift)
                .filter(s -> s != null && Boolean.TRUE.equals(s.getIsActive()))
                .map(s -> s.getName() + " (" + s.getStartTime() + "-" + s.getEndTime() + ")")
                .collect(Collectors.joining(", "));

        throw new ValidationException(
                "Thời gian hiện tại không nằm trong khoảng cho phép của bất kỳ ca nào. " +
                "Các ca hôm nay: " + shiftNames
        );
    }

    private boolean isTimeInShiftWindow(LocalTime currentTime, LocalTime shiftStart, LocalTime shiftEnd) {
        LocalTime windowStart = shiftStart.minusMinutes(CHECKIN_EARLY_BUFFER);
        LocalTime windowEnd = shiftEnd.plusMinutes(CHECKOUT_LATE_BUFFER);

        // Overnight window (crosses midnight), e.g. 22:00 -> 06:00
        if (windowStart.isAfter(windowEnd)) {
            return !currentTime.isBefore(windowStart) || !currentTime.isAfter(windowEnd);
        }

        return !currentTime.isBefore(windowStart) && !currentTime.isAfter(windowEnd);
    }

    // ── Calculations ────────────────────────────────────────────────

    private int calculateLateMinutes(LocalTime checkInTime, LocalTime shiftStart) {
        if (checkInTime.isAfter(shiftStart)) {
            return (int) Duration.between(shiftStart, checkInTime).toMinutes();
        }
        return 0;
    }

    private int calculateEarlyLeaveMinutes(LocalTime checkOutTime, LocalTime shiftEnd) {
        if (checkOutTime.isBefore(shiftEnd)) {
            return (int) Duration.between(checkOutTime, shiftEnd).toMinutes();
        }
        return 0;
    }

    private double calculateWorkingHours(LocalTime checkIn, LocalTime checkOut, double breakDuration) {
        double totalHours = Duration.between(checkIn, checkOut).toMinutes() / 60.0;
        return Math.max(0, totalHours - breakDuration);
    }

    public ResourceStatsResponse getAttendanceStats() {
        LocalDate today = LocalDate.now();
        return ResourceStatsResponse.builder()
                .total(attendanceRepository.count())
                .active(attendanceRepository.countByWorkDateAndIsDeletedFalse(today))
                .inactive(attendanceRepository.countLateByDate(today))
                .deleted(attendanceRepository.countByIsDeletedTrue())
                .build();
    }
}
