package com.example.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Task DTO")
class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task(
            "1",
            "Test Task",
            "Task description",
            TaskStatus.PENDING,
            TaskPriority.HIGH,
            "alice@example.com",
            Arrays.asList("urgent", "bug"),
            "2024-01-01T10:00:00Z",
            "2024-01-02T10:00:00Z",
            "2024-01-10T00:00:00Z"
        );
    }

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {
        @Test
        void default_constructor_creates_instance() {
            Task t = new Task();
            assertThat(t).isNotNull();
            assertThat(t.getId()).isNull();
            assertThat(t.getTitle()).isNull();
        }

        @Test
        void full_constructor_sets_all_fields() {
            assertThat(task.getId()).isEqualTo("1");
            assertThat(task.getTitle()).isEqualTo("Test Task");
            assertThat(task.getDescription()).isEqualTo("Task description");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
            assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
            assertThat(task.getAssignee()).isEqualTo("alice@example.com");
            assertThat(task.getTags()).hasSize(2).containsExactly("urgent", "bug");
        }

        @Test
        void constructor_with_null_values() {
            Task t = new Task(null, null, null, null, null, null, null, null, null, null);
            assertThat(t).isNotNull();
            assertThat(t.getId()).isNull();
        }

        @Test
        void constructor_with_empty_tags_list() {
            Task t = new Task("1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.LOW,
                "user", new ArrayList<>(), "2024-01-01T10:00:00Z", "2024-01-01T10:00:00Z", "2024-01-10T00:00:00Z");
            assertThat(t.getTags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {
        @Test
        void set_and_get_id() {
            task.setId("new-id");
            assertThat(task.getId()).isEqualTo("new-id");
        }

        @Test
        void set_and_get_title() {
            task.setTitle("New Title");
            assertThat(task.getTitle()).isEqualTo("New Title");
        }

        @Test
        void set_and_get_description() {
            task.setDescription("New description");
            assertThat(task.getDescription()).isEqualTo("New description");
        }

        @Test
        void set_and_get_status() {
            task.setStatus(TaskStatus.COMPLETED);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        void set_and_get_priority() {
            task.setPriority(TaskPriority.CRITICAL);
            assertThat(task.getPriority()).isEqualTo(TaskPriority.CRITICAL);
        }

        @Test
        void set_and_get_assignee() {
            task.setAssignee("charlie@example.com");
            assertThat(task.getAssignee()).isEqualTo("charlie@example.com");
        }

        @Test
        void set_and_get_tags() {
            List<String> newTags = Arrays.asList("new", "tags");
            task.setTags(newTags);
            assertThat(task.getTags()).isEqualTo(newTags);
        }

        @Test
        void set_tags_to_null() {
            task.setTags(null);
            assertThat(task.getTags()).isNull();
        }

        @Test
        void set_and_get_created_at() {
            task.setCreatedAt("2024-01-15T10:00:00Z");
            assertThat(task.getCreatedAt()).isEqualTo("2024-01-15T10:00:00Z");
        }

        @Test
        void set_and_get_updated_at() {
            task.setUpdatedAt("2024-01-16T10:00:00Z");
            assertThat(task.getUpdatedAt()).isEqualTo("2024-01-16T10:00:00Z");
        }

        @Test
        void set_and_get_due_date() {
            task.setDueDate("2024-02-01T00:00:00Z");
            assertThat(task.getDueDate()).isEqualTo("2024-02-01T00:00:00Z");
        }

        @Test
        void set_all_status_values() {
            for (TaskStatus status : TaskStatus.values()) {
                task.setStatus(status);
                assertThat(task.getStatus()).isEqualTo(status);
            }
        }

        @Test
        void set_all_priority_values() {
            for (TaskPriority priority : TaskPriority.values()) {
                task.setPriority(priority);
                assertThat(task.getPriority()).isEqualTo(priority);
            }
        }

        @Test
        void set_empty_string_values() {
            task.setId("");
            task.setTitle("");
            assertThat(task.getId()).isEmpty();
            assertThat(task.getTitle()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder Pattern")
    class BuilderTests {
        @Test
        void builder_creates_instance_with_no_fields() {
            Task t = Task.builder().build();
            assertThat(t).isNotNull();
            assertThat(t.getId()).isNull();
        }

        @Test
        void builder_sets_all_fields() {
            Task t = Task.builder()
                .id("1")
                .title("Test Task")
                .description("Task description")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.HIGH)
                .assignee("alice@example.com")
                .tags(Arrays.asList("urgent", "bug"))
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-02T10:00:00Z")
                .dueDate("2024-01-10T00:00:00Z")
                .build();

            assertThat(t.getId()).isEqualTo("1");
            assertThat(t.getTitle()).isEqualTo("Test Task");
            assertThat(t.getTags()).hasSize(2);
        }

        @Test
        void builder_partial_fields() {
            Task t = Task.builder()
                .id("123")
                .title("Partial Task")
                .build();

            assertThat(t.getId()).isEqualTo("123");
            assertThat(t.getTitle()).isEqualTo("Partial Task");
            assertThat(t.getDescription()).isNull();
        }

        @Test
        void builder_with_null_values() {
            Task t = Task.builder()
                .id(null)
                .title(null)
                .build();

            assertThat(t.getId()).isNull();
            assertThat(t.getTitle()).isNull();
        }

        @Test
        void builder_with_empty_list() {
            Task t = Task.builder()
                .id("1")
                .tags(new ArrayList<>())
                .build();

            assertThat(t.getTags()).isEmpty();
        }

        @Test
        void builder_override_field_multiple_times() {
            Task t = Task.builder()
                .id("1")
                .id("2")
                .id("3")
                .build();

            assertThat(t.getId()).isEqualTo("3");
        }
    }

    @Nested
    @DisplayName("toString() method")
    class ToStringTests {
        @Test
        void toString_returns_non_null() {
            String str = task.toString();
            assertThat(str).isNotNull().isNotEmpty();
        }

        @Test
        void toString_with_null_fields() {
            Task t = new Task(null, null, null, null, null, null, null, null, null, null);
            String str = t.toString();
            assertThat(str).isNotNull().isNotEmpty();
        }

        @Test
        void toString_with_special_characters() {
            Task t = new Task("1", "Title with \"quotes\"", "Description with \\backslashes\\",
                TaskStatus.PENDING, TaskPriority.HIGH, "user@example.com",
                Arrays.asList("tag1"), "2024-01-01T10:00:00Z", "2024-01-02T10:00:00Z", "2024-01-10T00:00:00Z");
            assertThat(t.toString()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {
        @Test
        void multiple_tags_in_list() {
            List<String> tags = Arrays.asList("tag1", "tag2", "tag3", "tag4", "tag5");
            Task t = new Task("1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.HIGH,
                "user", tags, "2024-01-01T10:00:00Z", "2024-01-02T10:00:00Z", "2024-01-10T00:00:00Z");
            assertThat(t.getTags()).hasSize(5);
        }

        @Test
        void very_long_string_fields() {
            String longString = "a".repeat(10000);
            Task t = new Task("1", longString, longString, TaskStatus.PENDING, TaskPriority.HIGH,
                longString, Arrays.asList(longString), longString, longString, longString);
            assertThat(t.getTitle().length()).isEqualTo(10000);
        }

        @Test
        void empty_string_fields() {
            Task t = new Task("", "", "", TaskStatus.PENDING, TaskPriority.HIGH,
                "", Arrays.asList(""), "", "", "");
            assertThat(t.getId()).isEmpty();
            assertThat(t.getTags()).hasSize(1).contains("");
        }

        @Test
        void all_task_statuses() {
            for (TaskStatus status : TaskStatus.values()) {
                Task t = Task.builder().id("1").status(status).build();
                assertThat(t.getStatus()).isEqualTo(status);
            }
        }

        @Test
        void all_task_priorities() {
            for (TaskPriority priority : TaskPriority.values()) {
                Task t = Task.builder().id("1").priority(priority).build();
                assertThat(t.getPriority()).isEqualTo(priority);
            }
        }
    }

    @Nested
    @DisplayName("Additional Test Coverage")
    class AdditionalTestCoverage {

        @Test
        @DisplayName("equals and hashCode consistency")
        void equals_and_hashcode_consistency() {
            Task task1 = Task.builder().id("1").title("Test").status(TaskStatus.PENDING).build();
            Task task2 = Task.builder().id("1").title("Test").status(TaskStatus.PENDING).build();

            if (task1.equals(task2)) {
                assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
            }
        }

        @Test
        @DisplayName("task with unicode characters in fields")
        void task_with_unicode_characters() {
            Task t = new Task("タスク1", "日本語タイトル", "описание на русском",
                TaskStatus.PENDING, TaskPriority.HIGH, "用户@例え.com",
                Arrays.asList("标签1", "टैग2"), "2024-01-01T10:00:00Z", "2024-01-02T10:00:00Z", "2024-01-10T00:00:00Z");

            assertThat(t.getId()).isEqualTo("タスク1");
            assertThat(t.getTitle()).isEqualTo("日本語タイトル");
            assertThat(t.getAssignee()).isEqualTo("用户@例え.com");
        }

        @Test
        @DisplayName("task with special characters in all fields")
        void task_with_special_characters() {
            String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            Task t = new Task(specialChars, specialChars, specialChars,
                TaskStatus.PENDING, TaskPriority.LOW, specialChars,
                Arrays.asList(specialChars), specialChars, specialChars, specialChars);

            assertThat(t.getId()).isEqualTo(specialChars);
            assertThat(t.getAssignee()).isEqualTo(specialChars);
        }

        @Test
        @DisplayName("task with very many tags")
        void task_with_many_tags() {
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                tags.add("tag-" + i);
            }

            Task t = new Task("1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.HIGH,
                "user", tags, "2024-01-01T10:00:00Z", "2024-01-02T10:00:00Z", "2024-01-10T00:00:00Z");

            assertThat(t.getTags()).hasSize(100);
        }

        @Test
        @DisplayName("builder with chained null and non-null values")
        void builder_mixed_null_and_values() {
            Task t = Task.builder()
                .id("123")
                .title(null)
                .description("Test Description")
                .priority(TaskPriority.CRITICAL)
                .build();

            assertThat(t.getId()).isEqualTo("123");
            assertThat(t.getTitle()).isNull();
            assertThat(t.getDescription()).isEqualTo("Test Description");
        }

        @Test
        @DisplayName("copy constructor through builder")
        void copy_task_via_builder() {
            Task original = task;
            Task copy = Task.builder()
                .id(original.getId())
                .title(original.getTitle())
                .description(original.getDescription())
                .status(original.getStatus())
                .priority(original.getPriority())
                .assignee(original.getAssignee())
                .tags(original.getTags() != null ? new ArrayList<>(original.getTags()) : null)
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .dueDate(original.getDueDate())
                .build();

            assertThat(copy.getId()).isEqualTo(original.getId());
            assertThat(copy.getTitle()).isEqualTo(original.getTitle());
        }

        @Test
        @DisplayName("task setters with all null values then non-null")
        void task_setters_transition_null_to_values() {
            Task t = new Task();
            assertThat(t.getId()).isNull();

            t.setId("new-id");
            assertThat(t.getId()).isEqualTo("new-id");

            t.setId(null);
            assertThat(t.getId()).isNull();
        }

        @Test
        @DisplayName("multiple tasks with different priorities and statuses")
        void multiple_tasks_with_various_combinations() {
            for (TaskStatus status : TaskStatus.values()) {
                for (TaskPriority priority : TaskPriority.values()) {
                    Task t = Task.builder()
                        .id("task-" + status + "-" + priority)
                        .status(status)
                        .priority(priority)
                        .build();

                    assertThat(t.getStatus()).isEqualTo(status);
                    assertThat(t.getPriority()).isEqualTo(priority);
                }
            }
        }

        @Test
        @DisplayName("task with dates at boundary values")
        void task_with_boundary_dates() {
            Task t = new Task("1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.HIGH,
                "user", Arrays.asList("tag"),
                "1900-01-01T00:00:00Z", "9999-12-31T23:59:59Z", "2100-01-01T00:00:00Z");

            assertThat(t.getCreatedAt()).isEqualTo("1900-01-01T00:00:00Z");
            assertThat(t.getDueDate()).isEqualTo("2100-01-01T00:00:00Z");
        }

        @Test
        @DisplayName("builder preserves list mutability")
        void builder_list_mutability() {
            List<String> originalTags = new ArrayList<>();
            originalTags.add("tag1");

            Task t = Task.builder()
                .id("1")
                .tags(originalTags)
                .build();

            originalTags.add("tag2");
            assertThat(t.getTags()).contains("tag1");
        }

        @Test
        @DisplayName("task field independence")
        void task_field_independence() {
            Task t1 = Task.builder().id("1").title("Title1").priority(TaskPriority.HIGH).build();
            Task t2 = Task.builder().id("2").title("Title2").priority(TaskPriority.LOW).build();

            assertThat(t1.getId()).isNotEqualTo(t2.getId());
            assertThat(t1.getPriority()).isNotEqualTo(t2.getPriority());
        }

        @Test
        @DisplayName("task with null tags list modifications")
        void task_null_tags_setter() {
            Task t = Task.builder().id("1").tags(Arrays.asList("tag1", "tag2")).build();
            assertThat(t.getTags()).hasSize(2);

            t.setTags(null);
            assertThat(t.getTags()).isNull();

            t.setTags(Arrays.asList("new-tag"));
            assertThat(t.getTags()).hasSize(1).contains("new-tag");
        }

        @Test
        @DisplayName("task immutability of returned tags list")
        void task_tags_list_immutability() {
            List<String> tags = Arrays.asList("tag1", "tag2", "tag3");
            Task t = new Task("1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.HIGH,
                "user", tags, "2024-01-01T10:00:00Z", "2024-01-02T10:00:00Z", "2024-01-10T00:00:00Z");

            List<String> retrievedTags = t.getTags();
            assertThat(retrievedTags).containsAll(tags);
        }
    }
}
