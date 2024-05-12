package pmb.user.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import pmb.user.dto.AppDto;
import pmb.user.model.App;

/** Maps {@link AppDto} with {@link App}. */
@Mapper(componentModel = "spring")
public interface AppMapper {

    AppDto toDto(App app);

    @InheritInverseConfiguration(name = "toDto")
    App toEntity(AppDto dto);
}
