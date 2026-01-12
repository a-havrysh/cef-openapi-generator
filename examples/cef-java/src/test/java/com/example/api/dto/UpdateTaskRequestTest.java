package com.example.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UpdateTaskRequest DTO")
class UpdateTaskRequestTest {
    private UpdateTaskRequest request;

    @BeforeEach
    void setUp() {
        request = new UpdateTaskRequest("Updated", "Desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, "user", Arrays.asList("tag"), "2024-01-10T00:00:00Z");
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {
        @Test
        void default_constructor() {
            UpdateTaskRequest r = new UpdateTaskRequest();
            assertThat(r).isNotNull();
        }

        @Test
        void full_constructor() {
            assertThat(request.getTitle()).isEqualTo("Updated");
            assertThat(request.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
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
        void set_get_priority() {
            request.setPriority(TaskPriority.CRITICAL);
            assertThat(request.getPriority()).isEqualTo(TaskPriority.CRITICAL);
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void builder_creates() {
            UpdateTaskRequest r = UpdateTaskRequest.builder()
                .status(TaskStatus.BLOCKED)
                .build();
            assertThat(r.getStatus()).isEqualTo(TaskStatus.BLOCKED);
        }
    }

    @Nested
    @DisplayName("All Statuses and Priorities")
    class AllValues {
        @Test
        void all_statuses() {
            for (TaskStatus s : TaskStatus.values()) {
                UpdateTaskRequest r = UpdateTaskRequest.builder().status(s).build();
                assertThat(r.getStatus()).isEqualTo(s);
            }
        }

        @Test
        void all_priorities() {
            for (TaskPriority p : TaskPriority.values()) {
                UpdateTaskRequest r = UpdateTaskRequest.builder().priority(p).build();
                assertThat(r.getPriority()).isEqualTo(p);
            }
        }
    }
}
