package com.example.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TaskListResponse DTO")
class TaskListResponseTest {
    private TaskListResponse response;

    @BeforeEach
    void setUp() {
        response = new TaskListResponse(
            Arrays.asList(
                new Task("1", "Task1", "Desc", TaskStatus.PENDING, TaskPriority.HIGH, "user", Arrays.asList("tag"), "2024-01-01T10:00:00Z", "2024-01-01T10:00:00Z", "2024-01-10T00:00:00Z")
            ),
            10, 1, 10
        );
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {
        @Test
        void default_constructor() {
            TaskListResponse r = new TaskListResponse();
            assertThat(r).isNotNull();
            assertThat(r.getTasks()).isNull();
        }

        @Test
        void full_constructor() {
            assertThat(response.getTasks()).hasSize(1);
            assertThat(response.getTotal()).isEqualTo(10);
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getPageSize()).isEqualTo(10);
        }

        @Test
        void empty_tasks() {
            TaskListResponse r = new TaskListResponse(new ArrayList<>(), 0, 1, 10);
            assertThat(r.getTasks()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getters/Setters")
    class GettersSetters {
        @Test
        void set_get_total() {
            response.setTotal(100);
            assertThat(response.getTotal()).isEqualTo(100);
        }

        @Test
        void set_get_page() {
            response.setPage(5);
            assertThat(response.getPage()).isEqualTo(5);
        }

        @Test
        void set_get_page_size() {
            response.setPageSize(50);
            assertThat(response.getPageSize()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void builder_creates() {
            TaskListResponse r = TaskListResponse.builder()
                .total(20)
                .page(2)
                .pageSize(10)
                .build();
            assertThat(r.getTotal()).isEqualTo(20);
            assertThat(r.getPage()).isEqualTo(2);
        }
    }
}
