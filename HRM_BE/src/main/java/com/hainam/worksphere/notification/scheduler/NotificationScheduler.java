package com.hainam.worksphere.notification.scheduler;

import com.hainam.worksphere.attendance.domain.Attendance;
import com.hainam.worksphere.attendance.repository.AttendanceRepository;
import com.hainam.worksphere.notification.domain.NotificationType;
import com.hainam.worksphere.notification.service.NotificationService;
import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import com.hainam.worksphere.workshift.repository.EmployeeWorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final EmployeeWorkShiftRepository employeeWorkShiftRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    // Run every minute
    @Scheduled(cron = "0 * * * * *")
    public void sendWorkShiftReminders() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<EmployeeWorkShift> shiftsToday = employeeWorkShiftRepository.findActiveByDate(today);

        for (EmployeeWorkShift ews : shiftsToday) {
            LocalTime startTime = ews.getWorkShift().getStartTime();
            if (startTime == null) continue;

            // 1. Sắp đến giờ làm (trước 15 phút)
            if (now.isAfter(startTime.minusMinutes(16)) && now.isBefore(startTime.minusMinutes(14))) {
                if (ews.getEmployee().getUser() != null) {
                    notificationService.sendNotification(
                        ews.getEmployee().getUser().getId(),
                        NotificationType.REMINDER,
                        "Upcoming Shift Reminder",
                        String.format("Your shift '%s' starts in 15 minutes.", ews.getWorkShift().getName())
                    );
                }
            }

            // 2. Nhắc nhở chấm công (nếu user chưa check-in sau 15 phút kể từ lúc bắt đầu)
            if (now.isAfter(startTime.plusMinutes(14)) && now.isBefore(startTime.plusMinutes(16))) {
                Optional<Attendance> attendance = attendanceRepository.findActiveByEmployeeIdAndWorkDateAndWorkShift(
                    ews.getEmployee().getId(), today, ews.getWorkShift().getId());
                
                if (attendance.isEmpty() || attendance.get().getCheckInTime() == null) {
                    if (ews.getEmployee().getUser() != null) {
                        notificationService.sendNotification(
                            ews.getEmployee().getUser().getId(),
                            NotificationType.REMINDER,
                            "Check-in Reminder",
                            String.format("You haven't checked in for your shift '%s'. Please check in as soon as possible.", ews.getWorkShift().getName())
                        );
                    }
                }
            }
        }
    }
}
