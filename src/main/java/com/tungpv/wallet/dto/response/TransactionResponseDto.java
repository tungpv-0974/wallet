package com.tungpv.wallet.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionResponseDto implements Serializable {
    private String hash;
    private String value;
    private String fee;
    private Integer confirmations;
    private String updateTime;
    private String type;
}
