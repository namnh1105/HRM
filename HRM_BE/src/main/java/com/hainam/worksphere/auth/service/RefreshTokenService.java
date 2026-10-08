package com.hainam.worksphere.auth.service;

import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.auth.repository.RefreshTokenRepository;
import com.hainam.worksphere.auth.util.JwtUtil;
import com.hainam.worksphere.shared.exception.RefreshTokenException;
import com.hainam.worksphere.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.revokeAllTokensByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByTokenAndIsRevokedFalse(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw RefreshTokenException.revoked();
        }

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw RefreshTokenException.expired();
        }

        return token;
    }

    @Transactional
    public void revokeByUser(User user) {
        refreshTokenRepository.revokeAllTokensByUser(user);
    }

    @Transactional
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void removeExpiredTokens() {
        log.info("Removing expired refresh tokens");
        refreshTokenRepository.deleteExpiredAndRevokedTokens(Instant.now());
    }
}

