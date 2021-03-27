package com.tungpv.wallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class LoginDto {
    @NotBlank
    @Size(min = 3, max = 60)
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}
