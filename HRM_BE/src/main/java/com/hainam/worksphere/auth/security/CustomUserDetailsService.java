package com.hainam.worksphere.auth.security;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.service.AuthorizationService;
import com.hainam.worksphere.shared.exception.UserNotFoundException;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findActiveByEmailAndEnabled(email)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));

        List<Role> userRoles = authorizationService.getUserRoles(user.getId());
        List<Permission> userPermissions = authorizationService.getUserPermissions(user.getId());

        return UserPrincipal.create(user, userRoles, userPermissions);
    }
}
