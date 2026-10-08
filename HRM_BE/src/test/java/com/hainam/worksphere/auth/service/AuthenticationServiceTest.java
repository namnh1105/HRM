package com.hainam.worksphere.auth.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.auth.dto.request.LoginRequest;
import com.hainam.worksphere.auth.dto.request.RegisterRequest;
import com.hainam.worksphere.auth.dto.response.AuthenticationResponse;
import com.hainam.worksphere.auth.mapper.AuthMapper;
import com.hainam.worksphere.auth.mapper.AuthResponseMapper;
import com.hainam.worksphere.auth.mapper.UserAuthorizationMapper;
import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.auth.util.JwtUtil;
import com.hainam.worksphere.authorization.service.AuthorizationService;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.exception.EmailAlreadyExistsException;
import com.hainam.worksphere.shared.exception.InvalidCredentialsException;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.response.UserWithAuthorizationResponse;
import com.hainam.worksphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private AuthResponseMapper authResponseMapper;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private UserAuthorizationMapper userAuthorizationMapper;

        @Mock
        private EmployeeRepository employeeRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshToken testRefreshToken;
    private AuthenticationResponse authResponse;
    private UserPrincipal testUserPrincipal;
        private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.createTestUser();
        testRefreshToken = TestFixtures.createTestRefreshToken();
        testUserPrincipal = UserPrincipal.create(testUser);
        testEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .firstName("John")
                .lastName("Doe")
                .fullName("Doe John")
                .email(testUser.getEmail())
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setGivenName("John");
        registerRequest.setFamilyName("Doe");

        loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);


        authResponse = AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .user(mock(UserWithAuthorizationResponse.class))
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsActiveByEmail(TEST_EMAIL)).thenReturn(false);
        when(authMapper.toUser(registerRequest)).thenReturn(testUser);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(employeeRepository.findActiveByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(authorizationService.getUserRoles(testUser.getId())).thenReturn(Collections.emptyList());
        when(authorizationService.getUserPermissions(testUser.getId())).thenReturn(Collections.emptyList());
        when(jwtUtil.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L);
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn(testRefreshToken);
        when(userAuthorizationMapper.toUserWithAuthorizationResponse(any(), any(), any(), any()))
                .thenReturn(mock(UserWithAuthorizationResponse.class));
        when(authResponseMapper.toAuthenticationResponse(anyString(), anyString(), any(Long.class), any()))
                .thenReturn(authResponse);

        // When
        AuthenticationResponse result = authenticationService.register(registerRequest);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(userRepository).existsActiveByEmail(TEST_EMAIL),
            () -> verify(authMapper).toUser(registerRequest),
            () -> verify(passwordEncoder).encode(TEST_PASSWORD),
            () -> verify(userRepository, times(2)).save(any(User.class)),
            () -> verify(jwtUtil).generateAccessToken(any(UserPrincipal.class)),
            () -> verify(refreshTokenService).createRefreshToken(testUser)
        );
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email exists")
    void shouldThrowEmailAlreadyExistsExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsActiveByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository).existsActiveByEmail(TEST_EMAIL);
        verifyNoInteractions(authMapper, passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void shouldLoginUserSuccessfully() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(authorizationService.getUserRoles(testUser.getId())).thenReturn(Collections.emptyList());
        when(authorizationService.getUserPermissions(testUser.getId())).thenReturn(Collections.emptyList());
        when(jwtUtil.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L);
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn(testRefreshToken);
        when(employeeRepository.findActiveByUserId(testUser.getId())).thenReturn(Optional.of(testEmployee));
        when(userAuthorizationMapper.toUserWithAuthorizationResponse(any(), any(), any(), any()))
                .thenReturn(mock(UserWithAuthorizationResponse.class));
        when(authResponseMapper.toAuthenticationResponse(anyString(), anyString(), any(Long.class), any()))
                .thenReturn(authResponse);

        // When
        AuthenticationResponse result = authenticationService.login(loginRequest);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class)),
            () -> verify(userRepository).findActiveById(testUser.getId()),
            () -> verify(jwtUtil).generateAccessToken(any(UserPrincipal.class)),
            () -> verify(refreshTokenService).createRefreshToken(testUser)
        );
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for bad credentials")
    void shouldThrowInvalidCredentialsExceptionForBadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtil, refreshTokenService);
    }
}
