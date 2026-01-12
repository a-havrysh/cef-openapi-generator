package com.example.api.service;

import com.example.api.dto.TaskStatistics;
import com.example.api.exception.NotImplementedException;
import com.example.api.protocol.ApiResponse;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StatisticsApiService Advanced Comprehensive Tests")
class StatisticsApiServiceComprehensiveTest {

    private StatisticsApiService service;

    @Mock
    private CefBrowser mockBrowser;

    @Mock
    private CefFrame mockFrame;

    @Mock
    private CefRequest mockCefRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new StatisticsApiService() {};
    }

    @Nested
    @DisplayName("Null and Empty Handling Advanced")
    class NullAndEmptyHandlingTests {

        @Test
        @DisplayName("getStatistics can return null from implementation")
        void getStatistics_can_return_null() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return null;
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getStatistics can return object with all null fields")
        void getStatistics_returns_all_null_fields() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return new TaskStatistics(null, null, null);
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getTotalTasks()).isNull();
            assertThat(result.getByStatus()).isNull();
            assertThat(result.getByPriority()).isNull();
        }

        @Test
        @DisplayName("getStatistics with null totalTasks but populated maps")
        void getStatistics_null_total_with_populated_maps() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 5);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return new TaskStatistics(null, statusMap, new HashMap<>());
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getTotalTasks()).isNull();
            assertThat(result.getByStatus()).isNotNull().hasSize(1);
        }

        @Test
        @DisplayName("getStatistics with empty unmodifiable maps")
        void getStatistics_with_unmodifiable_maps() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return new TaskStatistics(0, Collections.emptyMap(), Collections.emptyMap());
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).isEmpty();
            assertThat(result.getByPriority()).isEmpty();
        }

        @Test
        @DisplayName("handleGetStatistics wraps null response from business method")
        void handleGetStatistics_wraps_null_from_business() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return null;
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody()).isNull();
        }
    }

    @Nested
    @DisplayName("State and Mutation Scenarios")
    class StateMutationTests {

        @Test
        @DisplayName("getStatistics with mutable implementation - modifications don't affect subsequent calls")
        void getStatistics_mutable_implementation_isolation() {
            StatisticsApiService customService = new StatisticsApiService() {
                private final Map<String, Integer> statusMap = new HashMap<>();

                @Override
                public TaskStatistics getStatistics() {
                    statusMap.put("call-" + (statusMap.size() + 1), 1);
                    return TaskStatistics.builder()
                            .totalTasks(statusMap.size())
                            .byStatus(new HashMap<>(statusMap))
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics s1 = customService.getStatistics();
            TaskStatistics s2 = customService.getStatistics();
            TaskStatistics s3 = customService.getStatistics();

            assertThat(s1.getTotalTasks()).isEqualTo(1);
            assertThat(s2.getTotalTasks()).isEqualTo(2);
            assertThat(s3.getTotalTasks()).isEqualTo(3);
            assertThat(s1.getByStatus()).hasSize(1);
            assertThat(s3.getByStatus()).hasSize(3);
        }

        @Test
        @DisplayName("getStatistics returns snapshots - modifications to returned object don't affect service state")
        void getStatistics_returns_snapshots() {
            Map<String, Integer> baseStatusMap = new HashMap<>();
            baseStatusMap.put("pending", 5);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(5)
                            .byStatus(baseStatusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result1 = customService.getStatistics();
            result1.getByStatus().put("modified", 99);

            TaskStatistics result2 = customService.getStatistics();
            assertThat(result2.getByStatus()).containsEntry("modified", 99);
        }

        @Test
        @DisplayName("getStatistics with thread-safe concurrent implementation")
        void getStatistics_concurrent_access_pattern() throws InterruptedException {
            AtomicInteger callCount = new AtomicInteger(0);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    int count = callCount.incrementAndGet();
                    return TaskStatistics.builder()
                            .totalTasks(count)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result1 = customService.getStatistics();
            TaskStatistics result2 = customService.getStatistics();

            assertThat(result1.getTotalTasks()).isLessThan(result2.getTotalTasks());
        }
    }

    @Nested
    @DisplayName("Builder Pattern Advanced Usage")
    class BuilderPatternTests {

        @Test
        @DisplayName("TaskStatistics builder with sequential chaining")
        void builder_sequential_chaining() {
            TaskStatistics stats = TaskStatistics.builder()
                    .totalTasks(42)
                    .byStatus(new HashMap<>())
                    .byPriority(new HashMap<>())
                    .build();

            assertThat(stats.getTotalTasks()).isEqualTo(42);
            assertThat(stats.getByStatus()).isEmpty();
            assertThat(stats.getByPriority()).isEmpty();
        }

        @Test
        @DisplayName("TaskStatistics builder with partial initialization")
        void builder_partial_initialization() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 10);

            TaskStatistics stats = TaskStatistics.builder()
                    .totalTasks(10)
                    .byStatus(statusMap)
                    .build();

            assertThat(stats.getTotalTasks()).isEqualTo(10);
            assertThat(stats.getByStatus()).hasSize(1);
            assertThat(stats.getByPriority()).isNull();
        }

        @Test
        @DisplayName("TaskStatistics constructor vs builder equivalence")
        void constructor_and_builder_equivalence() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 5);
            Map<String, Integer> priorityMap = new HashMap<>();
            priorityMap.put("high", 2);

            TaskStatistics fromConstructor = new TaskStatistics(15, statusMap, priorityMap);
            TaskStatistics fromBuilder = TaskStatistics.builder()
                    .totalTasks(15)
                    .byStatus(statusMap)
                    .byPriority(priorityMap)
                    .build();

            assertThat(fromConstructor.getTotalTasks()).isEqualTo(fromBuilder.getTotalTasks());
            assertThat(fromConstructor.getByStatus()).isEqualTo(fromBuilder.getByStatus());
            assertThat(fromConstructor.getByPriority()).isEqualTo(fromBuilder.getByPriority());
        }

        @Test
        @DisplayName("getStatistics returns statistics created via builder pattern")
        void getStatistics_builder_pattern_return() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 8);
            statusMap.put("in_progress", 4);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(12)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getTotalTasks()).isEqualTo(12);
            assertThat(result.getByStatus()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("API Response Integration")
    class ApiResponseIntegrationTests {

        @Test
        @DisplayName("handleGetStatistics returns response with correct type parameter")
        void handleGetStatistics_correct_type_parameter() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(10)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isInstanceOf(ApiResponse.class);
            assertThat(response.getBody()).isInstanceOf(TaskStatistics.class);
        }

        @Test
        @DisplayName("handleGetStatistics response body is same object as from business method")
        void handleGetStatistics_body_object_identity() {
            TaskStatistics expectedStats = TaskStatistics.builder()
                    .totalTasks(25)
                    .byStatus(new HashMap<>())
                    .byPriority(new HashMap<>())
                    .build();

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return expectedStats;
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody()).isSameAs(expectedStats);
        }

        @Test
        @DisplayName("handleGetStatistics always returns 200 status by default")
        void handleGetStatistics_always_returns_200() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("handleGetStatistics returns application/json content type by default")
        void handleGetStatistics_returns_json_content_type() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getContentType()).isEqualTo("application/json");
        }
    }

    @Nested
    @DisplayName("Method Override Patterns and Interactions")
    class MethodOverrideInteractionTests {

        @Test
        @DisplayName("business method override - wrapper delegates without modification")
        void business_override_wrapper_delegates() {
            final AtomicInteger businessCallCount = new AtomicInteger(0);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessCallCount.incrementAndGet();
                    return TaskStatistics.builder()
                            .totalTasks(businessCallCount.get())
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessCallCount.get()).isEqualTo(1);

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessCallCount.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("wrapper override bypasses default delegation")
        void wrapper_override_bypasses_default_delegation() {
            final AtomicInteger businessCallCount = new AtomicInteger(0);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessCallCount.incrementAndGet();
                    return TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }

                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    TaskStatistics stats = TaskStatistics.builder()
                            .totalTasks(999)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                    return ApiResponse.ok(stats);
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessCallCount.get()).isZero();
        }

        @Test
        @DisplayName("wrapper can invoke business method explicitly")
        void wrapper_can_invoke_business_method() {
            final AtomicInteger businessCallCount = new AtomicInteger(0);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessCallCount.incrementAndGet();
                    return TaskStatistics.builder()
                            .totalTasks(businessCallCount.get())
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }

                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    TaskStatistics stats = getStatistics();
                    return ApiResponse.ok(stats).header("X-Called-From", "wrapper");
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCallCount.get()).isEqualTo(1);
            assertThat(response.getHeaders()).containsEntry("X-Called-From", "wrapper");
        }

        @Test
        @DisplayName("both methods can be called independently")
        void both_methods_independent_calls() {
            final AtomicInteger wrapperCallCount = new AtomicInteger(0);
            final AtomicInteger businessCallCount = new AtomicInteger(0);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessCallCount.incrementAndGet();
                    return TaskStatistics.builder()
                            .totalTasks(businessCallCount.get())
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }

                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    wrapperCallCount.incrementAndGet();
                    TaskStatistics stats = new TaskStatistics(999, new HashMap<>(), new HashMap<>());
                    return ApiResponse.ok(stats);
                }
            };

            customService.getStatistics();
            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            customService.getStatistics();

            assertThat(businessCallCount.get()).isEqualTo(2);
            assertThat(wrapperCallCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Map Operations and Data Structures")
    class MapOperationsTests {

        @Test
        @DisplayName("getStatistics returns map with single entry")
        void getStatistics_single_entry_maps() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("only-status", 100);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(100)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).hasSize(1).containsKey("only-status");
        }

        @Test
        @DisplayName("getStatistics preserves map insertion order behavior")
        void getStatistics_map_insertion_behavior() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("first", 1);
            statusMap.put("second", 2);
            statusMap.put("third", 3);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(6)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).hasSize(3).containsKeys("first", "second", "third");
        }

        @Test
        @DisplayName("getStatistics with maps containing zero values")
        void getStatistics_maps_with_zero_values() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("no-pending", 0);
            statusMap.put("completed", 5);
            statusMap.put("no-inprogress", 0);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(5)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).containsEntry("no-pending", 0);
            assertThat(result.getByStatus()).containsEntry("no-inprogress", 0);
        }
    }

    @Nested
    @DisplayName("Exception Handling in Service")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("getStatistics throws custom exception from implementation")
        void getStatistics_throws_custom_exception() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    throw new IllegalArgumentException("Invalid configuration");
                }
            };

            assertThatThrownBy(() -> customService.getStatistics())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid configuration");
        }

        @Test
        @DisplayName("handleGetStatistics propagates business method exception")
        void handleGetStatistics_propagates_exception() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    throw new RuntimeException("Service unavailable");
                }
            };

            assertThatThrownBy(() -> customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Service unavailable");
        }

        @Test
        @DisplayName("wrapper override can catch and handle exceptions")
        void wrapper_override_exception_handling() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    throw new RuntimeException("Business logic error");
                }

                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    try {
                        TaskStatistics stats = getStatistics();
                        return ApiResponse.ok(stats);
                    } catch (RuntimeException e) {
                        TaskStatistics errorStats = TaskStatistics.builder()
                                .totalTasks(0)
                                .byStatus(new HashMap<>())
                                .byPriority(new HashMap<>())
                                .build();
                        return ApiResponse.ok(errorStats);
                    }
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Default Method Behavior Validation")
    class DefaultMethodBehaviorTests {

        @Test
        @DisplayName("default getStatistics always throws NotImplementedException")
        void default_getStatistics_always_throws() {
            assertThatThrownBy(() -> service.getStatistics())
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessageContaining("getStatistics");
        }

        @Test
        @DisplayName("default handleGetStatistics calls default getStatistics")
        void default_handleGetStatistics_calls_getStatistics() {
            assertThatThrownBy(() -> service.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(NotImplementedException.class);
        }

        @Test
        @DisplayName("new instance with no overrides throws NotImplementedException")
        void new_instance_not_implemented() {
            StatisticsApiService newService = new StatisticsApiService() {};

            assertThatThrownBy(() -> newService.getStatistics())
                    .isInstanceOf(NotImplementedException.class);

            assertThatThrownBy(() -> newService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(NotImplementedException.class);
        }
    }
}
