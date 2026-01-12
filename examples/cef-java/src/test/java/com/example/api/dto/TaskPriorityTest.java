package com.example.api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TaskPriority Enum")
class TaskPriorityTest {

    @Nested
    @DisplayName("Enum Constants")
    class EnumConstantsTests {
        @Test
        void all_enum_constants_exist() {
            assertThat(TaskPriority.LOW).isNotNull();
            assertThat(TaskPriority.MEDIUM).isNotNull();
            assertThat(TaskPriority.HIGH).isNotNull();
            assertThat(TaskPriority.CRITICAL).isNotNull();
        }

        @Test
        void enum_values_count() {
            assertThat(TaskPriority.values()).hasSize(4);
        }

        @Test
        void all_enum_values_are_unique() {
            assertThat(TaskPriority.values()).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("getValue() method")
    class GetValueTests {
        @Test
        void low_has_correct_value() {
            assertThat(TaskPriority.LOW.getValue()).isEqualTo("low");
        }

        @Test
        void critical_has_correct_value() {
            assertThat(TaskPriority.CRITICAL.getValue()).isEqualTo("critical");
        }

        @Test
        void all_values_are_non_null() {
            for (TaskPriority priority : TaskPriority.values()) {
                assertThat(priority.getValue()).isNotNull().isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getDisplayName() method")
    class GetDisplayNameTests {
        @Test
        void low_display_name() {
            assertThat(TaskPriority.LOW.getDisplayName()).isEqualTo("Low");
        }

        @Test
        void critical_display_name() {
            assertThat(TaskPriority.CRITICAL.getDisplayName()).isEqualTo("Critical");
        }
    }

    @Nested
    @DisplayName("getWeight() method")
    class GetWeightTests {
        @Test
        void low_weight() {
            assertThat(TaskPriority.LOW.getWeight()).isEqualTo(1);
        }

        @Test
        void critical_weight() {
            assertThat(TaskPriority.CRITICAL.getWeight()).isEqualTo(4);
        }

        @Test
        void weights_in_ascending_order() {
            TaskPriority[] values = TaskPriority.values();
            for (int i = 0; i < values.length - 1; i++) {
                assertThat(values[i].getWeight()).isLessThan(values[i + 1].getWeight());
            }
        }
    }

    @Nested
    @DisplayName("getUrgent() method")
    class GetUrgentTests {
        @Test
        void low_not_urgent() {
            assertThat(TaskPriority.LOW.getUrgent()).isFalse();
        }

        @Test
        void high_urgent() {
            assertThat(TaskPriority.HIGH.getUrgent()).isTrue();
        }

        @Test
        void two_priorities_urgent() {
            long urgentCount = 0;
            for (TaskPriority priority : TaskPriority.values()) {
                if (priority.getUrgent()) {
                    urgentCount++;
                }
            }
            assertThat(urgentCount).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Enum Operations")
    class EnumOperationsTests {
        @Test
        void enum_value_of_works() {
            assertThat(TaskPriority.valueOf("LOW")).isEqualTo(TaskPriority.LOW);
        }

        @Test
        void invalid_value_throws_exception() {
            assertThatThrownBy(() -> TaskPriority.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void ordinal_values() {
            assertThat(TaskPriority.LOW.ordinal()).isEqualTo(0);
            assertThat(TaskPriority.CRITICAL.ordinal()).isEqualTo(3);
        }
    }
}
