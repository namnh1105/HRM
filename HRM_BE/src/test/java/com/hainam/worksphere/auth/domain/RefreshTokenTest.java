package com.hainam.worksphere.auth.domain;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("RefreshToken Domain Tests")
class RefreshTokenTest extends BaseUnitTest {

    private UUID refreshTokenId;
    private User testUser;
    private Instant expiresAt;
    private Instant createdAt;

    @BeforeEach
    void setUp() {
        refreshTokenId = UUID.randomUUID();
        testUser = TestFixtures.createTestUser();
        expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        createdAt = Instant.now();
    }

    @Test
    @DisplayName("Should create refresh token with builder pattern")
    void shouldCreateRefreshTokenWithBuilderPattern() {
        // Given
        String tokenValue = "test-refresh-token-123";

        // When
        RefreshToken refreshToken = RefreshToken.builder()
                .id(refreshTokenId)
                .token(tokenValue)
                .user(testUser)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .createdAt(createdAt)
                .build();

        // Then
        assertAll(
                () -> assertThat(refreshToken.getId()).isEqualTo(refreshTokenId),
                () -> assertThat(refreshToken.getToken()).isEqualTo(tokenValue),
                () -> assertThat(refreshToken.getUser()).isEqualTo(testUser),
                () -> assertThat(refreshToken.getExpiresAt()).isEqualTo(expiresAt),
                () -> assertThat(refreshToken.getIsRevoked()).isFalse(),
                () -> assertThat(refreshToken.getCreatedAt()).isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("Should create refresh token with no args constructor")
    void shouldCreateRefreshTokenWithNoArgsConstructor() {
        // When
        RefreshToken refreshToken = new RefreshToken();

        // Then
        assertThat(refreshToken).isNotNull();
    }

    @Test
    @DisplayName("Should handle token expiration")
    void shouldHandleTokenExpiration() {
        // Given
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);

        // When
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiresAt(pastDate)
                .isRevoked(false)
                .build();

        // Then
        assertAll(
                () -> assertThat(expiredToken.getExpiresAt()).isEqualTo(pastDate),
                () -> assertThat(expiredToken.getExpiresAt()).isBefore(Instant.now()),
                () -> assertThat(expiredToken.getIsRevoked()).isFalse()
        );
    }

    @Test
    @DisplayName("Should handle token revocation")
    void shouldHandleTokenRevocation() {
        // Given
        RefreshToken refreshToken = RefreshToken.builder()
                .token("revokable-token")
                .user(testUser)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();

        // When
        refreshToken.setIsRevoked(true);

        // Then
        assertThat(refreshToken.getIsRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should handle user relationship")
    void shouldHandleUserRelationship() {
        // Given
        User anotherUser = TestFixtures.createTestUser("another@example.com");

        // When
        RefreshToken refreshToken = RefreshToken.builder()
                .token("user-token")
                .user(anotherUser)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();

        // Then
        assertAll(
                () -> assertThat(refreshToken.getUser()).isEqualTo(anotherUser),
                () -> assertThat(refreshToken.getUser().getEmail()).isEqualTo("another@example.com")
        );
    }

    @Test
    @DisplayName("Should handle different token values")
    void shouldHandleDifferentTokenValues() {
        // When
        RefreshToken shortToken = RefreshToken.builder()
                .token("short")
                .user(testUser)
                .build();

        RefreshToken longToken = RefreshToken.builder()
                .token("very-long-token-with-many-characters-1234567890")
                .user(testUser)
                .build();

        RefreshToken uuidToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .build();

        // Then
        assertAll(
                () -> assertThat(shortToken.getToken()).isEqualTo("short"),
                () -> assertThat(longToken.getToken()).isEqualTo("very-long-token-with-many-characters-1234567890"),
                () -> assertThat(uuidToken.getToken()).matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        );
    }

    @Test
    @DisplayName("Should handle future expiration dates")
    void shouldHandleFutureExpirationDates() {
        // Given
        Instant futureDate = Instant.now().plus(30, ChronoUnit.DAYS);

        // When
        RefreshToken longLivedToken = RefreshToken.builder()
                .token("long-lived-token")
                .user(testUser)
                .expiresAt(futureDate)
                .isRevoked(false)
                .build();

        // Then
        assertAll(
                () -> assertThat(longLivedToken.getExpiresAt()).isEqualTo(futureDate),
                () -> assertThat(longLivedToken.getExpiresAt()).isAfter(Instant.now())
        );
    }

    @Test
    @DisplayName("Should handle token lifecycle states")
    void shouldHandleTokenLifecycleStates() {
        // Given
        RefreshToken activeToken = RefreshToken.builder()
                .token("active-token")
                .user(testUser)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .isRevoked(false)
                .build();

        RefreshToken revokedToken = RefreshToken.builder()
                .token("revoked-token")
                .user(testUser)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .isRevoked(true)
                .build();

        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .isRevoked(false)
                .build();

        // Then
        assertAll(
                () -> assertThat(activeToken.getIsRevoked()).isFalse(),
                () -> assertThat(activeToken.getExpiresAt()).isAfter(Instant.now()),
                () -> assertThat(revokedToken.getIsRevoked()).isTrue(),
                () -> assertThat(expiredToken.getExpiresAt()).isBefore(Instant.now())
        );
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void shouldHandleNullUserGracefully() {
        // When
        RefreshToken tokenWithoutUser = RefreshToken.builder()
                .token("orphan-token")
                .user(null)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();

        // Then
        assertThat(tokenWithoutUser.getUser()).isNull();
    }

    @Test
    @DisplayName("Should handle token creation timestamp")
    void shouldHandleTokenCreationTimestamp() {
        // Given
        Instant specificTime = Instant.parse("2024-01-15T10:30:45Z");

        // When
        RefreshToken timestampedToken = RefreshToken.builder()
                .token("timestamped-token")
                .user(testUser)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .createdAt(specificTime)
                .build();

        // Then
        assertThat(timestampedToken.getCreatedAt()).isEqualTo(specificTime);
    }

    @Test
    @DisplayName("Should handle token updates")
    void shouldHandleTokenUpdates() {
        // Given
        RefreshToken refreshToken = RefreshToken.builder()
                .token("updatable-token")
                .user(testUser)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();

        // When
        String newToken = "new-token-value";
        Instant newExpiresAt = Instant.now().plus(14, ChronoUnit.DAYS);

        refreshToken.setToken(newToken);
        refreshToken.setExpiresAt(newExpiresAt);

        // Then
        assertAll(
                () -> assertThat(refreshToken.getToken()).isEqualTo(newToken),
                () -> assertThat(refreshToken.getExpiresAt()).isEqualTo(newExpiresAt)
        );
    }

    @Test
    @DisplayName("Should handle token with different users")
    void shouldHandleTokenWithDifferentUsers() {
        // Given
        User user1 = TestFixtures.createTestUser("user1@example.com");
        User user2 = TestFixtures.createTestUser("user2@example.com");

        // When
        RefreshToken token1 = RefreshToken.builder()
                .token("token-for-user1")
                .user(user1)
                .expiresAt(expiresAt)
                .build();

        RefreshToken token2 = RefreshToken.builder()
                .token("token-for-user2")
                .user(user2)
                .expiresAt(expiresAt)
                .build();

        // Then
        assertAll(
                () -> assertThat(token1.getUser().getEmail()).isEqualTo("user1@example.com"),
                () -> assertThat(token2.getUser().getEmail()).isEqualTo("user2@example.com"),
                () -> assertThat(token1.getUser()).isNotEqualTo(token2.getUser())
        );
    }
}
