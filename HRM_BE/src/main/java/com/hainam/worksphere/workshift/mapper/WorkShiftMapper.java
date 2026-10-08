package com.hainam.worksphere.workshift.mapper;

import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.dto.request.CreateWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.response.WorkShiftResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkShiftMapper {

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeName", source = "store.name")
    WorkShiftResponse toWorkShiftResponse(WorkShift workShift);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalHours", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    WorkShift toEntity(CreateWorkShiftRequest request);
}
