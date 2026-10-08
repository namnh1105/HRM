package com.hainam.worksphere.degree.mapper;

import com.hainam.worksphere.degree.domain.Degree;
import com.hainam.worksphere.degree.dto.response.DegreeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DegreeMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "degreeLevel", expression = "java(degree.getDegreeLevel() != null ? degree.getDegreeLevel().name() : null)")
    DegreeResponse toDegreeResponse(Degree degree);
}
