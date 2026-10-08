package com.hainam.worksphere.user.repository;

import com.hainam.worksphere.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndIsEnabledTrue(String email);

    Optional<User> findByGoogleId(String googleId);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    Page<User> findAllActive(Pageable pageable);

    Page<User> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findActiveById(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isEnabled = true AND u.isDeleted = false")
    Optional<User> findActiveByEmailAndEnabled(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.isDeleted = false")
    boolean existsActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = true")
    Optional<User> findDeletedById(@Param("id") UUID id);

    @Query("SELECT COUNT(u) FROM User u")
    long countTotal();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isEnabled = true AND u.isDeleted = false")
    long countActive();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isEnabled = false AND u.isDeleted = false")
    long countInactive();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = true")
    long countDeleted();

    @Query("SELECT u FROM User u LEFT JOIN Employee e ON e.user = u WHERE (u.isDeleted = false OR u.isDeleted = :includeDeleted) AND (" +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<User> searchByKeyword(@Param("keyword") String keyword, @Param("includeDeleted") boolean includeDeleted, Pageable pageable);
}

