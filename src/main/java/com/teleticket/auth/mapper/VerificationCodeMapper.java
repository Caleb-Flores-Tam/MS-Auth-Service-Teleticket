package com.teleticket.auth.mapper;

import com.teleticket.auth.dto.request.VerificationCodeEmailRequest;
import com.teleticket.auth.dto.response.VerificationCodeEmailResponse;
import com.teleticket.auth.entity.VerificationCodeEmail;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VerificationCodeMapper {

    @Mapping(target = "createdAt", ignore = true)
    VerificationCodeEmail toEntity(VerificationCodeEmailRequest request);

    VerificationCodeEmailResponse toResponse(VerificationCodeEmail entity);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(VerificationCodeEmailRequest request, @MappingTarget VerificationCodeEmail entity);
}
