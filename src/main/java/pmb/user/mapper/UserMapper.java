package pmb.user.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import pmb.user.dto.UserDto;
import pmb.user.model.App;
import pmb.user.model.User;

/** Maps {@link UserDto} with {@link User}. */
@Mapper
public interface UserMapper {

  @Mapping(target = "username", source = "login")
  @Mapping(target = "authorities", ignore = true)
  @Mapping(target = "role", ignore = true)
  UserDto toDto(User user);

  @InheritConfiguration(name = "toDto")
  @Mapping(target = "password", ignore = true)
  UserDto toDtoWithoutPassword(User user);

  @Mapping(target = "role", qualifiedByName = "authoritiesToRole", source = "authorities")
  @InheritInverseConfiguration(name = "toDto")
  User toEntity(UserDto dto);

  default App appFromString(String value) {
    return new App(value);
  }

  default String appToString(App value) {
    return value.getName();
  }

  @Named("authoritiesToRole")
  default String authoritiesToRole(List<SimpleGrantedAuthority> authorities) {
    return authorities.stream().findFirst().map(SimpleGrantedAuthority::getAuthority).orElse("");
  }

  @AfterMapping
  default void roleToAuthorities(User user, @MappingTarget UserDto dto) {
    Optional.ofNullable(user.getRole())
        .filter(StringUtils::isNotBlank)
        .ifPresent(dto::setRole);
  }
}
