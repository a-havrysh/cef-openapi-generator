package com.example.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ErrorResponse DTO")
class ErrorResponseTest {
    private ErrorResponse response;

    @BeforeEach
    void setUp() {
        Map<String, String> details = new HashMap<>();
        details.put("field", "title");
        response = new ErrorResponse("BAD_REQUEST", "Validation failed", details);
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {
        @Test
        void default_constructor() {
            ErrorResponse r = new ErrorResponse();
            assertThat(r).isNotNull();
            assertThat(r.getError()).isNull();
        }

        @Test
        void full_constructor() {
            assertThat(response.getError()).isEqualTo("BAD_REQUEST");
            assertThat(response.getMessage()).isEqualTo("Validation failed");
            assertThat(response.getDetails()).isNotNull();
        }

        @Test
        void constructor_with_null_details() {
            ErrorResponse r = new ErrorResponse("ERROR", "Message", null);
            assertThat(r.getDetails()).isNull();
        }
    }

    @Nested
    @DisplayName("Getters/Setters")
    class GettersSetters {
        @Test
        void set_get_error() {
            response.setError("NOT_FOUND");
            assertThat(response.getError()).isEqualTo("NOT_FOUND");
        }

        @Test
        void set_get_message() {
            response.setMessage("New message");
            assertThat(response.getMessage()).isEqualTo("New message");
        }

        @Test
        void set_get_details_as_map() {
            Map<String, String> details = new HashMap<>();
            details.put("code", "500");
            response.setDetails(details);
            assertThat(response.getDetails()).isInstanceOf(Map.class);
        }

        @Test
        void set_get_details_as_string() {
            response.setDetails("String details");
            assertThat(response.getDetails()).isInstanceOf(String.class);
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void builder_creates() {
            ErrorResponse r = ErrorResponse.builder()
                .error("INTERNAL_ERROR")
                .message("Server error")
                .build();
            assertThat(r.getError()).isEqualTo("INTERNAL_ERROR");
            assertThat(r.getMessage()).isEqualTo("Server error");
        }

        @Test
        void builder_with_different_detail_types() {
            ErrorResponse r1 = ErrorResponse.builder().details(new HashMap<>()).build();
            ErrorResponse r2 = ErrorResponse.builder().details("string").build();
            assertThat(r1.getDetails()).isInstanceOf(Map.class);
            assertThat(r2.getDetails()).isInstanceOf(String.class);
        }
    }
}
