package com.hainam.worksphere.shared.audit.domain;

import com.hainam.worksphere.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("AuditLogDetail Domain Tests")
class AuditLogDetailTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create audit log detail with builder pattern")
    void shouldCreateAuditLogDetailWithBuilderPattern() {
        // Given
        Long id = 1L;
        UUID auditLogId = UUID.randomUUID();
        String fieldName = "email";
        String oldValue = "old@example.com";
        String newValue = "new@example.com";

        // When
        AuditLogDetail auditLogDetail = AuditLogDetail.builder()
                .id(id)
                .auditLogId(auditLogId)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLogDetail.getId()).isEqualTo(id),
                () -> assertThat(auditLogDetail.getAuditLogId()).isEqualTo(auditLogId),
                () -> assertThat(auditLogDetail.getFieldName()).isEqualTo(fieldName),
                () -> assertThat(auditLogDetail.getOldValue()).isEqualTo(oldValue),
                () -> assertThat(auditLogDetail.getNewValue()).isEqualTo(newValue)
        );
    }

    @Test
    @DisplayName("Should create audit log detail for field addition")
    void shouldCreateAuditLogDetailForFieldAddition() {
        // Given
        UUID auditLogId = UUID.randomUUID();
        String fieldName = "phoneNumber";
        String newValue = "+1234567890";

        // When
        AuditLogDetail auditLogDetail = AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .fieldName(fieldName)
                .oldValue(null) // Field was added
                .newValue(newValue)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLogDetail.getAuditLogId()).isEqualTo(auditLogId),
                () -> assertThat(auditLogDetail.getFieldName()).isEqualTo(fieldName),
                () -> assertThat(auditLogDetail.getOldValue()).isNull(),
                () -> assertThat(auditLogDetail.getNewValue()).isEqualTo(newValue)
        );
    }

    @Test
    @DisplayName("Should create audit log detail for field removal")
    void shouldCreateAuditLogDetailForFieldRemoval() {
        // Given
        UUID auditLogId = UUID.randomUUID();
        String fieldName = "phoneNumber";
        String oldValue = "+1234567890";

        // When
        AuditLogDetail auditLogDetail = AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(null) // Field was removed
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLogDetail.getAuditLogId()).isEqualTo(auditLogId),
                () -> assertThat(auditLogDetail.getFieldName()).isEqualTo(fieldName),
                () -> assertThat(auditLogDetail.getOldValue()).isEqualTo(oldValue),
                () -> assertThat(auditLogDetail.getNewValue()).isNull()
        );
    }

    @Test
    @DisplayName("Should handle long text values")
    void shouldHandleLongTextValues() {
        // Given
        UUID auditLogId = UUID.randomUUID();
        String fieldName = "description";
        String longOldValue = "A".repeat(1000); // Long text
        String longNewValue = "B".repeat(1000); // Long text

        // When
        AuditLogDetail auditLogDetail = AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .fieldName(fieldName)
                .oldValue(longOldValue)
                .newValue(longNewValue)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLogDetail.getFieldName()).isEqualTo(fieldName),
                () -> assertThat(auditLogDetail.getOldValue()).isEqualTo(longOldValue),
                () -> assertThat(auditLogDetail.getNewValue()).isEqualTo(longNewValue)
        );
    }

    @Test
    @DisplayName("Should create audit log detail with all constructor")
    void shouldCreateAuditLogDetailWithAllConstructor() {
        // Given
        Long id = 1L;
        UUID auditLogId = UUID.randomUUID();
        String fieldName = "status";
        String oldValue = "INACTIVE";
        String newValue = "ACTIVE";
        AuditLog auditLog = AuditLog.builder().id(auditLogId).build();

        // When
        AuditLogDetail auditLogDetail = new AuditLogDetail(id, auditLogId, fieldName,
                oldValue, newValue, auditLog);

        // Then
        assertAll(
                () -> assertThat(auditLogDetail.getId()).isEqualTo(id),
                () -> assertThat(auditLogDetail.getAuditLogId()).isEqualTo(auditLogId),
                () -> assertThat(auditLogDetail.getFieldName()).isEqualTo(fieldName),
                () -> assertThat(auditLogDetail.getOldValue()).isEqualTo(oldValue),
                () -> assertThat(auditLogDetail.getNewValue()).isEqualTo(newValue),
                () -> assertThat(auditLogDetail.getAuditLog()).isEqualTo(auditLog)
        );
    }

    @Test
    @DisplayName("Should create audit log detail with no args constructor")
    void shouldCreateAuditLogDetailWithNoArgsConstructor() {
        // When
        AuditLogDetail auditLogDetail = new AuditLogDetail();

        // Then
        assertThat(auditLogDetail).isNotNull();
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        // Given
        UUID auditLogId = UUID.randomUUID();
        String fieldName = "notes";
        String oldValue = "";
        String newValue = "Some notes";

        // When
        AuditLogDetail auditLogDetail = AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLogDetail.getFieldName()).isEqualTo(fieldName),
                () -> assertThat(auditLogDetail.getOldValue()).isEmpty(),
                () -> assertThat(auditLogDetail.getNewValue()).isEqualTo(newValue)
        );
    }

    @Test
    @DisplayName("Should maintain association with audit log")
    void shouldMaintainAssociationWithAuditLog() {
        // Given
        UUID auditLogId = UUID.randomUUID();
        AuditLog auditLog = AuditLog.builder()
                .id(auditLogId)
                .build();

        // When
        AuditLogDetail auditLogDetail = AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .fieldName("testField")
                .auditLog(auditLog)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLogDetail.getAuditLogId()).isEqualTo(auditLogId),
                () -> assertThat(auditLogDetail.getAuditLog()).isEqualTo(auditLog),
                () -> assertThat(auditLogDetail.getAuditLog().getId()).isEqualTo(auditLogId)
        );
    }
}
