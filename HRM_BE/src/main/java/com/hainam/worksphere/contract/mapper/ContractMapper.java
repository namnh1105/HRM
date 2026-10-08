package com.hainam.worksphere.contract.mapper;

import com.hainam.worksphere.contract.domain.Contract;
import com.hainam.worksphere.contract.dto.response.ContractResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "contractType", expression = "java(contract.getContractType() != null ? contract.getContractType().name() : null)")
    @Mapping(target = "status", expression = "java(contract.getStatus() != null ? contract.getStatus().name() : null)")
    ContractResponse toContractResponse(Contract contract);
}
