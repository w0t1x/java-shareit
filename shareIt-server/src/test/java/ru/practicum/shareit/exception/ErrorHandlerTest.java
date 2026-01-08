package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void handleNotFound_returnsMessage() {
        Map<String, String> res = handler.handleNotFound(new NotFoundException("nf"));
        assertEquals("nf", res.get("error"));
    }

    @Test
    void handleForbidden_returnsMessage() {
        Map<String, String> res = handler.handleForbidden(new ForbiddenException("forb"));
        assertEquals("forb", res.get("error"));
    }

    @Test
    void handleBadRequest_returnsMessage() {
        Map<String, String> res = handler.handleBadRequest(new IllegalArgumentException("bad"));
        assertEquals("bad", res.get("error"));
    }

    @Test
    void handleValidation_returnsMessage() {
        Map<String, String> res = handler.handleValidation(new ValidationException("val"));
        assertEquals("val", res.get("error"));
    }

    @Test
    void handleMissingHeader_returnsMessage() throws Exception {
        // Создаём реальный MethodParameter, чтобы MissingRequestHeaderException.getMessage() не падал
        Method m = Dummy.class.getDeclaredMethod("endpoint", Long.class);
        MethodParameter mp = new MethodParameter(m, 0);

        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-Sharer-User-Id", mp);

        Map<String, String> res = handler.handleMissingHeader(ex);

        assertNotNull(res.get("error"));
        assertTrue(res.get("error").contains("X-Sharer-User-Id"));
    }

    @Test
    void handleBadJson_returnsMalformed() {
        // В Spring 6 удобнее передать null-inputMessage, чем вызывать "короткий" конструктор
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("x", (ServerHttpRequest) null);
        Map<String, String> res = handler.handleBadJson(ex);
        assertEquals("Malformed JSON", res.get("error"));
    }

    @Test
    void handleConflict_returnsConflict() {
        Map<String, String> res = handler.handleConflict(new DataIntegrityViolationException("dup"));
        assertEquals("Conflict", res.get("error"));
    }

    @Test
    void handleOther_returnsInternalMessage() {
        Map<String, String> res = handler.handleOther(new RuntimeException("boom"));
        assertEquals("Internal server error", res.get("error"));
    }

    // dummy-метод только чтобы получить MethodParameter
    private static class Dummy {
        @SuppressWarnings("unused")
        public void endpoint(@RequestHeader("X-Sharer-User-Id") Long userId) {
        }
    }
}
