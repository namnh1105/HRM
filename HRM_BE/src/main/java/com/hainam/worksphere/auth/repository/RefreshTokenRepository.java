package com.hainam.worksphere.auth.repository;

import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndIsRevokedFalse(String token);

    List<RefreshToken> findByUserAndIsRevokedFalse(User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user")
    void revokeAllTokensByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.expiresAt < :now")
    void revokeExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true OR rt.expiresAt < :now")
    void deleteExpiredAndRevokedTokens(@Param("now") Instant now);
}
