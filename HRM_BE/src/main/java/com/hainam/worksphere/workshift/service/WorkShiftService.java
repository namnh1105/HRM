package com.hainam.worksphere.workshift.service;

import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.StoreNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.exception.WorkShiftNotFoundException;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.store.domain.Store;
import com.hainam.worksphere.store.repository.StoreRepository;
import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.dto.request.CreateWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.request.UpdateWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.response.WorkShiftResponse;
import com.hainam.worksphere.workshift.mapper.WorkShiftMapper;
import com.hainam.worksphere.workshift.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;

@Service
@RequiredArgsConstructor
public class WorkShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final StoreRepository storeRepository;
    private final WorkShiftMapper workShiftMapper;

    @Cacheable(value = CacheConfig.WORK_SHIFT_CACHE, key = "#workShiftId")
    public WorkShiftResponse getWorkShiftById(UUID workShiftId) {
        WorkShift workShift = workShiftRepository.findActiveById(workShiftId)
                .orElseThrow(() -> WorkShiftNotFoundException.byId(workShiftId.toString()));
        return workShiftMapper.toWorkShiftResponse(workShift);
    }

    public Page<WorkShiftResponse> getAllActiveWorkShifts(Pageable pageable) {
        return workShiftRepository.findAllActive(pageable)
                .map(workShiftMapper::toWorkShiftResponse);
    }

    public Page<WorkShiftResponse> getAllActiveAndEnabledWorkShifts(Pageable pageable) {
        return workShiftRepository.findAllActiveAndEnabled(pageable)
                .map(workShiftMapper::toWorkShiftResponse);
    }

    public Page<WorkShiftResponse> getWorkShiftsByStore(UUID storeId, Pageable pageable) {
        return workShiftRepository.findActiveByStoreId(storeId, pageable)
                .map(workShiftMapper::toWorkShiftResponse);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.WORK_SHIFT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "WORK_SHIFT")
    public WorkShiftResponse createWorkShift(CreateWorkShiftRequest request, UUID createdBy) {
        if (workShiftRepository.existsActiveByCode(request.getCode())) {
            throw ValidationException.duplicateField("code", request.getCode());
        }

        WorkShift workShift = workShiftMapper.toEntity(request);
        workShift.setCreatedBy(createdBy);
        workShift.setTotalHours(calculateTotalHours(workShift));

        if (request.getStoreId() != null) {
            Store store = storeRepository.findActiveById(request.getStoreId())
                    .orElseThrow(() -> StoreNotFoundException.byId(request.getStoreId().toString()));
            workShift.setStore(store);
        }

        WorkShift saved = workShiftRepository.save(workShift);
        AuditContext.registerCreated(saved);
        return workShiftMapper.toWorkShiftResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.WORK_SHIFT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "WORK_SHIFT")
    public WorkShiftResponse updateWorkShift(UUID workShiftId, UpdateWorkShiftRequest request, UUID updatedBy) {
        WorkShift workShift = workShiftRepository.findActiveById(workShiftId)
                .orElseThrow(() -> WorkShiftNotFoundException.byId(workShiftId.toString()));

        AuditContext.snapshot(workShift);

        if (request.getName() != null) {
            workShift.setName(request.getName());
        }
        if (request.getStartTime() != null) {
            workShift.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            workShift.setEndTime(request.getEndTime());
        }
        if (request.getBreakDuration() != null) {
            workShift.setBreakDuration(request.getBreakDuration());
        }
        if (request.getDescription() != null) {
            workShift.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            workShift.setIsActive(request.getIsActive());
        }
        if (request.getIsNightShift() != null) {
            workShift.setIsNightShift(request.getIsNightShift());
        }

        if (request.getStoreId() != null) {
            Store store = storeRepository.findActiveById(request.getStoreId())
                    .orElseThrow(() -> StoreNotFoundException.byId(request.getStoreId().toString()));
            workShift.setStore(store);
        }

        workShift.setTotalHours(calculateTotalHours(workShift));
        workShift.setUpdatedBy(updatedBy);

        WorkShift saved = workShiftRepository.save(workShift);
        AuditContext.registerUpdated(saved);
        return workShiftMapper.toWorkShiftResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.WORK_SHIFT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "WORK_SHIFT")
    public void softDeleteWorkShift(UUID workShiftId, UUID deletedBy) {
        WorkShift workShift = workShiftRepository.findActiveById(workShiftId)
                .orElseThrow(() -> WorkShiftNotFoundException.byId(workShiftId.toString()));

        AuditContext.registerDeleted(workShift);

        workShift.setIsDeleted(true);
        workShift.setDeletedAt(Instant.now());
        workShift.setDeletedBy(deletedBy);
        workShiftRepository.save(workShift);
    }

    private Double calculateTotalHours(WorkShift workShift) {
        double startMinutes = workShift.getStartTime().getHour() * 60.0 + workShift.getStartTime().getMinute();
        double endMinutes = workShift.getEndTime().getHour() * 60.0 + workShift.getEndTime().getMinute();

        double totalMinutes;
        if (endMinutes > startMinutes) {
            totalMinutes = endMinutes - startMinutes;
        } else {
            // Night shift: crosses midnight
            totalMinutes = (24 * 60) - startMinutes + endMinutes;
        }

        double breakDuration = workShift.getBreakDuration() != null ? workShift.getBreakDuration() : 1.0;
        return Math.round((totalMinutes / 60.0 - breakDuration) * 100.0) / 100.0;
    }

    public ResourceStatsResponse getWorkShiftStats() {
        return ResourceStatsResponse.builder()
                .total(workShiftRepository.countTotal())
                .active(workShiftRepository.countActive())
                .inactive(workShiftRepository.countInactive())
                .deleted(workShiftRepository.countDeleted())
                .build();
    }
}
