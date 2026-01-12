package com.example.api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TaskStatus Enum")
class TaskStatusTest {

    @Nested
    @DisplayName("Enum Constants and Values")
    class EnumConstantsTests {
        @Test
        void all_enum_constants_exist() {
            assertThat(TaskStatus.PENDING).isNotNull();
            assertThat(TaskStatus.IN_PROGRESS).isNotNull();
            assertThat(TaskStatus.BLOCKED).isNotNull();
            assertThat(TaskStatus.REVIEW).isNotNull();
            assertThat(TaskStatus.COMPLETED).isNotNull();
            assertThat(TaskStatus.CANCELLED).isNotNull();
        }

        @Test
        void enum_values_count() {
            assertThat(TaskStatus.values()).hasSize(6);
        }

        @Test
        void all_enum_values_are_unique() {
            TaskStatus[] values = TaskStatus.values();
            assertThat(values).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("getValue() method")
    class GetValueTests {
        @Test
        void pending_has_correct_value() {
            assertThat(TaskStatus.PENDING.getValue()).isEqualTo("pending");
        }

        @Test
        void in_progress_has_correct_value() {
            assertThat(TaskStatus.IN_PROGRESS.getValue()).isEqualTo("in_progress");
        }

        @Test
        void blocked_has_correct_value() {
            assertThat(TaskStatus.BLOCKED.getValue()).isEqualTo("blocked");
        }

        @Test
        void review_has_correct_value() {
            assertThat(TaskStatus.REVIEW.getValue()).isEqualTo("review");
        }

        @Test
        void completed_has_correct_value() {
            assertThat(TaskStatus.COMPLETED.getValue()).isEqualTo("completed");
        }

        @Test
        void cancelled_has_correct_value() {
            assertThat(TaskStatus.CANCELLED.getValue()).isEqualTo("cancelled");
        }

        @Test
        void all_values_are_non_null_and_non_empty() {
            for (TaskStatus status : TaskStatus.values()) {
                assertThat(status.getValue()).isNotNull().isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getColor() method")
    class GetColorTests {
        @Test
        void all_colors_are_valid_hex_codes() {
            for (TaskStatus status : TaskStatus.values()) {
                String color = status.getColor();
                assertThat(color).matches("^#[0-9A-Fa-f]{6}$");
            }
        }

        @Test
        void pending_color() {
            assertThat(TaskStatus.PENDING.getColor()).isEqualTo("#6B7280");
        }

        @Test
        void completed_color() {
            assertThat(TaskStatus.COMPLETED.getColor()).isEqualTo("#10B981");
        }
    }

    @Nested
    @DisplayName("getDisplayName() method")
    class GetDisplayNameTests {
        @Test
        void pending_display_name() {
            assertThat(TaskStatus.PENDING.getDisplayName()).isEqualTo("Pending");
        }

        @Test
        void all_display_names_non_null() {
            for (TaskStatus status : TaskStatus.values()) {
                assertThat(status.getDisplayName()).isNotNull().isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getPriority() method")
    class GetPriorityTests {
        @Test
        void pending_priority() {
            assertThat(TaskStatus.PENDING.getPriority()).isEqualTo(1);
        }

        @Test
        void completed_priority() {
            assertThat(TaskStatus.COMPLETED.getPriority()).isEqualTo(5);
        }

        @Test
        void priorities_in_ascending_order() {
            TaskStatus[] values = TaskStatus.values();
            for (int i = 0; i < values.length - 1; i++) {
                assertThat(values[i].getPriority()).isLessThan(values[i + 1].getPriority());
            }
        }
    }

    @Nested
    @DisplayName("getActive() method")
    class GetActiveTests {
        @Test
        void pending_is_active() {
            assertThat(TaskStatus.PENDING.getActive()).isTrue();
        }

        @Test
        void completed_is_not_active() {
            assertThat(TaskStatus.COMPLETED.getActive()).isFalse();
        }

        @Test
        void four_statuses_are_active() {
            long activeCount = 0;
            for (TaskStatus status : TaskStatus.values()) {
                if (status.getActive()) {
                    activeCount++;
                }
            }
            assertThat(activeCount).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Enum Operations")
    class EnumOperationsTests {
        @Test
        void enum_name_method_works() {
            assertThat(TaskStatus.PENDING.name()).isEqualTo("PENDING");
            assertThat(TaskStatus.IN_PROGRESS.name()).isEqualTo("IN_PROGRESS");
        }

        @Test
        void enum_ordinal_method_works() {
            assertThat(TaskStatus.PENDING.ordinal()).isEqualTo(0);
            assertThat(TaskStatus.COMPLETED.ordinal()).isEqualTo(4);
        }

        @Test
        void enum_value_of_returns_correct_constant() {
            assertThat(TaskStatus.valueOf("PENDING")).isEqualTo(TaskStatus.PENDING);
        }

        @Test
        void enum_value_of_throws_for_invalid_name() {
            assertThatThrownBy(() -> TaskStatus.valueOf("INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Enum Consistency")
    class ConsistencyTests {
        @Test
        void all_status_values_have_non_null_properties() {
            for (TaskStatus status : TaskStatus.values()) {
                assertThat(status.getValue()).isNotNull();
                assertThat(status.getColor()).isNotNull();
                assertThat(status.getDisplayName()).isNotNull();
                assertThat(status.getPriority()).isNotNull();
                assertThat(status.getActive()).isNotNull();
            }
        }
    }
}
