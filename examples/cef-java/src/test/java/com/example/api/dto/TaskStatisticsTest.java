package com.example.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TaskStatistics DTO")
class TaskStatisticsTest {
    private TaskStatistics stats;

    @BeforeEach
    void setUp() {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("pending", 5);
        Map<String, Integer> priorityMap = new HashMap<>();
        priorityMap.put("high", 3);
        stats = new TaskStatistics(10, statusMap, priorityMap);
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {
        @Test
        void default_constructor() {
            TaskStatistics s = new TaskStatistics();
            assertThat(s).isNotNull();
            assertThat(s.getTotalTasks()).isNull();
        }

        @Test
        void full_constructor() {
            assertThat(stats.getTotalTasks()).isEqualTo(10);
            assertThat(stats.getByStatus()).isNotNull().containsKey("pending");
        }

        @Test
        void empty_maps() {
            TaskStatistics s = new TaskStatistics(0, new HashMap<>(), new HashMap<>());
            assertThat(s.getByStatus()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getters/Setters")
    class GettersSetters {
        @Test
        void set_get_total() {
            stats.setTotalTasks(50);
            assertThat(stats.getTotalTasks()).isEqualTo(50);
        }

        @Test
        void set_get_by_status() {
            Map<String, Integer> map = new HashMap<>();
            map.put("completed", 10);
            stats.setByStatus(map);
            assertThat(stats.getByStatus()).containsKey("completed");
        }

        @Test
        void set_get_by_priority() {
            Map<String, Integer> map = new HashMap<>();
            map.put("critical", 5);
            stats.setByPriority(map);
            assertThat(stats.getByPriority()).containsKey("critical");
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void builder_creates() {
            TaskStatistics s = TaskStatistics.builder()
                .totalTasks(25)
                .build();
            assertThat(s.getTotalTasks()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Map Handling")
    class MapHandling {
        @Test
        void large_maps() {
            Map<String, Integer> map = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                map.put("key" + i, i);
            }
            TaskStatistics s = new TaskStatistics(100, map, map);
            assertThat(s.getByStatus()).hasSize(100);
        }
    }
}
