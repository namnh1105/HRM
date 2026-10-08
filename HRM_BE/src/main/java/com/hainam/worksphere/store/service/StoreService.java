package com.hainam.worksphere.store.service;

import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.StoreNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.store.domain.Store;
import com.hainam.worksphere.store.dto.request.CreateStoreRequest;
import com.hainam.worksphere.store.dto.request.UpdateStoreRequest;
import com.hainam.worksphere.store.dto.response.StoreResponse;
import com.hainam.worksphere.store.mapper.StoreMapper;
import com.hainam.worksphere.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    @Cacheable(value = CacheConfig.STORE_CACHE, key = "#storeId.toString()")
    public StoreResponse getStoreById(UUID storeId) {
        Store store = storeRepository.findActiveById(storeId)
                .orElseThrow(() -> StoreNotFoundException.byId(storeId.toString()));
        return storeMapper.toStoreResponse(store);
    }

    public List<StoreResponse> getAllActiveStores() {
        return storeRepository.findAllActive()
                .stream()
                .map(storeMapper::toStoreResponse)
                .collect(Collectors.toList());
    }

    public List<StoreResponse> searchStores(String keyword) {
        return storeRepository.searchActive(keyword)
                .stream()
                .map(storeMapper::toStoreResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = CacheConfig.STORE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "STORE")
    public StoreResponse createStore(CreateStoreRequest request, UUID createdBy) {
        if (storeRepository.existsActiveByCode(request.getCode())) {
            throw ValidationException.duplicateField("code", request.getCode());
        }

        Store store = storeMapper.toEntity(request);
        store.setCreatedBy(createdBy);

        Store saved = storeRepository.save(store);
        AuditContext.registerCreated(saved);

        return storeMapper.toStoreResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.STORE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "STORE")
    public StoreResponse updateStore(UUID storeId, UpdateStoreRequest request, UUID updatedBy) {
        Store store = storeRepository.findActiveById(storeId)
                .orElseThrow(() -> StoreNotFoundException.byId(storeId.toString()));

        AuditContext.snapshot(store);

        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getAddress() != null) {
            store.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            store.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            store.setEmail(request.getEmail());
        }
        if (request.getIsActive() != null) {
            store.setIsActive(request.getIsActive());
        }

        store.setUpdatedBy(updatedBy);
        Store saved = storeRepository.save(store);
        AuditContext.registerUpdated(saved);

        return storeMapper.toStoreResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.STORE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "STORE")
    public void softDeleteStore(UUID storeId, UUID deletedBy) {
        Store store = storeRepository.findActiveById(storeId)
                .orElseThrow(() -> StoreNotFoundException.byId(storeId.toString()));

        AuditContext.registerDeleted(store);

        store.setIsDeleted(true);
        store.setDeletedAt(Instant.now());
        store.setDeletedBy(deletedBy);
        storeRepository.save(store);
    }
}
