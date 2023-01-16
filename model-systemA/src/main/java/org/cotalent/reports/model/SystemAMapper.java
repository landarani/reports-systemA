package org.cotalent.reports.model;

import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class SystemAMapper {
  private final ObjectMapper objectMapper;

  public SystemAMapper() {

    objectMapper = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS)
        .build()
        .registerModule(new JavaTimeModule()
            .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyyMMdd"))));

  }

  public <T> Flux<T> read(Class<T> type, BufferedReader reader) {
    return Flux.fromStream(reader.lines().map(line -> {
      try {
        return fromLine(line, type);
      } catch (Exception x) {
        log.error("[{}: {}]. Error in mapping the line [{}]", x.getClass().getName(), x.getMessage(), line);
        return null;
      }
    }))
        .switchIfEmpty(Flux.error(new Exception("Error happened parsing systemA input.")));
  }

  private <T> T fromLine(String line, Class<T> type) throws Exception {
    T target = type.getConstructor().newInstance();
    Arrays.stream(type.getDeclaredFields())
        .forEach(f -> {
          try {
            if (f.isAnnotationPresent(SystemAField.class)) {
              Fields column = f.getDeclaredAnnotation(SystemAField.class).field();
              String valueStr = line.substring(column.getStartIndex(), column.getStartIndex() + column.getLen()).trim();
              if (f.getType() == LocalDate.class) {
                valueStr = '"' + valueStr + '"';
              }
              log.debug("Parsing [{}] for [{}] based on [{}]... ", valueStr, f.getName(), column.toString());
              f.setAccessible(true);
              if (f.getType() == String.class) {
                ReflectionUtils.setField(f, target, valueStr);
              } else {
                ReflectionUtils.setField(f, target, objectMapper.readValue(valueStr, f.getType()));
              }
              log.debug("The value for the field [{}] has been set to [{}]", f.getName(),
                  ReflectionUtils.getField(f, target));
            } else if (f.isAnnotationPresent(SystemARecord.class)) {
              f.setAccessible(true);
              ReflectionUtils.setField(f, target, fromLine(line, f.getType()));
            }
          } catch (Exception x) {
            log.error("Error in mapping the field [{}]", f.getName(), x);
            throw new RuntimeException("Parsing failed.", x);
          }
        });
    return target;
  }
}
