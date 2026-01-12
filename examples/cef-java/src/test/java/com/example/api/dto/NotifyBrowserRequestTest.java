package com.example.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotifyBrowserRequest DTO")
class NotifyBrowserRequestTest {
    private NotifyBrowserRequest request;

    @BeforeEach
    void setUp() {
        request = new NotifyBrowserRequest("Message", "info", "user123");
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {
        @Test
        void default_constructor() {
            NotifyBrowserRequest r = new NotifyBrowserRequest();
            assertThat(r).isNotNull();
            assertThat(r.getMessage()).isNull();
        }

        @Test
        void full_constructor() {
            assertThat(request.getMessage()).isEqualTo("Message");
            assertThat(request.getType()).isEqualTo("info");
            assertThat(request.getUserId()).isEqualTo("user123");
        }
    }

    @Nested
    @DisplayName("Getters/Setters")
    class GettersSetters {
        @Test
        void set_get_message() {
            request.setMessage("New Message");
            assertThat(request.getMessage()).isEqualTo("New Message");
        }

        @Test
        void set_get_type() {
            request.setType("error");
            assertThat(request.getType()).isEqualTo("error");
        }

        @Test
        void set_get_user_id() {
            request.setUserId("user999");
            assertThat(request.getUserId()).isEqualTo("user999");
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void builder_creates() {
            NotifyBrowserRequest r = NotifyBrowserRequest.builder()
                .message("Test")
                .type("warning")
                .userId("user1")
                .build();
            assertThat(r.getMessage()).isEqualTo("Test");
            assertThat(r.getType()).isEqualTo("warning");
            assertThat(r.getUserId()).isEqualTo("user1");
        }
    }
}
