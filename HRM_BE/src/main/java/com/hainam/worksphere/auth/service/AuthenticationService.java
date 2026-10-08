package com.hainam.worksphere.auth.service;

import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.auth.dto.request.*;
import com.hainam.worksphere.auth.dto.response.AuthenticationResponse;
import com.hainam.worksphere.auth.dto.response.TokenResponse;
import com.hainam.worksphere.auth.mapper.AuthMapper;
import com.hainam.worksphere.auth.mapper.AuthResponseMapper;
import com.hainam.worksphere.auth.mapper.UserAuthorizationMapper;
import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.auth.util.JwtUtil;
import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.repository.RoleRepository;
import com.hainam.worksphere.authorization.service.AuthorizationService;
import com.hainam.worksphere.authorization.service.UserRoleService;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.exception.*;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.response.UserWithAuthorizationResponse;
import com.hainam.worksphere.user.mapper.UserMapper;
import com.hainam.worksphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final AuthResponseMapper authResponseMapper;
    private final AuthorizationService authorizationService;
    private final UserAuthorizationMapper userAuthorizationMapper;
    private final UserRoleService userRoleService;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsActiveByEmail(request.getEmail())) {
            throw EmailAlreadyExistsException.withEmail(request.getEmail());
        }

        User user = authMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        savedUser.setCreatedBy(savedUser.getId());
        savedUser = userRepository.save(savedUser);

        Employee employee = createEmployeeForUser(savedUser, request.getGivenName(), request.getFamilyName());

        // Auto-assign USER role to new user
        assignDefaultUserRole(savedUser.getId());

        // Register for audit via AuditContext (picked up by @Auditable on controller or
        // logged inline)
        AuditContext.registerCreated(savedUser);

        List<Role> userRoles = authorizationService.getUserRoles(savedUser.getId());
        List<Permission> userPermissions = authorizationService.getUserPermissions(savedUser.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(savedUser, userRoles, userPermissions);

        String accessToken = jwtUtil.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        UserWithAuthorizationResponse userWithAuth = userAuthorizationMapper.toUserWithAuthorizationResponse(
                savedUser, employee, userRoles, userPermissions);

        return authResponseMapper.toAuthenticationResponse(
                accessToken,
                refreshToken.getToken(),
                jwtUtil.getAccessTokenExpiration() / 1000,
                userWithAuth);
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findActiveById(userPrincipal.getId())
                    .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

            List<Role> userRoles = authorizationService.getUserRoles(user.getId());
            List<Permission> userPermissions = authorizationService.getUserPermissions(user.getId());

            UserPrincipal enhancedUserPrincipal = UserPrincipal.create(user, userRoles, userPermissions);

            String accessToken = jwtUtil.generateAccessToken(enhancedUserPrincipal);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            Employee employee = employeeRepository.findActiveByUserId(user.getId()).orElse(null);
            UserWithAuthorizationResponse userWithAuth = userAuthorizationMapper.toUserWithAuthorizationResponse(
                    user, employee, userRoles, userPermissions);

            return authResponseMapper.toAuthenticationResponse(
                    accessToken,
                    refreshToken.getToken(),
                    jwtUtil.getAccessTokenExpiration() / 1000,
                    userWithAuth);
        } catch (BadCredentialsException e) {
            throw InvalidCredentialsException.create();
        }
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    User activeUser = userRepository.findActiveById(user.getId())
                            .orElseThrow(() -> new RefreshTokenException("User has been deactivated or deleted"));

                    List<Role> userRoles = authorizationService.getUserRoles(activeUser.getId());
                    List<Permission> userPermissions = authorizationService.getUserPermissions(activeUser.getId());

                    UserPrincipal userPrincipal = UserPrincipal.create(activeUser, userRoles, userPermissions);
                    String accessToken = jwtUtil.generateAccessToken(userPrincipal);

                    Employee employee = employeeRepository.findActiveByUserId(activeUser.getId()).orElse(null);
                    UserWithAuthorizationResponse userWithAuth = userAuthorizationMapper.toUserWithAuthorizationResponse(
                            activeUser, employee, userRoles, userPermissions);

                    return authResponseMapper.toTokenResponse(
                            accessToken,
                            jwtUtil.getAccessTokenExpiration() / 1000,
                            userWithAuth);
                })
                .orElseThrow(RefreshTokenException::notFound);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.findByToken(refreshToken)
                .ifPresent(refreshTokenService::revokeToken);
    }

    @Transactional
    public void logoutAll(UserPrincipal userPrincipal) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));
        refreshTokenService.revokeByUser(user);
    }

    @Transactional
    public AuthenticationResponse processGoogleOAuth2Login(String email, String name, String googleId, String givenName,
            String familyName) {
        if (email == null || email.trim().isEmpty()) {
            throw new OAuth2ValidationException("Email is required for Google OAuth2 login");
        }
        if (googleId == null || googleId.trim().isEmpty()) {
            throw new OAuth2ValidationException("Google ID is required for Google OAuth2 login");
        }
        if (givenName == null || givenName.trim().isEmpty()) {
            throw new OAuth2ValidationException("Given name is required for Google OAuth2 login");
        }
        if (familyName == null || familyName.trim().isEmpty()) {
            throw new OAuth2ValidationException("Family name is required for Google OAuth2 login");
        }

        boolean isNewUser = false;
        User user = userRepository.findActiveByEmail(email).orElse(null);

        if (user == null) {
            isNewUser = true;
            User newUser = User.builder()
                    .email(email)
                    .googleId(googleId)
                    .isEnabled(true)
                    .build();
            User savedUser = userRepository.save(newUser);
            savedUser.setCreatedBy(savedUser.getId());
            user = userRepository.save(savedUser);

            createEmployeeForUser(user, givenName, familyName);

            // Auto-assign USER role to new OAuth2 user
            assignDefaultUserRole(user.getId());

            // Register for audit
            AuditContext.registerCreated(user);
        }

        if (user.getGoogleId() == null || !user.getGoogleId().equals(googleId)) {
            user.setGoogleId(googleId);
            user = userRepository.save(user);
        }

        List<Role> userRoles = authorizationService.getUserRoles(user.getId());
        List<Permission> userPermissions = authorizationService.getUserPermissions(user.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(user, userRoles, userPermissions);
        String accessToken = jwtUtil.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Employee employee = employeeRepository.findActiveByUserId(user.getId()).orElse(null);
        UserWithAuthorizationResponse userWithAuth = userAuthorizationMapper.toUserWithAuthorizationResponse(
                user, employee, userRoles, userPermissions);

        return authResponseMapper.toAuthenticationResponse(
                accessToken,
                refreshToken.getToken(),
                jwtUtil.getAccessTokenExpiration() / 1000,
                userWithAuth);
    }

    /**
     * Assign the default USER role to a newly registered user
     */
    private void assignDefaultUserRole(java.util.UUID userId) {
        try {
            roleRepository.findByCode("USER").ifPresent(role -> {
                userRoleService.assignRoleToUser(userId, role.getId());
                log.info("Assigned default USER role to user: {}", userId);
            });
        } catch (Exception e) {
            log.warn("Failed to assign default USER role to user: {}", userId, e);
        }
    }

    private Employee createEmployeeForUser(User user, String givenName, String familyName) {
        return employeeRepository.findActiveByUserId(user.getId())
                .orElseGet(() -> {
                    String fullName = (givenName != null ? givenName : "") + " "
                            + (familyName != null ? familyName : "");
                    Employee employee = Employee.builder()
                            .user(user)
                            .employeeCode(null)
                            .firstName(givenName != null ? givenName : "")
                            .lastName(familyName != null ? familyName : "")
                            .fullName(fullName.trim())
                            .email(user.getEmail())
                            .createdBy(user.getId())
                            .build();
                    return employeeRepository.save(employee);
                });
    }

    public UserPrincipal validateAccessToken(String token) {
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));

        List<Role> userRoles = authorizationService.getUserRoles(user.getId());
        List<Permission> userPermissions = authorizationService.getUserPermissions(user.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(user, userRoles, userPermissions);

        if (!jwtUtil.isTokenValid(token, userPrincipal)) {
            throw new InvalidTokenException("Token is invalid");
        }

        return userPrincipal;
    }
}
