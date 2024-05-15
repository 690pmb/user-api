package pmb.user.dto;

import javax.validation.constraints.NotBlank;

/** User's Application access */
public record AppDto(@NotBlank String name) {}
