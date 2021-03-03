package com.tungpv.wallet.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ErrorObject implements Serializable {

  private static final long serialVersionUID = 1L;

  private Integer code;
  private String message;

  public ErrorObject(Integer code, String message) {
    this.code = code;
    this.message = message;
  }
}
