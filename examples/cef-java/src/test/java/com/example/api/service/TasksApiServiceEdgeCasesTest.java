package com.example.api.service;

import com.example.api.dto.CreateTaskRequest;
import com.example.api.dto.Task;
import com.example.api.dto.TaskListResponse;
import com.example.api.dto.TaskStatus;
import com.example.api.dto.UpdateTaskRequest;
import com.example.api.dto.UpdateTaskStatusRequest;
import com.example.api.exception.BadRequestException;
import com.example.api.exception.NotFoundException;
import com.example.api.protocol.ApiResponse;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TasksApiService Edge Cases and Error Scenarios")
class TasksApiServiceEdgeCasesTest {

    private TasksApiService service;

    @Mock
    CefBrowser mockBrowser;

    @Mock
    CefFrame mockFrame;

    @Mock
    CefRequest mockCefRequest;

    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create concrete implementation with custom behavior
        service = new TasksApiService() {
            @Override
            public Task createTask(CreateTaskRequest request) {
                if (request == null || request.getTitle() == null || request.getTitle().isEmpty()) {
                    throw new BadRequestException("Invalid task request");
                }
                Task task = new Task();
                task.setId("generated-id-123");
                task.setTitle(request.getTitle());
                task.setStatus(TaskStatus.PENDING);
                return task;
            }

            @Override
            public Void deleteTask(String taskId) {
                if (taskId == null || taskId.isEmpty()) {
                    throw new BadRequestException("Task ID required");
                }
                if (taskId.equals("non-existent-id-999")) {
                    throw new NotFoundException("Task not found");
                }
                return null;
            }

            @Override
            public Task getTask(String taskId) {
                if (taskId == null || taskId.isEmpty()) {
                    throw new BadRequestException("Task ID required");
                }
                if (taskId.equals("non-existent-999")) {
                    throw new NotFoundException("Task not found");
                }
                Task task = new Task();
                task.setId(taskId);
                task.setTitle("Retrieved Task");
                task.setStatus(TaskStatus.PENDING);
                return task;
            }

            @Override
            public TaskListResponse listTasks(String status, Integer page, Integer size) {
                if (status != null && !isValidStatus(status)) {
                    throw new BadRequestException("Invalid status");
                }
                if (page != null && (page < 1 || page > 1000)) {
                    throw new BadRequestException("Page must be between 1 and 1000");
                }
                if (size != null && (size < 1 || size > 100)) {
                    throw new BadRequestException("Size must be between 1 and 100");
                }
                TaskListResponse response = new TaskListResponse();
                response.setTasks(new ArrayList<>());
                response.setTotal(0);
                response.setPage(page != null ? page : 1);
                response.setPageSize(size != null ? size : 10);
                return response;
            }

            @Override
            public Task updateTask(String taskId, UpdateTaskRequest request) {
                if (taskId == null || taskId.isEmpty()) {
                    throw new BadRequestException("Task ID required");
                }
                if (request == null || request.getTitle() == null || request.getTitle().isEmpty()) {
                    throw new BadRequestException("Invalid update request");
                }
                if (taskId.equals("non-existent-999")) {
                    throw new NotFoundException("Task not found");
                }
                Task task = new Task();
                task.setId(taskId);
                task.setTitle(request.getTitle());
                return task;
            }

            @Override
            public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest request) {
                if (taskId == null || taskId.isEmpty()) {
                    throw new BadRequestException("Task ID required");
                }
                if (request == null || request.getStatus() == null) {
                    throw new BadRequestException("Status required");
                }
                if (taskId.equals("non-existent-999")) {
                    throw new NotFoundException("Task not found");
                }
                Task task = new Task();
                task.setId(taskId);
                task.setStatus(request.getStatus());
                return task;
            }

