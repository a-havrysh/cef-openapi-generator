package com.example.api.service;

import com.example.api.dto.CreateTaskRequest;
import com.example.api.dto.Task;
import com.example.api.dto.TaskListResponse;
import com.example.api.dto.TaskStatus;
import com.example.api.dto.TaskPriority;
import com.example.api.dto.UpdateTaskRequest;
import com.example.api.dto.UpdateTaskStatusRequest;
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
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TasksApiService Interface")
class TasksApiServiceTest {

    private TasksApiService service;

    @Mock
    private CefBrowser mockBrowser;

    @Mock
    private CefFrame mockFrame;

    @Mock
    private CefRequest mockCefRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TasksApiService() {};
    }

    @Nested
    @DisplayName("Task Creation Operations")
    class TaskCreationTests {

        @Test
        @DisplayName("createTask throws NotImplementedException by default")
        void createTask_default_throws_not_implemented() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .description("Description")
                    .build();

            assertThatThrownBy(() -> service.createTask(request))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("createTask not implemented");
        }

        @Test
        @DisplayName("createTask can be overridden with custom implementation")
        void createTask_can_be_overridden() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    return Task.builder()
                            .id("task-123")
                            .title(createTaskRequest.getTitle())
                            .description(createTaskRequest.getDescription())
                            .status(TaskStatus.PENDING)
                            .build();
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("New Task")
                    .description("Task Description")
                    .build();

            Task result = customService.createTask(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("task-123");
            assertThat(result.getTitle()).isEqualTo("New Task");
            assertThat(result.getDescription()).isEqualTo("Task Description");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        }

        @Test
        @DisplayName("handleCreateTask delegates to createTask method")
        void handleCreateTask_delegates_to_create_task() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    return Task.builder()
                            .id("task-456")
                            .title(createTaskRequest.getTitle())
                            .build();
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Delegated Task")
                    .build();

            ApiResponse<Task> response = customService.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo("task-456");
            assertThat(response.getBody().getTitle()).isEqualTo("Delegated Task");
        }

        @Test
        @DisplayName("handleCreateTask wraps result in ApiResponse.ok()")
        void handleCreateTask_wraps_in_api_response() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    return Task.builder()
                            .id("task-789")
                            .title("Task")
                            .build();
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Task")
                    .build();

            ApiResponse<Task> response = customService.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("handleCreateTask can be overridden for custom ApiResponse")
        void handleCreateTask_can_be_overridden_for_custom_response() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleCreateTask(
                        CreateTaskRequest createTaskRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    Task task = Task.builder()
                            .id("custom-201")
                            .title(createTaskRequest.getTitle())
                            .build();
                    return ApiResponse.created(task);
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Created Task")
                    .build();

            ApiResponse<Task> response = customService.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(201);
            assertThat(response.getBody().getId()).isEqualTo("custom-201");
        }

        @Test
        @DisplayName("handleCreateTask has access to CEF objects")
        void handleCreateTask_has_access_to_cef_objects() {
            final CefBrowser[] capturedBrowser = new CefBrowser[1];
            final CefFrame[] capturedFrame = new CefFrame[1];
            final CefRequest[] capturedRequest = new CefRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleCreateTask(
                        CreateTaskRequest createTaskRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedBrowser[0] = browser;
                    capturedFrame[0] = frame;
                    capturedRequest[0] = cefRequest;
                    return ApiResponse.ok(Task.builder().id("test").build());
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test")
                    .build();

            customService.handleCreateTask(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedBrowser[0]).isSameAs(mockBrowser);
            assertThat(capturedFrame[0]).isSameAs(mockFrame);
            assertThat(capturedRequest[0]).isSameAs(mockCefRequest);
        }
    }

    @Nested
    @DisplayName("Task Deletion Operations")
    class TaskDeletionTests {

        @Test
        @DisplayName("deleteTask throws NotImplementedException by default")
        void deleteTask_default_throws_not_implemented() {
            assertThatThrownBy(() -> service.deleteTask("task-123"))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("deleteTask not implemented");
        }

        @Test
        @DisplayName("deleteTask can be overridden to return null (Void)")
        void deleteTask_can_be_overridden() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Void deleteTask(String taskId) {
                    return null;
                }
            };

            Void result = customService.deleteTask("task-id");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("handleDeleteTask delegates to deleteTask")
        void handleDeleteTask_delegates_to_delete_task() {
            TasksApiService customService = new TasksApiService() {
                private String capturedTaskId;

                @Override
                public Void deleteTask(String taskId) {
                    this.capturedTaskId = taskId;
                    return null;
                }

                public String getCapturedTaskId() {
                    return capturedTaskId;
                }
            };

            customService.handleDeleteTask("task-456", mockBrowser, mockFrame, mockCefRequest);
        }

        @Test
        @DisplayName("handleDeleteTask passes taskId correctly to business method")
        void handleDeleteTask_passes_task_id_correctly() {
            final String[] capturedId = new String[1];
            TasksApiService customService = new TasksApiService() {
                @Override
                public Void deleteTask(String taskId) {
                    capturedId[0] = taskId;
                    return null;
                }
            };

            customService.handleDeleteTask("my-task-id", mockBrowser, mockFrame, mockCefRequest);
            assertThat(capturedId[0]).isEqualTo("my-task-id");
        }

        @Test
        @DisplayName("handleDeleteTask wraps result in ApiResponse.ok(null)")
        void handleDeleteTask_wraps_in_api_response_with_null() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Void deleteTask(String taskId) {
                    return null;
                }
            };

            ApiResponse<Void> response = customService.handleDeleteTask(
                    "task-123", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("handleDeleteTask can be overridden for custom response")
        void handleDeleteTask_can_be_overridden() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Void> handleDeleteTask(
                        String taskId, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.noContent();
                }
            };

            ApiResponse<Void> response = customService.handleDeleteTask(
                    "task-123", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(204);
        }
    }

    @Nested
    @DisplayName("Task Retrieval Operations")
    class TaskRetrievalTests {

        @Test
        @DisplayName("getTask throws NotImplementedException by default")
        void getTask_default_throws_not_implemented() {
            assertThatThrownBy(() -> service.getTask("task-123"))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("getTask not implemented");
        }

        @Test
        @DisplayName("getTask can be overridden to return Task")
        void getTask_can_be_overridden() {
            Task expectedTask = Task.builder()
                    .id("task-123")
                    .title("My Task")
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task getTask(String taskId) {
                    return expectedTask;
                }
            };

            Task result = customService.getTask("task-123");
            assertThat(result).isSameAs(expectedTask);
        }

        @Test
        @DisplayName("getTask receives correct taskId parameter")
        void getTask_receives_correct_parameter() {
            final String[] capturedId = new String[1];
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task getTask(String taskId) {
                    capturedId[0] = taskId;
                    return null;
                }
            };

            customService.getTask("specific-task-id");
            assertThat(capturedId[0]).isEqualTo("specific-task-id");
        }

        @Test
        @DisplayName("handleGetTask delegates to getTask")
        void handleGetTask_delegates_to_get_task() {
            Task expectedTask = Task.builder()
                    .id("task-789")
                    .title("Get Task Test")
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task getTask(String taskId) {
                    return expectedTask;
                }
            };

            ApiResponse<Task> response = customService.handleGetTask(
                    "task-789", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody()).isSameAs(expectedTask);
        }

        @Test
        @DisplayName("handleGetTask wraps result in ApiResponse.ok()")
        void handleGetTask_wraps_in_api_response() {
            Task task = Task.builder()
                    .id("task-abc")
                    .title("Wrapped Task")
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task getTask(String taskId) {
                    return task;
                }
            };

            ApiResponse<Task> response = customService.handleGetTask(
                    "task-abc", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getBody()).isSameAs(task);
        }

        @Test
        @DisplayName("handleGetTask works with various taskIds")
        void handleGetTask_works_with_various_ids() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task getTask(String taskId) {
                    return Task.builder()
                            .id(taskId)
                            .title("Task: " + taskId)
                            .build();
                }
            };

            String[] testIds = {"id-1", "id-2", "id-3"};
            for (String testId : testIds) {
                ApiResponse<Task> response = customService.handleGetTask(
                        testId, mockBrowser, mockFrame, mockCefRequest);
                assertThat(response.getBody().getId()).isEqualTo(testId);
            }
        }
    }

    @Nested
    @DisplayName("Task Listing Operations")
    class TaskListingTests {

        @Test
        @DisplayName("listTasks throws NotImplementedException by default")
        void listTasks_default_throws_not_implemented() {
            assertThatThrownBy(() -> service.listTasks(null, null, null))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("listTasks not implemented");
        }

        @Test
        @DisplayName("listTasks can be overridden with all parameters")
        void listTasks_can_be_overridden_with_parameters() {
            TaskListResponse expectedResponse = TaskListResponse.builder()
                    .tasks(new ArrayList<>())
                    .total(0)
                    .page(1)
                    .pageSize(10)
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    return expectedResponse;
                }
            };

            TaskListResponse result = customService.listTasks("pending", 1, 10);
            assertThat(result).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("listTasks receives correct status parameter")
        void listTasks_receives_status_parameter() {
            final String[] capturedStatus = new String[1];
            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    capturedStatus[0] = status;
                    return TaskListResponse.builder().build();
                }
            };

            customService.listTasks("completed", 1, 10);
            assertThat(capturedStatus[0]).isEqualTo("completed");
        }

        @Test
        @DisplayName("listTasks receives correct page and size parameters")
        void listTasks_receives_pagination_parameters() {
            final Integer[] capturedPage = new Integer[1];
            final Integer[] capturedSize = new Integer[1];
            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    capturedPage[0] = page;
                    capturedSize[0] = size;
                    return TaskListResponse.builder().build();
                }
            };

            customService.listTasks("pending", 5, 20);
            assertThat(capturedPage[0]).isEqualTo(5);
            assertThat(capturedSize[0]).isEqualTo(20);
        }

        @Test
        @DisplayName("listTasks accepts null parameters")
        void listTasks_accepts_null_parameters() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    assertThat(status).isNull();
                    assertThat(page).isNull();
                    assertThat(size).isNull();
                    return TaskListResponse.builder().build();
                }
            };

            TaskListResponse result = customService.listTasks(null, null, null);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("handleListTasks delegates to listTasks")
        void handleListTasks_delegates_to_list_tasks() {
            List<Task> tasks = new ArrayList<>();
            tasks.add(Task.builder().id("task-1").title("Task 1").build());
            TaskListResponse expectedResponse = TaskListResponse.builder()
                    .tasks(tasks)
                    .total(1)
                    .page(1)
                    .pageSize(10)
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    return expectedResponse;
                }
            };

            ApiResponse<TaskListResponse> response = customService.handleListTasks(
                    "pending", 1, 10, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody()).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("handleListTasks wraps result in ApiResponse.ok()")
        void handleListTasks_wraps_in_api_response() {
            TaskListResponse listResponse = TaskListResponse.builder()
                    .tasks(new ArrayList<>())
                    .total(5)
                    .page(1)
                    .pageSize(10)
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    return listResponse;
                }
            };

            ApiResponse<TaskListResponse> response = customService.handleListTasks(
                    null, null, null, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getBody()).isSameAs(listResponse);
        }

        @Test
        @DisplayName("handleListTasks passes all parameters correctly")
        void handleListTasks_passes_parameters_correctly() {
            final String[] capturedStatus = new String[1];
            final Integer[] capturedPage = new Integer[1];
            final Integer[] capturedSize = new Integer[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    capturedStatus[0] = status;
                    capturedPage[0] = page;
                    capturedSize[0] = size;
                    return TaskListResponse.builder().build();
                }
            };

            customService.handleListTasks("in_progress", 3, 25, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedStatus[0]).isEqualTo("in_progress");
            assertThat(capturedPage[0]).isEqualTo(3);
            assertThat(capturedSize[0]).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Task Update Operations")
    class TaskUpdateTests {

        @Test
        @DisplayName("updateTask throws NotImplementedException by default")
        void updateTask_default_throws_not_implemented() {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated")
                    .build();

            assertThatThrownBy(() -> service.updateTask("task-123", request))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("updateTask not implemented");
        }

        @Test
        @DisplayName("updateTask can be overridden to return updated Task")
        void updateTask_can_be_overridden() {
            Task updatedTask = Task.builder()
                    .id("task-123")
                    .title("Updated Title")
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    return updatedTask;
                }
            };

            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated Title")
                    .build();

            Task result = customService.updateTask("task-123", request);
            assertThat(result).isSameAs(updatedTask);
        }

        @Test
        @DisplayName("updateTask receives correct taskId and request")
        void updateTask_receives_correct_parameters() {
            final String[] capturedTaskId = new String[1];
            final UpdateTaskRequest[] capturedRequest = new UpdateTaskRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    capturedTaskId[0] = taskId;
                    capturedRequest[0] = updateTaskRequest;
                    return Task.builder().id(taskId).build();
                }
            };

            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("New Title")
                    .description("New Description")
                    .build();

            customService.updateTask("task-456", request);

            assertThat(capturedTaskId[0]).isEqualTo("task-456");
            assertThat(capturedRequest[0]).isSameAs(request);
        }

        @Test
        @DisplayName("handleUpdateTask delegates to updateTask")
        void handleUpdateTask_delegates_to_update_task() {
            Task updatedTask = Task.builder()
                    .id("task-789")
                    .title("Handled Update")
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    return updatedTask;
                }
            };

            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Handled Update")
                    .build();

            ApiResponse<Task> response = customService.handleUpdateTask(
                    "task-789", request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody()).isSameAs(updatedTask);
        }

        @Test
        @DisplayName("handleUpdateTask wraps result in ApiResponse.ok()")
        void handleUpdateTask_wraps_in_api_response() {
            Task task = Task.builder()
                    .id("task-upd")
                    .title("Updated")
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    return task;
                }
            };

            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated")
                    .build();

            ApiResponse<Task> response = customService.handleUpdateTask(
                    "task-upd", request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getBody()).isSameAs(task);
        }

        @Test
        @DisplayName("handleUpdateTask passes taskId and request correctly")
        void handleUpdateTask_passes_parameters_correctly() {
            final String[] capturedTaskId = new String[1];
            final UpdateTaskRequest[] capturedRequest = new UpdateTaskRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    capturedTaskId[0] = taskId;
                    capturedRequest[0] = updateTaskRequest;
                    return Task.builder().id(taskId).build();
                }
            };

            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("New Title")
                    .build();

            customService.handleUpdateTask("task-param-test", request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedTaskId[0]).isEqualTo("task-param-test");
            assertThat(capturedRequest[0]).isSameAs(request);
        }
    }

    @Nested
    @DisplayName("Task Status Update Operations")
    class TaskStatusUpdateTests {

        @Test
        @DisplayName("updateTaskStatus throws NotImplementedException by default")
        void updateTaskStatus_default_throws_not_implemented() {
            UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                    .status(TaskStatus.COMPLETED)
                    .build();

            assertThatThrownBy(() -> service.updateTaskStatus("task-123", request))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("updateTaskStatus not implemented");
        }

        @Test
        @DisplayName("updateTaskStatus can be overridden to return updated Task")
        void updateTaskStatus_can_be_overridden() {
            Task statusUpdatedTask = Task.builder()
                    .id("task-123")
                    .title("Task")
                    .status(TaskStatus.COMPLETED)
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest updateTaskStatusRequest) {
                    return statusUpdatedTask;
                }
            };

            UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                    .status(TaskStatus.COMPLETED)
                    .build();

            Task result = customService.updateTaskStatus("task-123", request);
            assertThat(result).isSameAs(statusUpdatedTask);
        }

        @Test
        @DisplayName("updateTaskStatus receives correct parameters")
        void updateTaskStatus_receives_correct_parameters() {
            final String[] capturedTaskId = new String[1];
            final UpdateTaskStatusRequest[] capturedRequest = new UpdateTaskStatusRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest updateTaskStatusRequest) {
                    capturedTaskId[0] = taskId;
                    capturedRequest[0] = updateTaskStatusRequest;
                    return Task.builder().id(taskId).build();
                }
            };

            UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                    .status(TaskStatus.REVIEW)
                    .build();

            customService.updateTaskStatus("status-task-id", request);

            assertThat(capturedTaskId[0]).isEqualTo("status-task-id");
            assertThat(capturedRequest[0]).isSameAs(request);
        }

        @Test
        @DisplayName("handleUpdateTaskStatus delegates to updateTaskStatus")
        void handleUpdateTaskStatus_delegates() {
            Task updatedTask = Task.builder()
                    .id("task-st-1")
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest updateTaskStatusRequest) {
                    return updatedTask;
                }
            };

            UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            ApiResponse<Task> response = customService.handleUpdateTaskStatus(
                    "task-st-1", request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody()).isSameAs(updatedTask);
        }

        @Test
        @DisplayName("handleUpdateTaskStatus wraps result in ApiResponse.ok()")
        void handleUpdateTaskStatus_wraps_in_api_response() {
            Task task = Task.builder()
                    .id("task-st-2")
                    .status(TaskStatus.BLOCKED)
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest updateTaskStatusRequest) {
                    return task;
                }
            };

            UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                    .status(TaskStatus.BLOCKED)
                    .build();

            ApiResponse<Task> response = customService.handleUpdateTaskStatus(
                    "task-st-2", request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getBody()).isSameAs(task);
        }

        @Test
        @DisplayName("handleUpdateTaskStatus passes parameters correctly")
        void handleUpdateTaskStatus_passes_parameters_correctly() {
            final String[] capturedTaskId = new String[1];
            final UpdateTaskStatusRequest[] capturedRequest = new UpdateTaskStatusRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest updateTaskStatusRequest) {
                    capturedTaskId[0] = taskId;
                    capturedRequest[0] = updateTaskStatusRequest;
                    return Task.builder().id(taskId).build();
                }
            };

            UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                    .status(TaskStatus.COMPLETED)
                    .build();

            customService.handleUpdateTaskStatus("status-param-test", request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedTaskId[0]).isEqualTo("status-param-test");
            assertThat(capturedRequest[0]).isSameAs(request);
        }
    }

    @Nested
    @DisplayName("Method Interaction Verification")
    class MethodInteractionTests {

        @Test
        @DisplayName("handleCreateTask calls business createTask method")
        void handleCreateTask_calls_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    businessMethodCalled[0] = true;
                    return Task.builder().id("test").build();
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test")
                    .build();

            customService.handleCreateTask(request, mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("handleDeleteTask calls business deleteTask method")
        void handleDeleteTask_calls_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Void deleteTask(String taskId) {
                    businessMethodCalled[0] = true;
                    return null;
                }
            };

            customService.handleDeleteTask("task-id", mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("handleGetTask calls business getTask method")
        void handleGetTask_calls_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task getTask(String taskId) {
                    businessMethodCalled[0] = true;
                    return Task.builder().id("test").build();
                }
            };

            customService.handleGetTask("task-id", mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("handleListTasks calls business listTasks method")
        void handleListTasks_calls_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    businessMethodCalled[0] = true;
                    return TaskListResponse.builder().build();
                }
            };

            customService.handleListTasks(null, null, null, mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("handleUpdateTask calls business updateTask method")
        void handleUpdateTask_calls_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    businessMethodCalled[0] = true;
                    return Task.builder().id("test").build();
                }
            };

            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated")
                    .build();

            customService.handleUpdateTask("task-id", request, mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("handleUpdateTaskStatus calls business updateTaskStatus method")
        void handleUpdateTaskStatus_calls_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest updateTaskStatusRequest) {
                    businessMethodCalled[0] = true;
                    return Task.builder().id("test").build();
                }
            };

            UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                    .status(TaskStatus.COMPLETED)
                    .build();

            customService.handleUpdateTaskStatus("task-id", request, mockBrowser, mockFrame, mockCefRequest);
            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Business methods throw NotImplementedException by default")
        void business_methods_throw_not_implemented_by_default() {
            assertThatThrownBy(() -> service.createTask(CreateTaskRequest.builder().build()))
                    .isInstanceOf(NotImplementedException.class);

            assertThatThrownBy(() -> service.deleteTask("task-id"))
                    .isInstanceOf(NotImplementedException.class);

            assertThatThrownBy(() -> service.getTask("task-id"))
                    .isInstanceOf(NotImplementedException.class);

            assertThatThrownBy(() -> service.listTasks(null, null, null))
                    .isInstanceOf(NotImplementedException.class);

            assertThatThrownBy(() -> service.updateTask("task-id", UpdateTaskRequest.builder().build()))
                    .isInstanceOf(NotImplementedException.class);

            assertThatThrownBy(() -> service.updateTaskStatus("task-id", UpdateTaskStatusRequest.builder().status(TaskStatus.PENDING).build()))
                    .isInstanceOf(NotImplementedException.class);
        }

        @Test
        @DisplayName("ApiResponse wrapping preserves business method result")
        void api_response_wrapping_preserves_result() {
            Task expectedTask = Task.builder()
                    .id("task-id")
                    .title("Title")
                    .description("Description")
                    .status(TaskStatus.PENDING)
                    .priority(TaskPriority.MEDIUM)
                    .build();

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task getTask(String taskId) {
                    return expectedTask;
                }
            };

            ApiResponse<Task> response = customService.handleGetTask(
                    "task-id", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getBody())
                    .isSameAs(expectedTask)
                    .hasFieldOrPropertyWithValue("id", "task-id")
                    .hasFieldOrPropertyWithValue("title", "Title")
                    .hasFieldOrPropertyWithValue("description", "Description")
                    .hasFieldOrPropertyWithValue("status", TaskStatus.PENDING)
                    .hasFieldOrPropertyWithValue("priority", TaskPriority.MEDIUM);
        }
    }

    @Nested
    @DisplayName("ApiResponse Status Code Tests")
    class ApiResponseStatusCodeTests {

        @Test
        @DisplayName("handleCreateTask with ApiResponse.created() returns 201")
        void handleCreateTask_with_created_returns_201() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleCreateTask(
                        CreateTaskRequest createTaskRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    Task task = Task.builder()
                            .id("new-task")
                            .title(createTaskRequest.getTitle())
                            .build();
                    return ApiResponse.created(task);
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("New Task")
                    .build();

            ApiResponse<Task> response = customService.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(201);
            assertThat(response.getBody().getId()).isEqualTo("new-task");
        }

        @Test
        @DisplayName("handleDeleteTask with ApiResponse.noContent() returns 204")
        void handleDeleteTask_with_noContent_returns_204() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Void> handleDeleteTask(
                        String taskId, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.noContent();
                }
            };

            ApiResponse<Void> response = customService.handleDeleteTask(
                    "task-id", mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(204);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("default handleCreateTask returns 200 with ApiResponse.ok()")
        void handleCreateTask_default_returns_200() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    return Task.builder().id("task-1").title("Task").build();
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder().title("Task").build();
            ApiResponse<Task> response = customService.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Override Chain Behavior")
    class OverrideChainTests {

        @Test
        @DisplayName("override business method only - wrapper calls it and wraps response")
        void override_business_only_wrapper_calls_and_wraps() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    return Task.builder()
                            .id("custom-id")
                            .title(createTaskRequest.getTitle())
                            .build();
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder().title("Test Task").build();
            ApiResponse<Task> response = customService.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody().getId()).isEqualTo("custom-id");
            assertThat(response.getBody().getTitle()).isEqualTo("Test Task");
        }

        @Test
        @DisplayName("override both business and wrapper method - business not called from default wrapper")
        void override_both_methods_business_not_called() {
            final boolean[] businessCalled = new boolean[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    businessCalled[0] = true;
                    return Task.builder().id("should-not-be-called").build();
                }

                @Override
                public ApiResponse<Task> handleCreateTask(
                        CreateTaskRequest createTaskRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.created(Task.builder().id("wrapper-override").build());
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder().title("Test").build();
            ApiResponse<Task> response = customService.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCalled[0]).isFalse();
            assertThat(response.getStatusCode()).isEqualTo(201);
            assertThat(response.getBody().getId()).isEqualTo("wrapper-override");
        }

        @Test
        @DisplayName("override both and explicitly call business from wrapper")
        void override_both_and_call_business_from_wrapper() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    return Task.builder()
                            .id("from-business")
                            .title(createTaskRequest.getTitle())
                            .build();
                }

                @Override
                public ApiResponse<Task> handleCreateTask(
                        CreateTaskRequest createTaskRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    Task businessResult = createTask(createTaskRequest);
                    return ApiResponse.created(businessResult).header("X-Created", "true");
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder().title("Test").build();
            ApiResponse<Task> response = customService.handleCreateTask(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(201);
            assertThat(response.getBody().getId()).isEqualTo("from-business");
            assertThat(response.getHeaders()).containsEntry("X-Created", "true");
        }
    }

    @Nested
    @DisplayName("Exception Propagation Tests")
    class ExceptionPropagationTests {

        @Test
        @DisplayName("exception in business method propagates through wrapper")
        void exception_in_business_propagates_through_wrapper() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    throw new IllegalArgumentException("Invalid request");
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder().title("Test").build();

            assertThatThrownBy(() ->
                    customService.handleCreateTask(request, mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid request");
        }

        @Test
        @DisplayName("exception in wrapper method is not caught")
        void exception_in_wrapper_not_caught() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleCreateTask(
                        CreateTaskRequest createTaskRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    throw new RuntimeException("Wrapper error");
                }
            };

            CreateTaskRequest request = CreateTaskRequest.builder().title("Test").build();

            assertThatThrownBy(() ->
                    customService.handleCreateTask(request, mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Wrapper error");
        }

        @Test
        @DisplayName("NotImplementedException thrown when business method not overridden")
        void not_implemented_exception_when_not_overridden() {
            assertThatThrownBy(() -> service.createTask(CreateTaskRequest.builder().build()))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("createTask not implemented");
        }
    }

    @Nested
    @DisplayName("Null Parameter Handling")
    class NullParameterHandlingTests {

        @Test
        @DisplayName("createTask handles null request body")
        void createTask_handles_null_request() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    if (createTaskRequest == null) {
                        return Task.builder().id("from-null").build();
                    }
                    return Task.builder().id("from-request").build();
                }
            };

            Task result = customService.createTask(null);
            assertThat(result.getId()).isEqualTo("from-null");
        }

        @Test
        @DisplayName("updateTask handles null request")
        void updateTask_handles_null_request() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    if (updateTaskRequest == null) {
                        return Task.builder().id(taskId).title("default").build();
                    }
                    return Task.builder().id(taskId).title(updateTaskRequest.getTitle()).build();
                }
            };

            Task result = customService.updateTask("task-123", null);
            assertThat(result.getId()).isEqualTo("task-123");
            assertThat(result.getTitle()).isEqualTo("default");
        }

        @Test
        @DisplayName("listTasks with all parameters null")
        void listTasks_all_parameters_null() {
            final String[] capturedStatus = new String[1];
            final Integer[] capturedPage = new Integer[1];
            final Integer[] capturedSize = new Integer[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    capturedStatus[0] = status;
                    capturedPage[0] = page;
                    capturedSize[0] = size;
                    return TaskListResponse.builder().build();
                }
            };

            customService.listTasks(null, null, null);

            assertThat(capturedStatus[0]).isNull();
            assertThat(capturedPage[0]).isNull();
            assertThat(capturedSize[0]).isNull();
        }

        @Test
        @DisplayName("listTasks with mixed null and provided parameters")
        void listTasks_mixed_null_and_provided() {
            final String[] capturedStatus = new String[1];
            final Integer[] capturedPage = new Integer[1];
            final Integer[] capturedSize = new Integer[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    capturedStatus[0] = status;
                    capturedPage[0] = page;
                    capturedSize[0] = size;
                    return TaskListResponse.builder().build();
                }
            };

            customService.listTasks("pending", null, 10);

            assertThat(capturedStatus[0]).isEqualTo("pending");
            assertThat(capturedPage[0]).isNull();
            assertThat(capturedSize[0]).isEqualTo(10);
        }

        @Test
        @DisplayName("updateTaskStatus with null request")
        void updateTaskStatus_with_null_request() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest updateTaskStatusRequest) {
                    if (updateTaskStatusRequest == null) {
                        return Task.builder().id(taskId).status(TaskStatus.PENDING).build();
                    }
                    return Task.builder().id(taskId).status(updateTaskStatusRequest.getStatus()).build();
                }
            };

            Task result = customService.updateTaskStatus("task-id", null);
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Complex Parameter Combinations")
    class ComplexParameterCombinationsTests {

        @Test
        @DisplayName("listTasks with various parameter combinations")
        void listTasks_various_combinations() {
            final List<Object[]> combinations = new ArrayList<>();

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    combinations.add(new Object[]{status, page, size});
                    return TaskListResponse.builder().build();
                }
            };

            customService.listTasks("pending", 1, 10);
            customService.listTasks("in_progress", 2, 20);
            customService.listTasks("completed", 3, 50);
            customService.listTasks(null, null, null);

            assertThat(combinations).hasSize(4);
            assertThat(combinations.get(0)).containsExactly("pending", 1, 10);
            assertThat(combinations.get(1)).containsExactly("in_progress", 2, 20);
            assertThat(combinations.get(2)).containsExactly("completed", 3, 50);
            assertThat(combinations.get(3)).containsExactly(null, null, null);
        }

        @Test
        @DisplayName("updateTask with various request content")
        void updateTask_various_request_content() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    return Task.builder()
                            .id(taskId)
                            .title(updateTaskRequest.getTitle())
                            .description(updateTaskRequest.getDescription())
                            .build();
                }
            };

            UpdateTaskRequest request1 = UpdateTaskRequest.builder()
                    .title("Title 1")
                    .description("Desc 1")
                    .build();

            UpdateTaskRequest request2 = UpdateTaskRequest.builder()
                    .title("Title 2")
                    .build();

            Task result1 = customService.updateTask("id-1", request1);
            Task result2 = customService.updateTask("id-2", request2);

            assertThat(result1.getTitle()).isEqualTo("Title 1");
            assertThat(result1.getDescription()).isEqualTo("Desc 1");
            assertThat(result2.getTitle()).isEqualTo("Title 2");
        }
    }

    @Nested
    @DisplayName("CEF Object Access in All Methods")
    class CefObjectAccessTests {

        @Test
        @DisplayName("handleCreateTask receives all CEF objects")
        void handleCreateTask_receives_all_cef_objects() {
            final CefBrowser[] browser = new CefBrowser[1];
            final CefFrame[] frame = new CefFrame[1];
            final CefRequest[] request = new CefRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleCreateTask(
                        CreateTaskRequest createTaskRequest,
                        CefBrowser b, CefFrame f, CefRequest r) {
                    browser[0] = b;
                    frame[0] = f;
                    request[0] = r;
                    return ApiResponse.ok(Task.builder().id("test").build());
                }
            };

            customService.handleCreateTask(CreateTaskRequest.builder().build(),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(browser[0]).isSameAs(mockBrowser);
            assertThat(frame[0]).isSameAs(mockFrame);
            assertThat(request[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("handleUpdateTask receives all CEF objects")
        void handleUpdateTask_receives_all_cef_objects() {
            final CefBrowser[] browser = new CefBrowser[1];
            final CefFrame[] frame = new CefFrame[1];
            final CefRequest[] request = new CefRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleUpdateTask(
                        String taskId, UpdateTaskRequest updateTaskRequest,
                        CefBrowser b, CefFrame f, CefRequest r) {
                    browser[0] = b;
                    frame[0] = f;
                    request[0] = r;
                    return ApiResponse.ok(Task.builder().id(taskId).build());
                }
            };

            customService.handleUpdateTask("task-id", UpdateTaskRequest.builder().build(),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(browser[0]).isSameAs(mockBrowser);
            assertThat(frame[0]).isSameAs(mockFrame);
            assertThat(request[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("handleUpdateTaskStatus receives all CEF objects")
        void handleUpdateTaskStatus_receives_all_cef_objects() {
            final CefBrowser[] browser = new CefBrowser[1];
            final CefFrame[] frame = new CefFrame[1];
            final CefRequest[] request = new CefRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleUpdateTaskStatus(
                        String taskId, UpdateTaskStatusRequest updateTaskStatusRequest,
                        CefBrowser b, CefFrame f, CefRequest r) {
                    browser[0] = b;
                    frame[0] = f;
                    request[0] = r;
                    return ApiResponse.ok(Task.builder().id(taskId).build());
                }
            };

            customService.handleUpdateTaskStatus("task-id", UpdateTaskStatusRequest.builder().status(TaskStatus.PENDING).build(),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(browser[0]).isSameAs(mockBrowser);
            assertThat(frame[0]).isSameAs(mockFrame);
            assertThat(request[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("handleListTasks receives all CEF objects")
        void handleListTasks_receives_all_cef_objects() {
            final CefBrowser[] browser = new CefBrowser[1];
            final CefFrame[] frame = new CefFrame[1];
            final CefRequest[] request = new CefRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<TaskListResponse> handleListTasks(
                        String status, Integer page, Integer size,
                        CefBrowser b, CefFrame f, CefRequest r) {
                    browser[0] = b;
                    frame[0] = f;
                    request[0] = r;
                    return ApiResponse.ok(TaskListResponse.builder().build());
                }
            };

            customService.handleListTasks(null, null, null, mockBrowser, mockFrame, mockCefRequest);

            assertThat(browser[0]).isSameAs(mockBrowser);
            assertThat(frame[0]).isSameAs(mockFrame);
            assertThat(request[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("handleDeleteTask receives all CEF objects")
        void handleDeleteTask_receives_all_cef_objects() {
            final CefBrowser[] browser = new CefBrowser[1];
            final CefFrame[] frame = new CefFrame[1];
            final CefRequest[] request = new CefRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Void> handleDeleteTask(
                        String taskId,
                        CefBrowser b, CefFrame f, CefRequest r) {
                    browser[0] = b;
                    frame[0] = f;
                    request[0] = r;
                    return ApiResponse.ok(null);
                }
            };

            customService.handleDeleteTask("task-id", mockBrowser, mockFrame, mockCefRequest);

            assertThat(browser[0]).isSameAs(mockBrowser);
            assertThat(frame[0]).isSameAs(mockFrame);
            assertThat(request[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("handleGetTask receives all CEF objects")
        void handleGetTask_receives_all_cef_objects() {
            final CefBrowser[] browser = new CefBrowser[1];
            final CefFrame[] frame = new CefFrame[1];
            final CefRequest[] request = new CefRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleGetTask(
                        String taskId,
                        CefBrowser b, CefFrame f, CefRequest r) {
                    browser[0] = b;
                    frame[0] = f;
                    request[0] = r;
                    return ApiResponse.ok(Task.builder().id(taskId).build());
                }
            };

            customService.handleGetTask("task-id", mockBrowser, mockFrame, mockCefRequest);

            assertThat(browser[0]).isSameAs(mockBrowser);
            assertThat(frame[0]).isSameAs(mockFrame);
            assertThat(request[0]).isSameAs(mockCefRequest);
        }
    }

    @Nested
    @DisplayName("Additional Edge Cases and Scenarios")
    class AdditionalEdgeCasesTests {

        @Test
        @DisplayName("createTask with all task statuses")
        void createTask_multiple_status_combinations() {
            for (TaskStatus status : TaskStatus.values()) {
                Task expectedTask = Task.builder()
                        .id("task-" + status.name())
                        .status(status)
                        .title("Task with " + status.name())
                        .build();

                TasksApiService customService = new TasksApiService() {
                    @Override
                    public Task createTask(CreateTaskRequest createTaskRequest) {
                        return expectedTask;
                    }
                };

                Task result = customService.createTask(CreateTaskRequest.builder().title("Test").build());
                assertThat(result.getStatus()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("createTask with all task priorities")
        void createTask_multiple_priority_combinations() {
            for (TaskPriority priority : TaskPriority.values()) {
                Task expectedTask = Task.builder()
                        .id("task-" + priority.name())
                        .priority(priority)
                        .title("Task with " + priority.name())
                        .build();

                TasksApiService customService = new TasksApiService() {
                    @Override
                    public Task createTask(CreateTaskRequest createTaskRequest) {
                        return expectedTask;
                    }
                };

                Task result = customService.createTask(CreateTaskRequest.builder().title("Test").build());
                assertThat(result.getPriority()).isEqualTo(priority);
            }
        }

        @Test
        @DisplayName("listTasks with boundary page values")
        void listTasks_boundary_page_values() {
            final Integer[] capturedPage = new Integer[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    capturedPage[0] = page;
                    return TaskListResponse.builder().page(page).build();
                }
            };

            customService.listTasks(null, 1, 10);
            assertThat(capturedPage[0]).isEqualTo(1);

            customService.listTasks(null, 1000, 10);
            assertThat(capturedPage[0]).isEqualTo(1000);

            customService.listTasks(null, 500, 100);
            assertThat(capturedPage[0]).isEqualTo(500);
        }

        @Test
        @DisplayName("listTasks with boundary size values")
        void listTasks_boundary_size_values() {
            final Integer[] capturedSize = new Integer[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    capturedSize[0] = size;
                    return TaskListResponse.builder().pageSize(size).build();
                }
            };

            customService.listTasks(null, 1, 1);
            assertThat(capturedSize[0]).isEqualTo(1);

            customService.listTasks(null, 1, 100);
            assertThat(capturedSize[0]).isEqualTo(100);
        }

        @Test
        @DisplayName("updateTask with null fields in request")
        void updateTask_null_fields_in_request() {
            final UpdateTaskRequest[] capturedRequest = new UpdateTaskRequest[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {
                    capturedRequest[0] = updateTaskRequest;
                    return Task.builder().id(taskId).build();
                }
            };

            UpdateTaskRequest request = UpdateTaskRequest.builder().build();
            customService.updateTask("task-id", request);

            assertThat(capturedRequest[0]).isNotNull();
            assertThat(capturedRequest[0].getTitle()).isNull();
            assertThat(capturedRequest[0].getDescription()).isNull();
        }

        @Test
        @DisplayName("updateTaskStatus with all status transitions")
        void updateTaskStatus_all_status_values() {
            for (TaskStatus status : TaskStatus.values()) {
                final TaskStatus[] capturedStatus = new TaskStatus[1];

                TasksApiService customService = new TasksApiService() {
                    @Override
                    public Task updateTaskStatus(String taskId, UpdateTaskStatusRequest updateTaskStatusRequest) {
                        capturedStatus[0] = updateTaskStatusRequest.getStatus();
                        return Task.builder().id(taskId).status(status).build();
                    }
                };

                UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder().status(status).build();
                customService.updateTaskStatus("task-id", request);

                assertThat(capturedStatus[0]).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("handleCreateTask propagates all response metadata")
        void handleCreateTask_response_metadata_propagation() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public ApiResponse<Task> handleCreateTask(
                        CreateTaskRequest createTaskRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    Task task = Task.builder().id("task-metadata").title("Test").build();
                    return ApiResponse.created(task)
                            .header("X-Request-ID", "req-123")
                            .header("X-Task-ID", "task-metadata");
                }
            };

            ApiResponse<Task> response = customService.handleCreateTask(
                    CreateTaskRequest.builder().title("Test").build(),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(201);
            assertThat(response.getHeaders()).containsEntry("X-Request-ID", "req-123");
            assertThat(response.getHeaders()).containsEntry("X-Task-ID", "task-metadata");
        }

        @Test
        @DisplayName("deleteTask with special characters in ID")
        void deleteTask_special_chars_in_id() {
            final String[] capturedId = new String[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Void deleteTask(String taskId) {
                    capturedId[0] = taskId;
                    return null;
                }
            };

            String specialId = "task-id-!@#$%^&*()";
            customService.deleteTask(specialId);
            assertThat(capturedId[0]).isEqualTo(specialId);
        }

        @Test
        @DisplayName("getTask with very long ID")
        void getTask_very_long_id() {
            final String[] capturedId = new String[1];

            TasksApiService customService = new TasksApiService() {
                @Override
                public Task getTask(String taskId) {
                    capturedId[0] = taskId;
                    return Task.builder().id(taskId).build();
                }
            };

            String longId = "a".repeat(1000);
            Task result = customService.getTask(longId);

            assertThat(capturedId[0]).isEqualTo(longId);
            assertThat(result.getId()).isEqualTo(longId);
        }

        @Test
        @DisplayName("createTask with CreateTaskRequest null check in business method")
        void createTask_null_request_handling() {
            TasksApiService customService = new TasksApiService() {
                @Override
                public Task createTask(CreateTaskRequest createTaskRequest) {
                    if (createTaskRequest == null) {
                        return Task.builder().id("null-request").build();
                    }
                    return Task.builder().id("valid-request").build();
                }
            };

            Task nullResult = customService.createTask(null);
            assertThat(nullResult.getId()).isEqualTo("null-request");

            Task validResult = customService.createTask(CreateTaskRequest.builder().title("Test").build());
            assertThat(validResult.getId()).isEqualTo("valid-request");
        }

        @Test
        @DisplayName("listTasks returns large response with many items")
        void listTasks_large_response() {
            List<Task> largeTasks = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                largeTasks.add(Task.builder().id("task-" + i).title("Task " + i).build());
            }

            TasksApiService customService = new TasksApiService() {
                @Override
                public TaskListResponse listTasks(String status, Integer page, Integer size) {
                    return TaskListResponse.builder()
                            .tasks(largeTasks)
                            .total(100)
                            .page(1)
                            .pageSize(100)
                            .build();
                }
            };

            TaskListResponse result = customService.listTasks(null, 1, 100);
            assertThat(result.getTasks()).hasSize(100);
            assertThat(result.getTotal()).isEqualTo(100);
        }
    }
}
