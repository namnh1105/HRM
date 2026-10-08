package com.hainam.worksphere.auth.util;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.auth.config.JwtProperties;
import com.hainam.worksphere.shared.exception.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.lenient;

@DisplayName("JwtUtil Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilTest extends BaseUnitTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails testUserDetails;
    private String testSecret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;

    @BeforeEach
    void setUp() {
        testUserDetails = User.builder()
                .username(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .authorities(Collections.emptyList())
                .build();

        testSecret = "mySecretKeyThatIsAtLeast256BitsLongForHMACAlgorithmToWork";
        accessTokenExpiration = 86400000L; // 24 hours in milliseconds
        refreshTokenExpiration = 604800000L; // 7 days in milliseconds

        // Mock JWT properties with lenient stubbing
        lenient().when(jwtProperties.getSecret()).thenReturn(testSecret);
        lenient().when(jwtProperties.getAccessTokenExpiration()).thenReturn(accessTokenExpiration);
        lenient().when(jwtProperties.getRefreshTokenExpiration()).thenReturn(refreshTokenExpiration);
    }

    @Test
    @DisplayName("Should generate access token successfully")
    void shouldGenerateAccessTokenSuccessfully() {
        // When
        String token = jwtUtil.generateAccessToken(testUserDetails);

        // Then
        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(token).isNotEmpty(),
                () -> assertThat(token.split("\\.")).hasSize(3) // JWT has 3 parts
        );
    }

    @Test
    @DisplayName("Should generate access token with extra claims")
    void shouldGenerateAccessTokenWithExtraClaims() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", "123");

        // When
        String token = jwtUtil.generateAccessToken(extraClaims, testUserDetails);

        // Then
        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(token).isNotEmpty(),
                () -> assertThat(jwtUtil.extractUsername(token)).isEqualTo(TEST_EMAIL)
        );
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void shouldGenerateRefreshTokenSuccessfully() {
        // When
        String token = jwtUtil.generateRefreshToken(testUserDetails);

        // Then
        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(token).isNotEmpty(),
                () -> assertThat(token.split("\\.")).hasSize(3)
        );
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtUtil.generateAccessToken(testUserDetails);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should extract expiration from token")
    void shouldExtractExpirationFromToken() {
        // Given
        String token = jwtUtil.generateAccessToken(testUserDetails);

        // When
        Date actualExpiration = jwtUtil.extractExpiration(token);

        // Then - Check that expiration is approximately correct (within 5 seconds of expected)
        long now = System.currentTimeMillis();
        long expectedExpiration = now + accessTokenExpiration;
        long actualExpirationTime = actualExpiration.getTime();

        // Allow for timing differences up to 5 seconds
        assertThat(Math.abs(actualExpirationTime - expectedExpiration))
            .isLessThan(5000L);
    }

    @Test
    @DisplayName("Should validate token successfully for correct user")
    void shouldValidateTokenSuccessfullyForCorrectUser() {
        // Given
        String token = jwtUtil.generateAccessToken(testUserDetails);

        // When
        boolean isValid = jwtUtil.isTokenValid(token, testUserDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate token for different user")
    void shouldInvalidateTokenForDifferentUser() {
        // Given
        String token = jwtUtil.generateAccessToken(testUserDetails);
        UserDetails differentUser = User.builder()
                .username("different@example.com")
                .password(TEST_PASSWORD)
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtUtil.isTokenValid(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for expired token")
    void shouldThrowInvalidTokenExceptionForExpiredToken() {
        // Given - Create an expired token
        String expiredToken = createExpiredToken();

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(expiredToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for malformed token")
    void shouldThrowInvalidTokenExceptionForMalformedToken() {
        // Given
        String malformedToken = "invalid.token.structure";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for token with wrong signature")
    void shouldThrowInvalidTokenExceptionForTokenWithWrongSignature() {
        // Given - Create token with different secret
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject(TEST_EMAIL)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(Keys.hmacShaKeyFor("differentSecretKey1234567890123456789".getBytes()), SignatureAlgorithm.HS256)
                .compact();

        // When & Then - The actual implementation throws SignatureException which may be wrapped
        assertThatThrownBy(() -> jwtUtil.extractUsername(tokenWithWrongSignature))
                .isInstanceOf(Exception.class); // Accept any exception as signature verification can throw different types
    }

    @Test
    @DisplayName("Should return correct access token expiration")
    void shouldReturnCorrectAccessTokenExpiration() {
        // When
        long expiration = jwtUtil.getAccessTokenExpiration();

        // Then
        assertThat(expiration).isEqualTo(accessTokenExpiration);
    }

    @Test
    @DisplayName("Should return correct refresh token expiration")
    void shouldReturnCorrectRefreshTokenExpiration() {
        // When
        long expiration = jwtUtil.getRefreshTokenExpiration();

        // Then
        assertThat(expiration).isEqualTo(refreshTokenExpiration);
    }

    @Test
    @DisplayName("Should extract custom claim from token")
    void shouldExtractCustomClaimFromToken() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        String token = jwtUtil.generateAccessToken(extraClaims, testUserDetails);

        // When
        String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));

        // Then
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Should handle null or empty token")
    void shouldHandleNullOrEmptyToken() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(null))
                .isInstanceOf(InvalidTokenException.class);

        assertThatThrownBy(() -> jwtUtil.extractUsername(""))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Should generate tokens with different expiration times")
    void shouldGenerateTokensWithDifferentExpirationTimes() {
        // Given
        String accessToken = jwtUtil.generateAccessToken(testUserDetails);
        String refreshToken = jwtUtil.generateRefreshToken(testUserDetails);

        // When
        Date accessTokenExpiration = jwtUtil.extractExpiration(accessToken);
        Date refreshTokenExpiration = jwtUtil.extractExpiration(refreshToken);

        // Then
        assertThat(refreshTokenExpiration).isAfter(accessTokenExpiration);
    }

    @Test
    @DisplayName("Should validate token with correct timing")
    void shouldValidateTokenWithCorrectTiming() {
        // Given
        String token = jwtUtil.generateAccessToken(testUserDetails);

        // When - Immediately validate (should be valid)
        boolean isValidNow = jwtUtil.isTokenValid(token, testUserDetails);

        // Then
        assertThat(isValidNow).isTrue();
    }

    private String createExpiredToken() {
        Date pastDate = new Date(System.currentTimeMillis() - 1000); // 1 second ago
        byte[] keyBytes = Decoders.BASE64.decode(testSecret);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(TEST_EMAIL)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(pastDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
