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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StatisticsApiService Interface")
class StatisticsApiServiceTest {

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
    @DisplayName("Statistics Retrieval Operations")
    class StatisticsRetrievalTests {

        @Test
        @DisplayName("getStatistics throws NotImplementedException by default")
        void getStatistics_default_throws_not_implemented() {
            assertThatThrownBy(() -> service.getStatistics())
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("getStatistics not implemented");
        }

        @Test
        @DisplayName("getStatistics can be overridden to return TaskStatistics")
        void getStatistics_can_be_overridden() {
            TaskStatistics expectedStats = TaskStatistics.builder()
                    .totalTasks(10)
                    .byStatus(new HashMap<>())
                    .byPriority(new HashMap<>())
                    .build();

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return expectedStats;
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result).isSameAs(expectedStats);
        }

        @Test
        @DisplayName("getStatistics returns TaskStatistics with total tasks")
        void getStatistics_returns_total_tasks() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(42)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getTotalTasks()).isEqualTo(42);
        }

        @Test
        @DisplayName("getStatistics returns TaskStatistics with status map")
        void getStatistics_returns_status_map() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 5);
            statusMap.put("in_progress", 3);
            statusMap.put("completed", 4);

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
            assertThat(result.getByStatus())
                    .containsEntry("pending", 5)
                    .containsEntry("in_progress", 3)
                    .containsEntry("completed", 4);
        }

        @Test
        @DisplayName("getStatistics returns TaskStatistics with priority map")
        void getStatistics_returns_priority_map() {
            Map<String, Integer> priorityMap = new HashMap<>();
            priorityMap.put("high", 2);
            priorityMap.put("medium", 5);
            priorityMap.put("low", 3);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(10)
                            .byStatus(new HashMap<>())
                            .byPriority(priorityMap)
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByPriority())
                    .containsEntry("high", 2)
                    .containsEntry("medium", 5)
                    .containsEntry("low", 3);
        }

        @Test
        @DisplayName("getStatistics returns complete TaskStatistics object")
        void getStatistics_returns_complete_statistics() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 10);
            statusMap.put("in_progress", 5);
            statusMap.put("completed", 8);

            Map<String, Integer> priorityMap = new HashMap<>();
            priorityMap.put("high", 4);
            priorityMap.put("medium", 12);
            priorityMap.put("low", 7);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(23)
                            .byStatus(statusMap)
                            .byPriority(priorityMap)
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();

            assertThat(result.getTotalTasks()).isEqualTo(23);
            assertThat(result.getByStatus()).hasSize(3).containsEntry("pending", 10);
            assertThat(result.getByPriority()).hasSize(3).containsEntry("high", 4);
        }

        @Test
        @DisplayName("handleGetStatistics delegates to getStatistics")
        void handleGetStatistics_delegates_to_get_statistics() {
            TaskStatistics expectedStats = TaskStatistics.builder()
                    .totalTasks(15)
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
        @DisplayName("handleGetStatistics wraps result in ApiResponse.ok()")
        void handleGetStatistics_wraps_in_api_response() {
            TaskStatistics stats = TaskStatistics.builder()
                    .totalTasks(20)
                    .byStatus(new HashMap<>())
                    .byPriority(new HashMap<>())
                    .build();

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return stats;
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getBody()).isSameAs(stats);
        }

        @Test
        @DisplayName("handleGetStatistics preserves all statistics data")
        void handleGetStatistics_preserves_all_data() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 7);
            statusMap.put("in_progress", 3);
            statusMap.put("completed", 5);

            Map<String, Integer> priorityMap = new HashMap<>();
            priorityMap.put("high", 3);
            priorityMap.put("medium", 8);
            priorityMap.put("low", 4);

            TaskStatistics stats = TaskStatistics.builder()
                    .totalTasks(15)
                    .byStatus(statusMap)
                    .byPriority(priorityMap)
                    .build();

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return stats;
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            TaskStatistics result = response.getBody();
            assertThat(result.getTotalTasks()).isEqualTo(15);
            assertThat(result.getByStatus()).isEqualTo(statusMap);
            assertThat(result.getByPriority()).isEqualTo(priorityMap);
        }

        @Test
        @DisplayName("handleGetStatistics can be overridden for custom response")
        void handleGetStatistics_can_be_overridden() {
            TaskStatistics stats = TaskStatistics.builder()
                    .totalTasks(25)
                    .byStatus(new HashMap<>())
                    .byPriority(new HashMap<>())
                    .build();

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.ok(stats).header("X-Cache", "hit");
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getHeaders()).containsEntry("X-Cache", "hit");
            assertThat(response.getBody().getTotalTasks()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Default Implementation Behavior")
    class DefaultImplementationTests {

        @Test
        @DisplayName("Business method throws NotImplementedException with correct message")
        void business_method_throws_correct_exception() {
            assertThatThrownBy(() -> service.getStatistics())
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("getStatistics not implemented");
        }

        @Test
        @DisplayName("NotImplementedException extends RuntimeException")
        void not_implemented_exception_is_runtime_exception() {
            Throwable thrown = catchThrowable(() -> service.getStatistics());

            assertThat(thrown)
                    .isInstanceOf(RuntimeException.class)
                    .isInstanceOf(NotImplementedException.class);
        }

        @Test
        @DisplayName("Wrapper method provides default implementation")
        void wrapper_method_provides_default_implementation() {
            final boolean[] businessMethodCalled = new boolean[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessMethodCalled[0] = true;
                    return TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Wrapper method calls business method by default")
        void wrapper_method_calls_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessMethodCalled[0] = true;
                    return TaskStatistics.builder()
                            .totalTasks(5)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Wrapper method wraps result in ApiResponse")
        void wrapper_method_wraps_in_api_response() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(8)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Overriding wrapper method bypasses default business method call")
        void override_wrapper_method_bypasses_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessMethodCalled[0] = true;
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
                            .totalTasks(99)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                    return ApiResponse.ok(stats);
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isFalse();
        }

        @Test
        @DisplayName("Business method can be reused in custom wrapper")
        void business_method_reuse_in_custom_wrapper() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    Map<String, Integer> status = new HashMap<>();
                    status.put("pending", 5);
                    return TaskStatistics.builder()
                            .totalTasks(5)
                            .byStatus(status)
                            .byPriority(new HashMap<>())
                            .build();
                }

                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    TaskStatistics stats = getStatistics();
                    return ApiResponse.ok(stats).header("X-Custom-Header", "value");
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody().getTotalTasks()).isEqualTo(5);
            assertThat(response.getHeaders()).containsEntry("X-Custom-Header", "value");
        }
    }

    @Nested
    @DisplayName("CEF Object Access in Wrapper Method")
    class CefObjectAccessTests {

        @Test
        @DisplayName("handleGetStatistics has access to CEF browser object")
        void handle_get_statistics_has_access_to_browser() {
            final CefBrowser[] capturedBrowser = new CefBrowser[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedBrowser[0] = browser;
                    return ApiResponse.ok(TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build());
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(capturedBrowser[0]).isSameAs(mockBrowser);
        }

        @Test
        @DisplayName("handleGetStatistics has access to CEF frame object")
        void handle_get_statistics_has_access_to_frame() {
            final CefFrame[] capturedFrame = new CefFrame[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedFrame[0] = frame;
                    return ApiResponse.ok(TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build());
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(capturedFrame[0]).isSameAs(mockFrame);
        }

        @Test
        @DisplayName("handleGetStatistics has access to CEF request object")
        void handle_get_statistics_has_access_to_request() {
            final CefRequest[] capturedRequest = new CefRequest[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedRequest[0] = cefRequest;
                    return ApiResponse.ok(TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build());
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);
            assertThat(capturedRequest[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("handleGetStatistics can access all CEF objects simultaneously")
        void handle_get_statistics_can_access_all_cef_objects() {
            final CefBrowser[] capturedBrowser = new CefBrowser[1];
            final CefFrame[] capturedFrame = new CefFrame[1];
            final CefRequest[] capturedRequest = new CefRequest[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedBrowser[0] = browser;
                    capturedFrame[0] = frame;
                    capturedRequest[0] = cefRequest;
                    return ApiResponse.ok(TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build());
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedBrowser[0]).isSameAs(mockBrowser);
            assertThat(capturedFrame[0]).isSameAs(mockFrame);
            assertThat(capturedRequest[0]).isSameAs(mockCefRequest);
        }
    }

    @Nested
    @DisplayName("Statistics Data Variations")
    class StatisticsDataVariationsTests {

        @Test
        @DisplayName("getStatistics returns stats with zero tasks")
        void get_statistics_with_zero_tasks() {
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

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getTotalTasks()).isZero();
        }

        @Test
        @DisplayName("getStatistics returns stats with large task count")
        void get_statistics_with_large_task_count() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(10000)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getTotalTasks()).isEqualTo(10000);
        }

        @Test
        @DisplayName("getStatistics returns stats with all status values")
        void get_statistics_with_all_status_values() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 2);
            statusMap.put("in_progress", 3);
            statusMap.put("blocked", 1);
            statusMap.put("review", 2);
            statusMap.put("completed", 5);
            statusMap.put("cancelled", 1);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(14)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).hasSize(6);
        }

        @Test
        @DisplayName("getStatistics returns stats with all priority values")
        void get_statistics_with_all_priority_values() {
            Map<String, Integer> priorityMap = new HashMap<>();
            priorityMap.put("low", 5);
            priorityMap.put("medium", 8);
            priorityMap.put("high", 4);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(17)
                            .byStatus(new HashMap<>())
                            .byPriority(priorityMap)
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByPriority()).hasSize(3);
        }

        @Test
        @DisplayName("getStatistics returns empty status and priority maps")
        void get_statistics_with_empty_maps() {
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

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).isEmpty();
            assertThat(result.getByPriority()).isEmpty();
        }

        @Test
        @DisplayName("handleGetStatistics preserves different statistics variations")
        void handle_get_statistics_preserves_variations() {
            // First variation: high task count
            Map<String, Integer> statusMap1 = new HashMap<>();
            statusMap1.put("pending", 50);
            TaskStatistics stats1 = TaskStatistics.builder()
                    .totalTasks(100)
                    .byStatus(statusMap1)
                    .byPriority(new HashMap<>())
                    .build();

            StatisticsApiService customService1 = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return stats1;
                }
            };

            ApiResponse<TaskStatistics> response1 = customService1.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);
            assertThat(response1.getBody().getTotalTasks()).isEqualTo(100);

            // Second variation: low task count
            Map<String, Integer> statusMap2 = new HashMap<>();
            statusMap2.put("completed", 5);
            TaskStatistics stats2 = TaskStatistics.builder()
                    .totalTasks(5)
                    .byStatus(statusMap2)
                    .byPriority(new HashMap<>())
                    .build();

            StatisticsApiService customService2 = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return stats2;
                }
            };

            ApiResponse<TaskStatistics> response2 = customService2.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);
            assertThat(response2.getBody().getTotalTasks()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Override Chain and Exception Tests")
    class OverrideChainExceptionTests {

        @Test
        @DisplayName("override business method only - wrapper calls and wraps")
        void override_business_only_wrapper_calls_and_wraps() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    Map<String, Integer> status = new HashMap<>();
                    status.put("pending", 10);
                    return TaskStatistics.builder()
                            .totalTasks(10)
                            .byStatus(status)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody().getTotalTasks()).isEqualTo(10);
        }

        @Test
        @DisplayName("override both methods - wrapper bypass business by default")
        void override_both_methods_wrapper_bypass() {
            final boolean[] businessCalled = new boolean[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessCalled[0] = true;
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
                            .totalTasks(100)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                    return ApiResponse.ok(stats);
                }
            };

            customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCalled[0]).isFalse();
        }

        @Test
        @DisplayName("override both and call business from wrapper")
        void override_both_call_business_from_wrapper() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    Map<String, Integer> status = new HashMap<>();
                    status.put("in_progress", 5);
                    return TaskStatistics.builder()
                            .totalTasks(5)
                            .byStatus(status)
                            .byPriority(new HashMap<>())
                            .build();
                }

                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    TaskStatistics stats = getStatistics();
                    return ApiResponse.ok(stats).header("X-From-Business", "true");
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody().getTotalTasks()).isEqualTo(5);
            assertThat(response.getHeaders()).containsEntry("X-From-Business", "true");
        }

        @Test
        @DisplayName("exception in business method propagates through wrapper")
        void exception_in_business_propagates() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    throw new IllegalStateException("Database connection failed");
                }
            };

            assertThatThrownBy(() ->
                    customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Database connection failed");
        }

        @Test
        @DisplayName("exception in wrapper method is not caught")
        void exception_in_wrapper_not_caught() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    throw new RuntimeException("Wrapper error");
                }
            };

            assertThatThrownBy(() ->
                    customService.handleGetStatistics(mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Wrapper error");
        }

        @Test
        @DisplayName("NotImplementedException when business method not overridden")
        void not_implemented_exception_when_not_overridden() {
            assertThatThrownBy(() -> service.getStatistics())
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("getStatistics not implemented");
        }
    }

    @Nested
    @DisplayName("ApiResponse Status Code Tests")
    class ApiResponseStatusCodeTests {

        @Test
        @DisplayName("handleGetStatistics returns 200 status code by default")
        void handleGetStatistics_default_returns_200() {
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
            assertThat(response.getContentType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("handleGetStatistics can set custom headers")
        void handleGetStatistics_custom_headers() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    TaskStatistics stats = TaskStatistics.builder()
                            .totalTasks(42)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                    return ApiResponse.ok(stats)
                            .header("X-Total-Count", "42")
                            .header("X-Cache-Status", "miss");
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getHeaders())
                    .containsEntry("X-Total-Count", "42")
                    .containsEntry("X-Cache-Status", "miss");
        }
    }

    @Nested
    @DisplayName("Complex Statistics Processing")
    class ComplexStatisticsProcessingTests {

        @Test
        @DisplayName("getStatistics with mixed empty and populated maps")
        void getStatistics_with_mixed_empty_and_populated_maps() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 5);

            Map<String, Integer> priorityMap = new HashMap<>();

            TaskStatistics stats = TaskStatistics.builder()
                    .totalTasks(5)
                    .byStatus(statusMap)
                    .byPriority(priorityMap)
                    .build();

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return stats;
                }
            };

            TaskStatistics result = customService.getStatistics();

            assertThat(result.getByStatus()).hasSize(1);
            assertThat(result.getByPriority()).isEmpty();
        }

        @Test
        @DisplayName("handleGetStatistics preserves map references")
        void handleGetStatistics_preserves_map_references() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("done", 8);

            TaskStatistics stats = TaskStatistics.builder()
                    .totalTasks(8)
                    .byStatus(statusMap)
                    .byPriority(new HashMap<>())
                    .build();

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return stats;
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody().getByStatus()).isSameAs(statusMap);
        }

        @Test
        @DisplayName("getStatistics with large counts")
        void getStatistics_with_large_counts() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 5000);
            statusMap.put("in_progress", 3000);
            statusMap.put("completed", 2000);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(10000)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();

            assertThat(result.getTotalTasks()).isEqualTo(10000);
            assertThat(result.getByStatus().values().stream().mapToInt(Integer::intValue).sum())
                    .isEqualTo(10000);
        }

        @Test
        @DisplayName("getStatistics with single entry maps")
        void getStatistics_with_single_entry_maps() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("completed", 100);

            Map<String, Integer> priorityMap = new HashMap<>();
            priorityMap.put("high", 100);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(100)
                            .byStatus(statusMap)
                            .byPriority(priorityMap)
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();

            assertThat(result.getByStatus()).hasSize(1).containsEntry("completed", 100);
            assertThat(result.getByPriority()).hasSize(1).containsEntry("high", 100);
        }

        @Test
        @DisplayName("multiple getStatistics calls with different data")
        void multiple_getStatistics_calls_with_different_data() {
            final List<Integer> totalCounts = new ArrayList<>();

            StatisticsApiService customService = new StatisticsApiService() {
                private int callCount = 0;

                @Override
                public TaskStatistics getStatistics() {
                    callCount++;
                    int total = callCount * 10;
                    totalCounts.add(total);
                    return TaskStatistics.builder()
                            .totalTasks(total)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            customService.getStatistics();
            customService.getStatistics();
            customService.getStatistics();

            assertThat(totalCounts).hasSize(3).containsExactly(10, 20, 30);
        }
    }

    @Nested
    @DisplayName("Additional Edge Cases and Advanced Scenarios")
    class AdditionalEdgeCasesTests {

        @Test
        @DisplayName("getStatistics with negative counts in maps")
        void getStatistics_with_negative_counts() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", -5);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(-5)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getTotalTasks()).isNegative();
            assertThat(result.getByStatus()).containsEntry("pending", -5);
        }

        @Test
        @DisplayName("getStatistics with Integer.MAX_VALUE")
        void getStatistics_with_max_integer() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", Integer.MAX_VALUE);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(Integer.MAX_VALUE)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getTotalTasks()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("handleGetStatistics with null CEF objects handled gracefully")
        void handleGetStatistics_with_null_cef_objects() {
            final CefBrowser[] capturedBrowser = new CefBrowser[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedBrowser[0] = browser;
                    return ApiResponse.ok(TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build());
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(null, null, null);
            assertThat(capturedBrowser[0]).isNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("getStatistics returns consistent results on multiple calls")
        void getStatistics_consistency_across_calls() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 5);
            statusMap.put("completed", 10);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(15)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result1 = customService.getStatistics();
            TaskStatistics result2 = customService.getStatistics();
            TaskStatistics result3 = customService.getStatistics();

            assertThat(result1.getTotalTasks()).isEqualTo(result2.getTotalTasks());
            assertThat(result2.getTotalTasks()).isEqualTo(result3.getTotalTasks());
        }

        @Test
        @DisplayName("getStatistics with many distinct status entries")
        void getStatistics_with_many_status_entries() {
            Map<String, Integer> statusMap = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                statusMap.put("status-" + i, i);
            }

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(4950)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).hasSize(100);
        }

        @Test
        @DisplayName("handleGetStatistics with many headers")
        void handleGetStatistics_with_many_headers() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    ApiResponse<TaskStatistics> response = ApiResponse.ok(TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build());

                    for (int i = 0; i < 20; i++) {
                        response = response.header("X-Header-" + i, "value-" + i);
                    }
                    return response;
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getHeaders()).hasSizeGreaterThanOrEqualTo(20);
        }

        @Test
        @DisplayName("getStatistics with status map having all same values")
        void getStatistics_status_map_with_same_values() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("status-1", 100);
            statusMap.put("status-2", 100);
            statusMap.put("status-3", 100);
            statusMap.put("status-4", 100);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(400)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            for (Integer count : result.getByStatus().values()) {
                assertThat(count).isEqualTo(100);
            }
        }

        @Test
        @DisplayName("handleGetStatistics response is immutable after creation")
        void handleGetStatistics_response_data_integrity() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 5);

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

            TaskStatistics result1 = customService.getStatistics();
            int initialCount = result1.getTotalTasks();

            TaskStatistics result2 = customService.getStatistics();
            assertThat(result2.getTotalTasks()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("getStatistics with priority map edge cases")
        void getStatistics_priority_map_with_unknown_values() {
            Map<String, Integer> priorityMap = new HashMap<>();
            priorityMap.put("unknown-priority-1", 1);
            priorityMap.put("unknown-priority-2", 2);
            priorityMap.put("low", 3);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(6)
                            .byStatus(new HashMap<>())
                            .byPriority(priorityMap)
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByPriority()).hasSize(3);
        }

        @Test
        @DisplayName("handleGetStatistics with customized response builder")
        void handleGetStatistics_custom_response_building() {
            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public ApiResponse<TaskStatistics> handleGetStatistics(
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    TaskStatistics stats = TaskStatistics.builder()
                            .totalTasks(42)
                            .byStatus(new HashMap<>())
                            .byPriority(new HashMap<>())
                            .build();

                    return ApiResponse.ok(stats)
                            .header("X-Stat-Total", "42")
                            .header("X-Generated", "true");
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody().getTotalTasks()).isEqualTo(42);
            assertThat(response.getHeaders().get("X-Stat-Total")).isEqualTo("42");
        }

        @Test
        @DisplayName("getStatistics called sequentially with stateful implementation")
        void getStatistics_stateful_sequential_calls() {
            StatisticsApiService customService = new StatisticsApiService() {
                private int callCount = 0;

                @Override
                public TaskStatistics getStatistics() {
                    callCount++;
                    return TaskStatistics.builder()
                            .totalTasks(callCount)
                            .byStatus(new HashMap<>())
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
        }

        @Test
        @DisplayName("getStatistics with duplicate status keys (map behavior)")
        void getStatistics_map_duplicate_key_overwrite() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("pending", 5);
            statusMap.put("pending", 10);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(10)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).containsEntry("pending", 10);
            assertThat(result.getByStatus().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("handleGetStatistics delegates and wraps correctly")
        void handleGetStatistics_delegate_wrapper_integrity() {
            final boolean[] businessCalled = new boolean[1];

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    businessCalled[0] = true;
                    Map<String, Integer> status = new HashMap<>();
                    status.put("done", 99);
                    return TaskStatistics.builder()
                            .totalTasks(99)
                            .byStatus(status)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            ApiResponse<TaskStatistics> response = customService.handleGetStatistics(
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCalled[0]).isTrue();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody().getTotalTasks()).isEqualTo(99);
        }

        @Test
        @DisplayName("getStatistics with very specific boundary values")
        void getStatistics_boundary_value_combinations() {
            Map<String, Integer> statusMap = new HashMap<>();
            statusMap.put("zero", 0);
            statusMap.put("one", 1);
            statusMap.put("max", Integer.MAX_VALUE);
            statusMap.put("min", Integer.MIN_VALUE);

            StatisticsApiService customService = new StatisticsApiService() {
                @Override
                public TaskStatistics getStatistics() {
                    return TaskStatistics.builder()
                            .totalTasks(0)
                            .byStatus(statusMap)
                            .byPriority(new HashMap<>())
                            .build();
                }
            };

            TaskStatistics result = customService.getStatistics();
            assertThat(result.getByStatus()).containsEntry("zero", 0);
            assertThat(result.getByStatus()).containsEntry("one", 1);
            assertThat(result.getByStatus()).containsEntry("max", Integer.MAX_VALUE);
        }
    }
}
