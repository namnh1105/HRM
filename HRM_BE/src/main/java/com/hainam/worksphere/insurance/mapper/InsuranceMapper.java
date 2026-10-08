package com.hainam.worksphere.insurance.mapper;

import com.hainam.worksphere.insurance.domain.Insurance;
import com.hainam.worksphere.insurance.domain.InsuranceRegistration;
import com.hainam.worksphere.insurance.dto.response.InsuranceRegistrationResponse;
import com.hainam.worksphere.insurance.dto.response.InsuranceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InsuranceMapper {

    @Mapping(target = "insuranceType", expression = "java(insurance.getInsuranceType() != null ? insurance.getInsuranceType().name() : null)")
    InsuranceResponse toInsuranceResponse(Insurance insurance);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "insuranceId", source = "insurance.id")
    @Mapping(target = "insuranceName", source = "insurance.name")
    @Mapping(target = "status", expression = "java(registration.getStatus() != null ? registration.getStatus().name() : null)")
    InsuranceRegistrationResponse toInsuranceRegistrationResponse(InsuranceRegistration registration);
}
