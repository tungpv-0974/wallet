package com.tungpv.wallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendBitcoinDto {
    @NotNull
    private String address;

    @NotNull
    private String amount;
}
