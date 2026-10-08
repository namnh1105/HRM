package com.hainam.worksphere.store.mapper;

import com.hainam.worksphere.store.domain.Store;
import com.hainam.worksphere.store.dto.request.CreateStoreRequest;
import com.hainam.worksphere.store.dto.response.StoreResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StoreMapper {

    StoreResponse toStoreResponse(Store store);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Store toEntity(CreateStoreRequest request);
}
