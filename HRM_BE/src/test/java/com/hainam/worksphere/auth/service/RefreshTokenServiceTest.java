package com.hainam.worksphere.auth.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.auth.repository.RefreshTokenRepository;
import com.hainam.worksphere.auth.util.JwtUtil;
import com.hainam.worksphere.shared.exception.RefreshTokenException;
import com.hainam.worksphere.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RefreshTokenService Tests")
class RefreshTokenServiceTest extends BaseUnitTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.createTestUser();
        testRefreshToken = TestFixtures.createTestRefreshToken();
        testRefreshToken.setUser(testUser);
    }

    @Test
    @DisplayName("Should create refresh token successfully")
    void shouldCreateRefreshTokenSuccessfully() {
        // Given
        long refreshTokenExpiration = 604800000L; // 7 days in milliseconds
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(refreshTokenExpiration);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // When
        RefreshToken result = refreshTokenService.createRefreshToken(testUser);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getUser()).isEqualTo(testUser),
            () -> assertThat(result.getToken()).isNotNull(),
            () -> verify(refreshTokenRepository).revokeAllTokensByUser(testUser),
            () -> verify(refreshTokenRepository).save(any(RefreshToken.class))
        );
    }

    @Test
    @DisplayName("Should find refresh token by token successfully")
    void shouldFindRefreshTokenByTokenSuccessfully() {
        // Given
        String tokenValue = "test-refresh-token";
        when(refreshTokenRepository.findByTokenAndIsRevokedFalse(tokenValue))
                .thenReturn(Optional.of(testRefreshToken));

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenValue);

        // Then
        assertAll(
            () -> assertThat(result).isPresent(),
            () -> assertThat(result.get()).isEqualTo(testRefreshToken),
            () -> verify(refreshTokenRepository).findByTokenAndIsRevokedFalse(tokenValue)
        );
    }

    @Test
    @DisplayName("Should return empty when refresh token not found")
    void shouldReturnEmptyWhenRefreshTokenNotFound() {
        // Given
        String nonExistentToken = "non-existent-token";
        when(refreshTokenRepository.findByTokenAndIsRevokedFalse(nonExistentToken))
                .thenReturn(Optional.empty());

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken(nonExistentToken);

        // Then
        assertAll(
            () -> assertThat(result).isEmpty(),
            () -> verify(refreshTokenRepository).findByTokenAndIsRevokedFalse(nonExistentToken)
        );
    }

    @Test
    @DisplayName("Should verify refresh token successfully for valid token")
    void shouldVerifyRefreshTokenSuccessfullyForValidToken() {
        // Given
        testRefreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        testRefreshToken.setIsRevoked(false);

        // When
        RefreshToken result = refreshTokenService.verifyExpiration(testRefreshToken);

        // Then
        assertThat(result).isEqualTo(testRefreshToken);
    }

    @Test
    @DisplayName("Should throw RefreshTokenException for expired token")
    void shouldThrowRefreshTokenExceptionForExpiredToken() {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("expired-token")
                .user(testUser)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .isRevoked(false)
                .createdAt(Instant.now().minus(8, ChronoUnit.DAYS))
                .build();

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredToken))
                .isInstanceOf(RefreshTokenException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("Should throw RefreshTokenException for revoked token")
    void shouldThrowRefreshTokenExceptionForRevokedToken() {
        // Given
        RefreshToken revokedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("revoked-token")
                .user(testUser)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .isRevoked(true)
                .createdAt(Instant.now())
                .build();

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(revokedToken))
                .isInstanceOf(RefreshTokenException.class)
                .hasMessageContaining("revoked");

        verify(refreshTokenRepository).delete(revokedToken);
    }

    @Test
    @DisplayName("Should revoke all tokens by user successfully")
    void shouldRevokeAllTokensByUserSuccessfully() {
        // When
        refreshTokenService.revokeByUser(testUser);

        // Then
        verify(refreshTokenRepository).revokeAllTokensByUser(testUser);
    }

    @Test
    @DisplayName("Should revoke specific token successfully")
    void shouldRevokeSpecificTokenSuccessfully() {
        // Given
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // When
        refreshTokenService.revokeToken(testRefreshToken);

        // Then
        assertAll(
            () -> assertThat(testRefreshToken.getIsRevoked()).isTrue(),
            () -> verify(refreshTokenRepository).save(testRefreshToken)
        );
    }

    @Test
    @DisplayName("Should remove expired tokens successfully")
    void shouldRemoveExpiredTokensSuccessfully() {
        // When
        refreshTokenService.removeExpiredTokens();

        // Then
        verify(refreshTokenRepository).deleteExpiredAndRevokedTokens(any(Instant.class));
    }

    @Test
    @DisplayName("Should handle token creation when user has existing tokens")
    void shouldHandleTokenCreationWhenUserHasExistingTokens() {
        // Given
        long refreshTokenExpiration = 604800000L;
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(refreshTokenExpiration);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // When
        RefreshToken result = refreshTokenService.createRefreshToken(testUser);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(refreshTokenRepository).revokeAllTokensByUser(testUser), // Should revoke existing tokens first
            () -> verify(refreshTokenRepository).save(any(RefreshToken.class))
        );
    }

    @Test
    @DisplayName("Should set correct expiration time for new tokens")
    void shouldSetCorrectExpirationTimeForNewTokens() {
        // Given
        long refreshTokenExpiration = 604800000L; // 7 days
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(refreshTokenExpiration);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            // Verify expiration is set correctly (approximately)
            Instant expectedExpiration = Instant.now().plusSeconds(refreshTokenExpiration / 1000);
            assertThat(token.getExpiresAt()).isAfterOrEqualTo(expectedExpiration.minusSeconds(1));
            assertThat(token.getExpiresAt()).isBeforeOrEqualTo(expectedExpiration.plusSeconds(1));
            return token;
        });

        // When
        refreshTokenService.createRefreshToken(testUser);

        // Then
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should generate unique token strings")
    void shouldGenerateUniqueTokenStrings() {
        // Given
        long refreshTokenExpiration = 604800000L;
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(refreshTokenExpiration);

        RefreshToken token1 = RefreshToken.builder()
                .token("token-1")
                .user(testUser)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .isRevoked(false)
                .build();

        RefreshToken token2 = RefreshToken.builder()
                .token("token-2")
                .user(testUser)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .isRevoked(false)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(token1)
                .thenReturn(token2);

        // When
        RefreshToken result1 = refreshTokenService.createRefreshToken(testUser);
        RefreshToken result2 = refreshTokenService.createRefreshToken(testUser);

        // Then
        assertThat(result1.getToken()).isNotEqualTo(result2.getToken());
    }
}
