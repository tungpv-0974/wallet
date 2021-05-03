package com.tungpv.wallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDto implements Serializable {
//    @NotBlank
//    @Size(min = 3, max = 50)
//    private String name;
//
//    @NotBlank
//    @Size(min = 3, max = 50)
//    private String username;

    @NotBlank
    @Size(max = 60)
    @Email
    private String email;

//    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}
