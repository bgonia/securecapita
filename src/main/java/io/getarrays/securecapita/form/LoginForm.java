package io.getarrays.securecapita.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginForm {

    @NotEmpty(message = "Email can't be empty")
    @NotNull(message = "Email can't be null")
    private String email;
    @NotEmpty
    @NotNull(message = "Password cannot be null")
    private String password;

}
