package com.tungpv.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateWalletResponseDto {
    private String address;
    private Long balance;
    private String mnemonicCode;
}
