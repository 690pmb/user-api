package pmb.user.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pmb.user.dto.UserDto;
import pmb.user.model.User;

/** Maps {@link UserDto} with {@link User}. */
@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "username", source = "login")
  @Mapping(target = "authorities", ignore = true)
  UserDto toDto(User user);

  @InheritConfiguration(name = "toDto")
  @Mapping(target = "password", ignore = true)
  UserDto toDtoWithoutPassword(User user);

  @InheritInverseConfiguration(name = "toDto")
  User toEntity(UserDto dto);
}
