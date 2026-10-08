package com.hainam.worksphere.store.repository;

import com.hainam.worksphere.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("SELECT s FROM Store s WHERE s.isDeleted = false")
    List<Store> findAllActive();

    @Query("SELECT s FROM Store s WHERE s.id = :id AND s.isDeleted = false")
    Optional<Store> findActiveById(@Param("id") UUID id);

    @Query("SELECT s FROM Store s WHERE s.code = :code AND s.isDeleted = false")
    Optional<Store> findActiveByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Store s WHERE s.code = :code AND s.isDeleted = false")
    boolean existsActiveByCode(@Param("code") String code);

    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isDeleted = false")
    List<Store> findAllActiveAndEnabled();

    @Query("SELECT s FROM Store s WHERE s.isDeleted = false AND (" +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Store> searchActive(@Param("keyword") String keyword);
}
