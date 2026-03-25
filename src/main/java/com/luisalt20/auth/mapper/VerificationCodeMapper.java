package com.luisalt20.auth.mapper;

import com.luisalt20.auth.dto.request.VerificationCodeEmailRequest;
import com.luisalt20.auth.dto.response.VerificationCodeEmailResponse;
import com.luisalt20.auth.entity.VerificationCodeEmail;
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
