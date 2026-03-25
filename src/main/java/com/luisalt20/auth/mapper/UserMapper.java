package com.luisalt20.auth.mapper;

import com.luisalt20.auth.dto.request.RegisterRequest;
import com.luisalt20.auth.dto.response.UserResponse;
import com.luisalt20.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target="passwordHash", ignore=true)
    @Mapping(target="status", ignore=true)
    @Mapping(target="createdAt", ignore=true)
    @Mapping(target="updatedAt", ignore=true)
    @Mapping(target="secretBase32", ignore=true)
    @Mapping(target="qrCodeBase64", ignore=true)
    User toEntity(RegisterRequest req);

    @Mapping(target="roles", source="roles")
    UserResponse toDto(User user, List<String> roles);
}
