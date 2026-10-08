package com.hainam.worksphere.relative.mapper;

import com.hainam.worksphere.relative.domain.Relative;
import com.hainam.worksphere.relative.dto.response.RelativeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RelativeMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "relationship", expression = "java(relative.getRelationship() != null ? relative.getRelationship().name() : null)")
    RelativeResponse toRelativeResponse(Relative relative);
}