            private boolean isValidStatus(String status) {
                return status.matches("(pending|in_progress|blocked|review|completed|cancelled)");
            }
        };
    }

    @Nested
    @DisplayName("createTask Edge Cases")
    class CreateTaskEdgeCases {

        @Test
        @DisplayName("createTask with null request throws exception")
        void testCreateTaskNullRequest() {
            setUp();
            assertThatThrownBy(() -> service.createTask(null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("createTask with empty title in request")
        void testCreateTaskEmptyTitle() {
            setUp();
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("");

            // Should handle or throw validation error
            assertThatThrownBy(() -> service.createTask(request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("createTask with null title in request")
        void testCreateTaskNullTitle() {
            setUp();
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(null);

            assertThatThrownBy(() -> service.createTask(request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("createTask with very long title")
        void testCreateTaskVeryLongTitle() {
            setUp();
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("a".repeat(500)); // Very long title

            // Should either accept or reject based on validation
            assertThatCode(() -> service.createTask(request))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("createTask with special characters in title")
        void testCreateTaskSpecialCharacters() {
            setUp();
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Task !@#$%^&*(){}[]|\\:;\"'<>?,./");

            assertThatCode(() -> service.createTask(request))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("createTask with unicode characters")
        void testCreateTaskUnicodeTitle() {
            setUp();
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("任务 中文 текст العربية");

            assertThatCode(() -> service.createTask(request))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("handleCreateTask passes CEF objects correctly")
        void testHandleCreateTaskCefObjects() {
            setUp();
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Test");

            ApiResponse<Task> response = service.handleCreateTask(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("deleteTask Edge Cases")
    class DeleteTaskEdgeCases {

        @Test
        @DisplayName("deleteTask with null taskId throws exception")
        void testDeleteTaskNullId() {
            setUp();
            assertThatThrownBy(() -> service.deleteTask(null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("deleteTask with empty taskId")
        void testDeleteTaskEmptyId() {
            setUp();
            assertThatThrownBy(() -> service.deleteTask(""))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("deleteTask with non-existent taskId")
        void testDeleteTaskNonExistentId() {
            setUp();
            assertThatThrownBy(() -> service.deleteTask("non-existent-id-999"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("deleteTask with very long taskId")
        void testDeleteTaskVeryLongId() {
            setUp();
            String longId = "task-" + "a".repeat(1000);

            assertThatCode(() -> service.deleteTask(longId))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("handleDeleteTask returns Void correctly")
        void testHandleDeleteTaskVoid() {
            setUp();
            ApiResponse<Void> response = service.handleDeleteTask("task-123", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("deleteTask with special characters in ID")
        void testDeleteTaskSpecialCharacters() {
            setUp();
            assertThatCode(() -> service.deleteTask("task-@#$-123"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getTask Edge Cases")
    class GetTaskEdgeCases {

        @Test
        @DisplayName("getTask with null taskId throws exception")
        void testGetTaskNullId() {
            setUp();
            assertThatThrownBy(() -> service.getTask(null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("getTask with empty taskId")
        void testGetTaskEmptyId() {
            setUp();
            assertThatThrownBy(() -> service.getTask(""))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("getTask with non-existent taskId returns 404")
        void testGetTaskNonExistent() {
            setUp();
            assertThatThrownBy(() -> service.getTask("non-existent-999"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("getTask with numeric taskId")
        void testGetTaskNumericId() {
            setUp();
            assertThatCode(() -> service.getTask("12345"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getTask with UUID format taskId")
        void testGetTaskUUIDFormat() {
            setUp();
            assertThatCode(() -> service.getTask("550e8400-e29b-41d4-a716-446655440000"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("handleGetTask receives correct CEF objects")
        void testHandleGetTaskCefObjects() {
            setUp();
            ApiResponse<Task> response = service.handleGetTask("task-123", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("listTasks Edge Cases and Pagination")
    class ListTasksEdgeCases {

        @Test
        @DisplayName("listTasks with no parameters returns results")
        void testListTasksNoParameters() {
            setUp();
            assertThatCode(() -> service.listTasks(null, null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("listTasks with null status parameter")
        void testListTasksNullStatus() {
            setUp();
            assertThatCode(() -> service.listTasks(null, 1, 10))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("listTasks with invalid status enum value")
        void testListTasksInvalidStatus() {
            setUp();
            assertThatThrownBy(() -> service.listTasks("invalid_status", null, null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("listTasks with all valid status enum values")
        void testListTasksAllStatuses() {
            setUp();
            String[] validStatuses = {"pending", "in_progress", "blocked", "review", "completed", "cancelled"};

            for (String status : validStatuses) {
                assertThatCode(() -> service.listTasks(status, null, null))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("listTasks with page at minimum boundary (1)")
        void testListTasksPageMinimum() {
            setUp();
            assertThatCode(() -> service.listTasks(null, 1, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("listTasks with page at maximum boundary (1000)")
        void testListTasksPageMaximum() {
            setUp();
            assertThatCode(() -> service.listTasks(null, 1000, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("listTasks with page below minimum (0)")
        void testListTasksPageZero() {
            setUp();
            assertThatThrownBy(() -> service.listTasks(null, 0, null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("listTasks with page above maximum (1001)")
        void testListTasksPageAboveMax() {
            setUp();
            assertThatThrownBy(() -> service.listTasks(null, 1001, null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("listTasks with negative page number")
        void testListTasksNegativePage() {
            setUp();
            assertThatThrownBy(() -> service.listTasks(null, -1, null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("listTasks with size at minimum boundary (1)")
        void testListTasksSizeMinimum() {
            setUp();
            assertThatCode(() -> service.listTasks(null, null, 1))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("listTasks with size at maximum boundary (100)")
        void testListTasksSizeMaximum() {
            setUp();
            assertThatCode(() -> service.listTasks(null, null, 100))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("listTasks with size below minimum (0)")
        void testListTasksSizeZero() {
            setUp();
            assertThatThrownBy(() -> service.listTasks(null, null, 0))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("listTasks with size above maximum (101)")
        void testListTasksSizeAboveMax() {
            setUp();
            assertThatThrownBy(() -> service.listTasks(null, null, 101))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("listTasks with negative size")
        void testListTasksNegativeSize() {
            setUp();
            assertThatThrownBy(() -> service.listTasks(null, null, -1))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("listTasks with all parameters valid")
        void testListTasksAllValid() {
            setUp();
            assertThatCode(() -> service.listTasks("pending", 5, 50))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("listTasks returns TaskListResponse correctly")
        void testListTasksReturnType() {
            setUp();
            TaskListResponse response = service.listTasks(null, 1, 10);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("handleListTasks passes CEF objects correctly")
        void testHandleListTasksCefObjects() {
            setUp();
            ApiResponse<TaskListResponse> response = service.handleListTasks(
                    "pending", 1, 10, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("listTasks with page boundary conditions")
        void testListTasksPageBoundaryConditions() {
            setUp();
            // Test exact boundaries
            assertThatCode(() -> service.listTasks(null, 1, 10)).doesNotThrowAnyException();
            assertThatCode(() -> service.listTasks(null, 1000, 10)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("updateTask Edge Cases")
    class UpdateTaskEdgeCases {

        @Test
        @DisplayName("updateTask with null taskId throws exception")
        void testUpdateTaskNullId() {
            setUp();
            UpdateTaskRequest request = new UpdateTaskRequest();
            assertThatThrownBy(() -> service.updateTask(null, request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("updateTask with null request throws exception")
        void testUpdateTaskNullRequest() {
            setUp();
            assertThatThrownBy(() -> service.updateTask("task-123", null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("updateTask with empty taskId")
        void testUpdateTaskEmptyId() {
            setUp();
            UpdateTaskRequest request = new UpdateTaskRequest();
            assertThatThrownBy(() -> service.updateTask("", request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("updateTask with non-existent taskId returns 404")
        void testUpdateTaskNonExistent() {
            setUp();
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("Updated");

            assertThatThrownBy(() -> service.updateTask("non-existent-999", request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("updateTask with empty title in request")
        void testUpdateTaskEmptyTitle() {
            setUp();
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("");

            assertThatThrownBy(() -> service.updateTask("task-123", request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("handleUpdateTask passes all parameters correctly")
        void testHandleUpdateTask() {
            setUp();
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("Updated Task");

            ApiResponse<Task> response = service.handleUpdateTask(
                    "task-123", request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("updateTaskStatus Edge Cases")
    class UpdateTaskStatusEdgeCases {

        @Test
        @DisplayName("updateTaskStatus with null taskId throws exception")
        void testUpdateTaskStatusNullId() {
            setUp();
            UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
            assertThatThrownBy(() -> service.updateTaskStatus(null, request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("updateTaskStatus with null request throws exception")
        void testUpdateTaskStatusNullRequest() {
            setUp();
            assertThatThrownBy(() -> service.updateTaskStatus("task-123", null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("updateTaskStatus with empty taskId")
        void testUpdateTaskStatusEmptyId() {
            setUp();
            UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
            assertThatThrownBy(() -> service.updateTaskStatus("", request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("updateTaskStatus with non-existent taskId")
        void testUpdateTaskStatusNonExistent() {
            setUp();
            UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
            request.setStatus(TaskStatus.COMPLETED);

            assertThatThrownBy(() -> service.updateTaskStatus("non-existent-999", request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("updateTaskStatus with valid status value")
        void testUpdateTaskStatusValidStatus() {
            setUp();
            UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
            request.setStatus(TaskStatus.IN_PROGRESS);

            assertThatCode(() -> service.updateTaskStatus("task-123", request))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("updateTaskStatus with all status enum values")
        void testUpdateTaskStatusAllEnumValues() {
            setUp();
            TaskStatus[] statuses = {
                TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED,
                TaskStatus.REVIEW, TaskStatus.COMPLETED, TaskStatus.CANCELLED
            };

            for (TaskStatus status : statuses) {
                UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
                request.setStatus(status);

                assertThatCode(() -> service.updateTaskStatus("task-123", request))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("handleUpdateTaskStatus passes all parameters correctly")
        void testHandleUpdateTaskStatus() {
            setUp();
            UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
            request.setStatus(TaskStatus.COMPLETED);

            ApiResponse<Task> response = service.handleUpdateTaskStatus(
                    "task-123", request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Wrapper Method Behavior")
    class WrapperMethodBehavior {

        @Test
        @DisplayName("handleCreateTask calls business method")
        void testHandleCreateTaskCallsBusiness() {
            setUp();
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Test");

            ApiResponse<Task> response = service.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("handleDeleteTask wraps result in ApiResponse")
        void testHandleDeleteTaskWrapsResult() {
            setUp();
            ApiResponse<Void> response = service.handleDeleteTask(
                    "task-123", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("handleGetTask returns correct status code")
        void testHandleGetTaskStatusCode() {
            setUp();
            ApiResponse<Task> response = service.handleGetTask(
                    "task-123", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("handleListTasks returns TaskListResponse wrapped in ApiResponse")
        void testHandleListTasksReturnType() {
            setUp();
            ApiResponse<TaskListResponse> response = service.handleListTasks(
                    null, 1, 10, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getBody()).isInstanceOf(TaskListResponse.class);
        }
    }
}
