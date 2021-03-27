package com.tungpv.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;

    private String type = "Bearer";

    public JwtResponse(String accessToken) {
        this.token = accessToken;
    }
}
