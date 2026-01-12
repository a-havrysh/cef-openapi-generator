package com.example.api.interceptor;

import com.example.api.exception.ValidationException;
import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ValidationInterceptor.
 * Target coverage: 100% of all code paths
 *
 * Tests all validation routes and constraints:
 * - DELETE /api/tasks/{taskId} - path parameter validation
 * - GET /api/tasks/{taskId} - path parameter validation
 * - GET /api/tasks - query parameter validation with enum and range
 * - PUT /api/tasks/{taskId} - path parameter validation
 * - PATCH /api/tasks/{taskId}/status - path parameter validation
 */
@DisplayName("ValidationInterceptor Tests")
class ValidationInterceptorTest {

    private ValidationInterceptor interceptor;

    @Mock
    private ApiRequest mockRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptor = new ValidationInterceptor();
    }

    @Nested
    @DisplayName("DELETE /api/tasks/{taskId} Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("Valid taskId should pass validation")
        void testValidTaskId() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.DELETE);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("task-123");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Valid taskId with alphanumeric characters")
        void testTaskIdAlphanumeric() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.DELETE);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("TASK_ABC_123");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Valid taskId with special characters")
        void testTaskIdSpecialCharacters() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.DELETE);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("task-@#$-123");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Missing required taskId should throw ValidationException")
        void testMissingTaskId() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.DELETE);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).isNotEmpty();
                        assertThat(vex.getErrors().get(0).getParameter()).isEqualTo("taskId");
                    });
        }

        @Test
        @DisplayName("Empty taskId should throw ValidationException")
        void testEmptyTaskId() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.DELETE);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{taskId} Tests")
    class GetTaskTests {

        @Test
        @DisplayName("Valid taskId should pass validation")
        void testValidTaskId() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("task-456");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Missing required taskId should throw ValidationException")
        void testMissingTaskId() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).hasSize(1);
                    });
        }

        @Test
        @DisplayName("Empty taskId should throw ValidationException")
        void testEmptyTaskId() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("GET /api/tasks (List) Tests")
    class ListTasksTests {

        @Test
        @DisplayName("No query parameters should pass (all optional)")
        void testNoQueryParameters() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Valid status enum value 'pending'")
        void testValidStatusPending() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("pending");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Valid status enum value 'in_progress'")
        void testValidStatusInProgress() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("in_progress");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Valid status enum value 'blocked'")
        void testValidStatusBlocked() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("blocked");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Valid status enum value 'review'")
        void testValidStatusReview() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("review");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Valid status enum value 'completed'")
        void testValidStatusCompleted() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("completed");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Valid status enum value 'cancelled'")
        void testValidStatusCancelled() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("cancelled");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Invalid status enum value should throw ValidationException")
        void testInvalidStatus() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("invalid_status");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            e.getParameter().equals("status") && e.getConstraint().equals("enum")
                        );
                    });
        }

        @Test
        @DisplayName("Page parameter at minimum boundary (1)")
        void testPageMinimum() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("1");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Page parameter at maximum boundary (1000)")
        void testPageMaximum() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("1000");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Page parameter below minimum should throw ValidationException")
        void testPageBelowMinimum() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("0");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            e.getParameter().equals("page") && e.getConstraint().equals("minimum")
                        );
                    });
        }

        @Test
        @DisplayName("Page parameter above maximum should throw ValidationException")
        void testPageAboveMaximum() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("1001");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            e.getParameter().equals("page") && e.getConstraint().equals("maximum")
                        );
                    });
        }

        @Test
        @DisplayName("Invalid page parameter format (not a number)")
        void testPageInvalidFormat() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("not-a-number");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            e.getParameter().equals("page") && e.getConstraint().equals("type")
                        );
                    });
        }

        @Test
        @DisplayName("Size parameter at minimum boundary (1)")
        void testSizeMinimum() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn("1");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Size parameter at maximum boundary (100)")
        void testSizeMaximum() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn("100");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Size parameter below minimum should throw ValidationException")
        void testSizeBelowMinimum() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn("0");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            e.getParameter().equals("size") && e.getConstraint().equals("minimum")
                        );
                    });
        }

        @Test
        @DisplayName("Size parameter above maximum should throw ValidationException")
        void testSizeAboveMaximum() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn("101");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            e.getParameter().equals("size") && e.getConstraint().equals("maximum")
                        );
                    });
        }

        @Test
        @DisplayName("Invalid size parameter format (not a number)")
        void testSizeInvalidFormat() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn("invalid");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            e.getParameter().equals("size") && e.getConstraint().equals("type")
                        );
                    });
        }

        @Test
        @DisplayName("Multiple validation errors should be collected")
        void testMultipleErrors() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("invalid_status");
            when(mockRequest.getQueryParam("page")).thenReturn("0");
            when(mockRequest.getQueryParam("size")).thenReturn("101");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).hasSize(3);
                    });
        }

        @Test
        @DisplayName("All valid parameters together")
        void testAllValidParameters() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("pending");
            when(mockRequest.getQueryParam("page")).thenReturn("5");
            when(mockRequest.getQueryParam("size")).thenReturn("50");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Case-sensitive enum validation")
        void testCaseSensitiveEnum() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("PENDING");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Negative page number should fail validation")
        void testNegativePage() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("-1");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Negative size number should fail validation")
        void testNegativeSize() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn("-1");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Large page numbers should fail validation")
        void testLargePageNumber() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("999999");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("PUT /api/tasks/{taskId} Tests")
    class UpdateTaskTests {

        @Test
        @DisplayName("Valid taskId should pass validation")
        void testValidTaskId() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.PUT);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("task-789");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Missing required taskId should throw ValidationException")
        void testMissingTaskId() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.PUT);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).hasSize(1);
                        assertThat(vex.getErrors().get(0).getParameter()).isEqualTo("taskId");
                    });
        }

        @Test
        @DisplayName("Empty taskId should throw ValidationException")
        void testEmptyTaskId() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.PUT);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Valid taskId with UUID format")
        void testTaskIdUUID() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.PUT);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("550e8400-e29b-41d4-a716-446655440000");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("PATCH /api/tasks/{taskId}/status Tests")
    class UpdateTaskStatusTests {

        @Test
        @DisplayName("Valid taskId should pass validation")
        void testValidTaskId() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.PATCH);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}/status");
            when(mockRequest.getPathVariable("taskId")).thenReturn("task-999");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Missing required taskId should throw ValidationException")
        void testMissingTaskId() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.PATCH);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}/status");
            when(mockRequest.getPathVariable("taskId")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).hasSize(1);
                    });
        }

        @Test
        @DisplayName("Empty taskId should throw ValidationException")
        void testEmptyTaskId() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.PATCH);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}/status");
            when(mockRequest.getPathVariable("taskId")).thenReturn("");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Valid taskId with numeric format")
        void testTaskIdNumeric() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.PATCH);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}/status");
            when(mockRequest.getPathVariable("taskId")).thenReturn("12345");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Unknown Route Tests")
    class UnknownRouteTests {

        @Test
        @DisplayName("Unknown route should not throw exception (no validation)")
        void testUnknownRoute() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.POST);
            when(mockRequest.getPath()).thenReturn("/api/unknown/route");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Route with different HTTP method should not be validated")
        void testDifferentHttpMethod() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.POST);
            when(mockRequest.getPath()).thenReturn("/api/tasks");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Special Characters Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Path parameter with unicode characters")
        void testPathParameterUnicode() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("task-中文-123");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Path parameter with whitespace")
        void testPathParameterWhitespace() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn("task with spaces");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Query parameter with leading/trailing whitespace should fail enum validation")
        void testQueryParameterWhitespace() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("  pending  ");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            e.getParameter().equals("status") && e.getConstraint().equals("enum")
                        );
                    });
        }

        @Test
        @DisplayName("Floating point page number (should fail type validation)")
        void testFloatingPointPage() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("1.5");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Very long taskId should still pass validation")
        void testVeryLongTaskId() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            String longId = "task-" + "a".repeat(1000);
            when(mockRequest.getPathVariable("taskId")).thenReturn(longId);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Integer at exact minimum boundary")
        void testExactMinimumBoundary() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("1");
            when(mockRequest.getQueryParam("size")).thenReturn("1");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Integer at exact maximum boundary")
        void testExactMaximumBoundary() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("1000");
            when(mockRequest.getQueryParam("size")).thenReturn("100");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ValidationException Details Tests")
    class ValidationExceptionDetailsTests {

        @Test
        @DisplayName("ValidationException should contain error details")
        void testExceptionContainsErrorDetails() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("invalid");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).isNotEmpty();
                        assertThat(vex.getErrors().get(0).getParameter()).isEqualTo("status");
                        assertThat(vex.getErrors().get(0).getConstraint()).isNotEmpty();
                        assertThat(vex.getErrors().get(0).getMessage()).isNotEmpty();
                    });
        }

        @Test
        @DisplayName("Single error message should match error message")
        void testSingleErrorMessage() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getMessage()).contains("taskId");
                    });
        }

        @Test
        @DisplayName("Multiple errors message should indicate count")
        void testMultipleErrorsMessage() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("bad");
            when(mockRequest.getQueryParam("page")).thenReturn("bad");
            when(mockRequest.getQueryParam("size")).thenReturn("bad");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).hasSize(3);
                        assertThat(vex.getMessage()).contains("3");
                    });
        }
    }

    @Nested
    @DisplayName("Request Interceptor Interface Tests")
    class InterfaceTests {

        @Test
        @DisplayName("Should implement RequestInterceptor interface")
        void testImplementsRequestInterceptor() {
            assertThat(interceptor).isInstanceOf(RequestInterceptor.class);
        }

        @Test
        @DisplayName("Constructor should initialize without error")
        void testConstructor() {
            ValidationInterceptor newInterceptor = new ValidationInterceptor();
            assertThat(newInterceptor).isNotNull();
        }
    }

    @Nested
    @DisplayName("Advanced Validation Scenarios")
    class AdvancedValidationTests {

        @Test
        @DisplayName("Boundary value: page = 0 should fail (below minimum)")
        void testPageBoundaryZero() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("0");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Boundary value: page = 1 should pass (at minimum)")
        void testPageBoundaryOne() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("1");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Boundary value: size = 0 should fail (below minimum)")
        void testSizeBoundaryZero() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn("0");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Validation should collect all errors from single route")
        void testErrorCollectionSingleRoute() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("invalid");
            when(mockRequest.getQueryParam("page")).thenReturn("invalid");
            when(mockRequest.getQueryParam("size")).thenReturn("invalid");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).hasSize(3);
                    });
        }

        @Test
        @DisplayName("Unknown route should pass validation (no validation metadata)")
        void testUnknownRouteNoValidation() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/unknown");

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("All routes should be independent for validation")
        void testRouteIndependence() throws Exception {
            // DELETE /api/tasks/{taskId} with missing parameter should fail
            when(mockRequest.getMethod()).thenReturn(HttpMethod.DELETE);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class);

            // GET /api/tasks with missing status should pass (optional)
            reset(mockRequest);
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Validation error should include parameter name")
        void testErrorIncludesParameterName() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks/{taskId}");
            when(mockRequest.getPathVariable("taskId")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            "taskId".equals(e.getParameter())
                        );
                    });
        }

        @Test
        @DisplayName("Validation error should include constraint type")
        void testErrorIncludesConstraintType() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("invalid");
            when(mockRequest.getQueryParam("page")).thenReturn(null);
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).anyMatch(e ->
                            "enum".equals(e.getConstraint())
                        );
                    });
        }

        @Test
        @DisplayName("Multiple validation errors should preserve order")
        void testErrorOrderPreservation() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("invalid");
            when(mockRequest.getQueryParam("page")).thenReturn("invalid");
            when(mockRequest.getQueryParam("size")).thenReturn("invalid");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        assertThat(vex.getErrors()).isNotEmpty();
                        // Errors should be for path, then query params
                        assertThat(vex.getErrors().get(0).getParameter()).isEqualTo("status");
                    });
        }

        @Test
        @DisplayName("Validation should handle mixed required and optional parameters")
        void testMixedRequiredAndOptional() throws Exception {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            // status is optional, page is optional, size is optional
            when(mockRequest.getQueryParam("status")).thenReturn(null);
            when(mockRequest.getQueryParam("page")).thenReturn("1");
            when(mockRequest.getQueryParam("size")).thenReturn(null);

            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Enum validation should check all allowed values are rejected when invalid")
        void testEnumValidationComprehensive() {
            String[] invalidStatuses = {"active", "inactive", "PENDING", "pending ", " pending"};

            for (String invalidStatus : invalidStatuses) {
                when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
                when(mockRequest.getPath()).thenReturn("/api/tasks");
                when(mockRequest.getQueryParam("status")).thenReturn(invalidStatus);
                when(mockRequest.getQueryParam("page")).thenReturn(null);
                when(mockRequest.getQueryParam("size")).thenReturn(null);

                assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                        .isInstanceOf(ValidationException.class)
                        .as("Should reject status value: " + invalidStatus);

                reset(mockRequest);
            }
        }

        @Test
        @DisplayName("Numeric boundary tests for page parameter")
        void testPageNumericBoundaries() {
            // Test several invalid values
            int[] invalidPages = {-1, 0, 1001, 1002, 99999};

            for (int invalidPage : invalidPages) {
                when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
                when(mockRequest.getPath()).thenReturn("/api/tasks");
                when(mockRequest.getQueryParam("status")).thenReturn(null);
                when(mockRequest.getQueryParam("page")).thenReturn(String.valueOf(invalidPage));
                when(mockRequest.getQueryParam("size")).thenReturn(null);

                assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                        .isInstanceOf(ValidationException.class)
                        .as("Should reject page value: " + invalidPage);

                reset(mockRequest);
            }
        }

        @Test
        @DisplayName("Numeric boundary tests for size parameter")
        void testSizeNumericBoundaries() {
            // Test several invalid values
            int[] invalidSizes = {-1, 0, 101, 102, 1000};

            for (int invalidSize : invalidSizes) {
                when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
                when(mockRequest.getPath()).thenReturn("/api/tasks");
                when(mockRequest.getQueryParam("status")).thenReturn(null);
                when(mockRequest.getQueryParam("page")).thenReturn(null);
                when(mockRequest.getQueryParam("size")).thenReturn(String.valueOf(invalidSize));

                assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                        .isInstanceOf(ValidationException.class)
                        .as("Should reject size value: " + invalidSize);

                reset(mockRequest);
            }
        }

        @Test
        @DisplayName("Validation exception message should indicate number of errors")
        void testExceptionMessageIndicatesErrorCount() {
            when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
            when(mockRequest.getPath()).thenReturn("/api/tasks");
            when(mockRequest.getQueryParam("status")).thenReturn("bad1");
            when(mockRequest.getQueryParam("page")).thenReturn("bad2");
            when(mockRequest.getQueryParam("size")).thenReturn("bad3");

            assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException vex = (ValidationException) ex;
                        // Should indicate 3 errors in message
                        assertThat(vex.getMessage()).contains("3");
                    });
        }

        @Test
        @DisplayName("Validation should accept all valid enum values")
        void testAllValidEnumValuesAccepted() throws Exception {
            String[] validStatuses = {"pending", "in_progress", "blocked", "review", "completed", "cancelled"};

            for (String status : validStatuses) {
                when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
                when(mockRequest.getPath()).thenReturn("/api/tasks");
                when(mockRequest.getQueryParam("status")).thenReturn(status);
                when(mockRequest.getQueryParam("page")).thenReturn(null);
                when(mockRequest.getQueryParam("size")).thenReturn(null);

                assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                        .as("Should accept status value: " + status)
                        .doesNotThrowAnyException();

                reset(mockRequest);
            }
        }

        @Test
        @DisplayName("Path variable validation should be enforced for all path operations")
        void testPathVariableValidationAllOperations() {
            // Test all path operations that have taskId parameter
            Object[][] pathOperations = {
                    {HttpMethod.GET, "/api/tasks/{taskId}"},
                    {HttpMethod.DELETE, "/api/tasks/{taskId}"},
                    {HttpMethod.PUT, "/api/tasks/{taskId}"},
                    {HttpMethod.PATCH, "/api/tasks/{taskId}/status"}
            };

            for (Object[] operation : pathOperations) {
                when(mockRequest.getMethod()).thenReturn((HttpMethod) operation[0]);
                when(mockRequest.getPath()).thenReturn((String) operation[1]);
                when(mockRequest.getPathVariable("taskId")).thenReturn(null);

                assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                        .isInstanceOf(ValidationException.class)
                        .as("Should validate taskId for " + operation[0] + " " + operation[1]);

                reset(mockRequest);
            }
        }
    }
}
