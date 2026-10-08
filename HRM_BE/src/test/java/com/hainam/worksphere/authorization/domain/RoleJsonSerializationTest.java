package com.hainam.worksphere.authorization.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hainam.worksphere.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Role JSON Serialization Tests")
class RoleJsonSerializationTest extends BaseUnitTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should serialize Role with Instant fields to JSON")
    void shouldSerializeRoleWithLocalDateTimeToJson() throws Exception {
        // Given
        Instant createdAt = Instant.parse("2026-02-10T08:00:00.123Z");
        Instant updatedAt = Instant.parse("2026-02-10T09:30:45.567Z");

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .code("TEST_ROLE")
                .displayName("Test Role")
                .description("A test role for JSON serialization")
                .isSystem(false)
                .isActive(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        String json = objectMapper.writeValueAsString(role);

        // Then
        assertAll(
                () -> assertThat(json).contains("\"code\":\"TEST_ROLE\""),
                () -> assertThat(json).contains("\"displayName\":\"Test Role\""),
                () -> assertThat(json).contains("\"createdAt\":\"2026-02-10T08:00:00.123Z\""),
                () -> assertThat(json).contains("\"updatedAt\":\"2026-02-10T09:30:45.567Z\""),
                () -> assertThat(json).doesNotContain("\"createdAt\":[") // Should not be an array (timestamp)
        );

        System.out.println("Serialized JSON: " + json);
    }

    @Test
    @DisplayName("Should deserialize JSON with Instant fields to Role")
    void shouldDeserializeJsonWithLocalDateTimeToRole() throws Exception {
        // Given
        String json = """
                {
                    "id": "550e8400-e29b-41d4-a716-446655440001",
                    "code": "DESERIALIZE_TEST",
                    "displayName": "Deserialize Test Role",
                    "description": "A role for deserialization testing",
                    "isSystem": false,
                    "isActive": true,
                    "createdAt": "2026-02-10T10:15:30.789Z",
                    "updatedAt": "2026-02-10T11:20:35.123Z",
                    "rolePermissions": []
                }
                """;

        // When
        Role role = objectMapper.readValue(json, Role.class);

        // Then
        assertAll(
                () -> assertThat(role.getCode()).isEqualTo("DESERIALIZE_TEST"),
                () -> assertThat(role.getDisplayName()).isEqualTo("Deserialize Test Role"),
                () -> assertThat(role.getCreatedAt()).isEqualTo(Instant.parse("2026-02-10T10:15:30.789Z")),
                () -> assertThat(role.getUpdatedAt()).isEqualTo(Instant.parse("2026-02-10T11:20:35.123Z")),
                () -> assertThat(role.getIsActive()).isTrue(),
                () -> assertThat(role.getIsSystem()).isFalse()
        );
    }
}
