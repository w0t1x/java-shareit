package ru.practicum.shareit.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Утилита для тестов: сериализация DTO в JSON без ручной сборки строк.
 * Это убирает IDE-предупреждения вида "JSON standard does not allow such tokens".
 */
public final class TestJson {

    private TestJson() {
    }

    /**
     * Превращает объект в JSON-строку через Jackson.
     */
    public static String json(ObjectMapper mapper, Object dto) {
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize object to JSON: " + dto, e);
        }
    }

    /**
     * Удобный HttpEntity для TestRestTemplate/RestTemplate: JSON + Content-Type.
     */
    public static <T> HttpEntity<T> entity(T body, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) {
            headers.add("X-Sharer-User-Id", String.valueOf(userId));
        }
        return new HttpEntity<>(body, headers);
    }
}
