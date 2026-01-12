package com.example.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UpdateTaskStatusRequest DTO")
class UpdateTaskStatusRequestTest {
    private UpdateTaskStatusRequest request;

    @BeforeEach
    void setUp() {
        request = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {
        @Test
        void default_constructor() {
            UpdateTaskStatusRequest r = new UpdateTaskStatusRequest();
            assertThat(r).isNotNull();
            assertThat(r.getStatus()).isNull();
        }

        @Test
        void full_constructor() {
            assertThat(request.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        void all_statuses() {
            for (TaskStatus s : TaskStatus.values()) {
                UpdateTaskStatusRequest r = new UpdateTaskStatusRequest(s);
                assertThat(r.getStatus()).isEqualTo(s);
            }
        }
    }

    @Nested
    @DisplayName("Getters/Setters")
    class GettersSetters {
        @Test
        void set_get_status() {
            request.setStatus(TaskStatus.COMPLETED);
            assertThat(request.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        void all_statuses_settable() {
            for (TaskStatus s : TaskStatus.values()) {
                request.setStatus(s);
                assertThat(request.getStatus()).isEqualTo(s);
            }
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void builder_creates() {
            UpdateTaskStatusRequest r = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.BLOCKED)
                .build();
            assertThat(r.getStatus()).isEqualTo(TaskStatus.BLOCKED);
        }

        @Test
        void builder_all_statuses() {
            for (TaskStatus s : TaskStatus.values()) {
                UpdateTaskStatusRequest r = UpdateTaskStatusRequest.builder()
                    .status(s)
                    .build();
                assertThat(r.getStatus()).isEqualTo(s);
            }
        }
    }
}
