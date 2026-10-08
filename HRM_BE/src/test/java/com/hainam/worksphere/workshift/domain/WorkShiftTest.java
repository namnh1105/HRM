package com.hainam.worksphere.workshift.domain;

import com.hainam.worksphere.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("WorkShift Domain Tests")
class WorkShiftTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create work shift with builder pattern")
    void shouldCreateWorkShiftWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "Morning Shift";
        String code = "MORNING";
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        Double breakDuration = 1.0;
        Double totalHours = 8.0;
        String description = "Regular morning shift";
        Instant now = Instant.now();

        // When
        WorkShift workShift = WorkShift.builder()
                .id(id)
                .name(name)
                .code(code)
                .startTime(startTime)
                .endTime(endTime)
                .breakDuration(breakDuration)
                .totalHours(totalHours)
                .description(description)
                .isActive(true)
                .isNightShift(false)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(workShift.getId()).isEqualTo(id),
                () -> assertThat(workShift.getName()).isEqualTo(name),
                () -> assertThat(workShift.getCode()).isEqualTo(code),
                () -> assertThat(workShift.getStartTime()).isEqualTo(startTime),
                () -> assertThat(workShift.getEndTime()).isEqualTo(endTime),
                () -> assertThat(workShift.getBreakDuration()).isEqualTo(breakDuration),
                () -> assertThat(workShift.getTotalHours()).isEqualTo(totalHours),
                () -> assertThat(workShift.getDescription()).isEqualTo(description),
                () -> assertThat(workShift.getIsActive()).isTrue(),
                () -> assertThat(workShift.getIsNightShift()).isFalse(),
                () -> assertThat(workShift.getIsDeleted()).isFalse(),
                () -> assertThat(workShift.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create work shift with default values")
    void shouldCreateWorkShiftWithDefaultValues() {
        // When
        WorkShift workShift = WorkShift.builder()
                .name("Default Shift")
                .code("DEFAULT")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        // Then
        assertAll(
                () -> assertThat(workShift.getIsActive()).isTrue(),         // Default value
                () -> assertThat(workShift.getIsNightShift()).isFalse(),    // Default value
                () -> assertThat(workShift.getBreakDuration()).isEqualTo(1.0), // Default value
                () -> assertThat(workShift.getIsDeleted()).isFalse()        // Default value
        );
    }

    @Test
    @DisplayName("Should create work shift with no args constructor")
    void shouldCreateWorkShiftWithNoArgsConstructor() {
        // When
        WorkShift workShift = new WorkShift();

        // Then
        assertThat(workShift).isNotNull();
    }

    @Test
    @DisplayName("Should handle time fields")
    void shouldHandleTimeFields() {
        // Given
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        Double totalHours = 8.0;
        Double breakDuration = 1.0;

        // When
        WorkShift workShift = WorkShift.builder()
                .name("Time Shift")
                .code("TIME")
                .startTime(startTime)
                .endTime(endTime)
                .totalHours(totalHours)
                .breakDuration(breakDuration)
                .build();

        // Then
        assertAll(
                () -> assertThat(workShift.getStartTime()).isEqualTo(startTime),
                () -> assertThat(workShift.getEndTime()).isEqualTo(endTime),
                () -> assertThat(workShift.getTotalHours()).isEqualTo(totalHours),
                () -> assertThat(workShift.getBreakDuration()).isEqualTo(breakDuration)
        );
    }

    @Test
    @DisplayName("Should handle night shift")
    void shouldHandleNightShift() {
        // When
        WorkShift nightShift = WorkShift.builder()
                .name("Night Shift")
                .code("NIGHT")
                .startTime(LocalTime.of(22, 0))
                .endTime(LocalTime.of(6, 0))
                .totalHours(7.0)
                .isActive(true)
                .isNightShift(true)
                .build();

        // Then
        assertAll(
                () -> assertThat(nightShift.getIsNightShift()).isTrue(),
                () -> assertThat(nightShift.getStartTime()).isEqualTo(LocalTime.of(22, 0)),
                () -> assertThat(nightShift.getEndTime()).isEqualTo(LocalTime.of(6, 0))
        );
    }

    @Test
    @DisplayName("Should handle deactivation")
    void shouldHandleDeactivation() {
        // Given
        WorkShift workShift = WorkShift.builder()
                .name("Active Shift")
                .code("ACT")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();

        // When
        workShift.setIsActive(false);

        // Then
        assertThat(workShift.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        WorkShift workShift = WorkShift.builder()
                .name("Deleted Shift")
                .code("DEL")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(workShift.getIsDeleted()).isTrue(),
                () -> assertThat(workShift.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(workShift.getDeletedBy()).isEqualTo(deletedBy)
        );
    }
}
