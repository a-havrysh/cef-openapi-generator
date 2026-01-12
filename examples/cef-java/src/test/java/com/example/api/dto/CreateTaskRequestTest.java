package com.example.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CreateTaskRequest DTO")
class CreateTaskRequestTest {
    private CreateTaskRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateTaskRequest("Test", "Desc", TaskPriority.HIGH, "user", Arrays.asList("tag1"), "2024-01-10T00:00:00Z");
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {
        @Test
        void default_constructor() {
            CreateTaskRequest r = new CreateTaskRequest();
            assertThat(r).isNotNull();
            assertThat(r.getTitle()).isNull();
        }

        @Test
        void full_constructor() {
            assertThat(request.getTitle()).isEqualTo("Test");
            assertThat(request.getPriority()).isEqualTo(TaskPriority.HIGH);
        }
    }

    @Nested
    @DisplayName("Getters/Setters")
    class GettersSetters {
        @Test
        void set_get_title() {
            request.setTitle("New Title");
            assertThat(request.getTitle()).isEqualTo("New Title");
        }

        @Test
        void set_get_priority() {
            request.setPriority(TaskPriority.CRITICAL);
            assertThat(request.getPriority()).isEqualTo(TaskPriority.CRITICAL);
        }

        @Test
        void set_get_tags() {
            request.setTags(Arrays.asList("new"));
            assertThat(request.getTags()).contains("new");
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void builder_creates_instance() {
            CreateTaskRequest r = CreateTaskRequest.builder()
                .title("Title")
                .priority(TaskPriority.LOW)
                .build();
            assertThat(r.getTitle()).isEqualTo("Title");
            assertThat(r.getPriority()).isEqualTo(TaskPriority.LOW);
        }

        @Test
        void builder_partial() {
            CreateTaskRequest r = CreateTaskRequest.builder()
                .title("Title")
                .build();
            assertThat(r.getTitle()).isEqualTo("Title");
            assertThat(r.getDescription()).isNull();
        }

        @Test
        void builder_override() {
            CreateTaskRequest r = CreateTaskRequest.builder()
                .title("T1")
                .title("T2")
                .build();
            assertThat(r.getTitle()).isEqualTo("T2");
        }
    }

    @Nested
    @DisplayName("All Priorities")
    class AllPriorities {
        @Test
        void all_priority_values() {
            for (TaskPriority p : TaskPriority.values()) {
                CreateTaskRequest r = CreateTaskRequest.builder().priority(p).build();
                assertThat(r.getPriority()).isEqualTo(p);
            }
        }
    }
}
