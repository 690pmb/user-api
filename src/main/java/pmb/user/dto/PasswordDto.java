package pmb.user.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** Used when updating password, holding new & old passwords. */
public record PasswordDto(@NotNull @Size(min = 6, max = 30) String oldPassword,
    @NotNull @Size(min = 6, max = 30) String newPassword) {
}
