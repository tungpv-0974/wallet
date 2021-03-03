package com.tungpv.wallet.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {
  public static <T> T parseStringToObject(String json, Class<T> classObject)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json, classObject);
  }

  public static String parseObjectToString(Object object) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(object);
  }

  private JsonUtils() {
    throw new IllegalStateException("Utility class");
  }
}
