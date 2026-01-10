package com.example.api.service;

import com.example.api.dto.*;
import com.example.api.exception.NotImplementedException;
import com.example.api.mock.MockCefFactory;
import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for generated TasksApiService interface.
 * Demonstrates query parameters, path variables, and two-level architecture.
 */
class TasksApiServiceTest {

    @Test
    void testDefaultBusinessMethodThrowsNotImplemented() {
        TasksApiService service = new TasksApiService() {};

        // Business method throws NotImplementedException by default
        assertThrows(NotImplementedException.class, () -> {
            service.listTasks("PENDING", 0, 20);
        });
    }

    @Test
    void testQueryParametersExtraction() {
        // Given: Service with overridden business method
        TasksApiService service = new TasksApiService() {
            @Override
            public TaskListResponse listTasks(String status, Integer page, Integer size) {
                // Verify parameters are extracted correctly
                assertEquals("PENDING", status);
                assertEquals(1, page);
                assertEquals(20, size);

                return TaskListResponse.builder()
                    .tasks(java.util.Collections.emptyList())
                    .total(0)
                    .page(page != null ? page : 0)
                    .pageSize(size != null ? size : 20)
                    .build();
            }
        };

        // When: Call wrapper with request containing query params
        ApiRequest request = new ApiRequest(
            MockCefFactory.builder()
                .url("http://localhost/api/tasks?status=PENDING&page=1&size=20")
                .method("GET")
                .build(),
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        String status = request.getQueryParam("status");
        Integer page = request.getQueryParam("page") != null ? Integer.parseInt(request.getQueryParam("page")) : null;
        Integer size = request.getQueryParam("size") != null ? Integer.parseInt(request.getQueryParam("size")) : null;
        ApiResponse<TaskListResponse> response = service.handleListTasks(status, page, size, request.getCefBrowser(), request.getCefFrame(), request.getCefRequest());

        // Then: Response contains data
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testPathVariableExtraction() {
        // Given: Service with overridden business method
        TasksApiService service = new TasksApiService() {
            @Override
            public Task getTask(String taskId) {
                assertEquals("task-123", taskId);

                return Task.builder()
                    .id(taskId)
                    .title("Test Task")
                    .status(TaskStatus.PENDING)
                    .createdAt("2026-01-10T12:00:00")
                    .build();
            }
        };

        // When: Call wrapper with path variable
        ApiRequest request = new ApiRequest(
            MockCefFactory.createMockRequest("http://localhost/api/tasks/task-123", "GET"),
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // Set path variable (normally done by RouteTree)
        java.util.Map<String, String> pathVars = new java.util.HashMap<>();
        pathVars.put("taskId", "task-123");
        request.setPathVariables(pathVars);

        String taskId = request.getPathVariable("taskId");
        ApiResponse<Task> response = service.handleGetTask(taskId, request.getCefBrowser(), request.getCefFrame(), request.getCefRequest());

        // Then: Task returned
        assertNotNull(response);
        assertEquals("task-123", response.getBody().getId());
    }

    @Test
    void testRequestBodyExtraction() {
        // Given: Service with overridden business method
        TasksApiService service = new TasksApiService() {
            @Override
            public Task updateTask(String taskId, UpdateTaskRequest updateRequest) {
                assertEquals("task-456", taskId);
                assertNotNull(updateRequest);

                return Task.builder()
                    .id(taskId)
                    .title(updateRequest.getTitle())
                    .status(TaskStatus.IN_PROGRESS)
                    .createdAt("2026-01-10T12:00:00")
                    .build();
            }
        };

        // When: Call wrapper with body
        String jsonBody = "{\"title\":\"Updated Title\",\"status\":\"IN_PROGRESS\"}";
        ApiRequest request = new ApiRequest(
            MockCefFactory.createMockRequestWithBody(
                "http://localhost/api/tasks/task-456",
                "PUT",
                jsonBody
            ),
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        java.util.Map<String, String> pathVars = new java.util.HashMap<>();
        pathVars.put("taskId", "task-456");
        request.setPathVariables(pathVars);

        String taskId = request.getPathVariable("taskId");
        UpdateTaskRequest updateTaskRequest = request.getBody(UpdateTaskRequest.class);
        ApiResponse<Task> response = service.handleUpdateTask(taskId, updateTaskRequest, request.getCefBrowser(), request.getCefFrame(), request.getCefRequest());

        // Then: Task updated
        assertNotNull(response);
        assertEquals("Updated Title", response.getBody().getTitle());
    }

    @Test
    void testWrapperCallsBusinessMethod() {
        // Given: Service with mocked business method
        final boolean[] businessMethodCalled = {false};

        TasksApiService service = new TasksApiService() {
            @Override
            public Task getTask(String taskId) {
                businessMethodCalled[0] = true;
                return Task.builder()
                    .id(taskId)
                    .title("Test")
                    .status(TaskStatus.PENDING)
                    .createdAt("2026-01-10")
                    .build();
            }
        };

        // When: Call wrapper
        ApiRequest request = new ApiRequest(
            MockCefFactory.createMockRequest("http://localhost/api/tasks/123", "GET"),
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        Map<String, String> pathVars = new HashMap<>();
        pathVars.put("taskId", "123");
        request.setPathVariables(pathVars);

        String taskId = request.getPathVariable("taskId");
        service.handleGetTask(taskId, request.getCefBrowser(), request.getCefFrame(), request.getCefRequest());

        // Then: Business method was called
        assertTrue(businessMethodCalled[0]);
    }

    @Test
    void testNullQueryParameters() {
        // Given: Service that handles null params
        TasksApiService service = new TasksApiService() {
            @Override
            public TaskListResponse listTasks(String status, Integer page, Integer size) {
                // All params should be null
                assertNull(status);
                assertNull(page);
                assertNull(size);

                return TaskListResponse.builder()
                    .tasks(java.util.Collections.emptyList())
                    .total(0)
                    .page(0)
                    .pageSize(20)
                    .build();
            }
        };

        // When: Request without query params
        ApiRequest request = new ApiRequest(
            MockCefFactory.createMockRequest("http://localhost/api/tasks", "GET"),
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        String status = request.getQueryParam("status");
        Integer page = request.getQueryParam("page") != null ? Integer.parseInt(request.getQueryParam("page")) : null;
        Integer size = request.getQueryParam("size") != null ? Integer.parseInt(request.getQueryParam("size")) : null;
        ApiResponse<TaskListResponse> response = service.handleListTasks(status, page, size, request.getCefBrowser(), request.getCefFrame(), request.getCefRequest());

        // Then: Handles null params gracefully
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }
}
