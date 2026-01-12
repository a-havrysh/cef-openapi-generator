package com.example.api

import com.example.api.dto.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for Kotlin-generated data classes
 * Verifies that generated models have proper Kotlin characteristics
 */
@DisplayName("Kotlin Data Class Generation Tests")
class KotlinDTOComprehensiveTest {

    @Nested
    @DisplayName("Task DTO Tests")
    inner class TaskDTOTests {

        private lateinit var task: Task

        @BeforeEach
        fun setup() {
            task = Task(
                id = "task-1",
                title = "Sample Task",
                description = "This is a sample task",
                status = TaskStatus.PENDING,
                priority = TaskPriority.HIGH,
                assignee = "john.doe",
                tags = listOf("urgent", "backend"),
                createdAt = "2024-01-01T10:00:00Z",
                updatedAt = "2024-01-12T10:00:00Z",
                dueDate = "2024-01-31T23:59:59Z"
            )
        }

        @Test
        @DisplayName("Task data class has all properties")
        fun testTaskProperties() {
            assertEquals("task-1", task.id)
            assertEquals("Sample Task", task.title)
            assertEquals(TaskStatus.PENDING, task.status)
            assertEquals("2024-01-01T10:00:00Z", task.createdAt)
            assertEquals("This is a sample task", task.description)
            assertEquals(TaskPriority.HIGH, task.priority)
            assertEquals("john.doe", task.assignee)
            assertEquals(listOf("urgent", "backend"), task.tags)
        }

        @Test
        @DisplayName("Task copy function works (data class feature)")
        fun testTaskCopy() {
            val copiedTask = task.copy(
                title = "Updated Task",
                status = TaskStatus.IN_PROGRESS
            )

            assertEquals("task-1", copiedTask.id)
            assertEquals("Updated Task", copiedTask.title)
            assertEquals(TaskStatus.IN_PROGRESS, copiedTask.status)
            // Other fields should be copied
            assertEquals(task.createdAt, copiedTask.createdAt)
        }

        @Test
        @DisplayName("Task with all required fields")
        fun testTaskWithAllRequiredFields() {
            val fullTask = Task(
                id = "task-2",
                title = "Full Task",
                description = "Description",
                status = TaskStatus.PENDING,
                priority = TaskPriority.LOW,
                assignee = "assignee",
                tags = emptyList(),
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z",
                dueDate = "2024-01-31T00:00:00Z"
            )

            assertEquals("task-2", fullTask.id)
            assertEquals("Full Task", fullTask.title)
            assertEquals("Description", fullTask.description)
            assertEquals(TaskPriority.LOW, fullTask.priority)
            assertEquals("assignee", fullTask.assignee)
            assertEquals(emptyList(), fullTask.tags)
        }

        @Test
        @DisplayName("Task equals and hashCode work (data class feature)")
        fun testTaskEqualityAndHash() {
            val task1 = Task("1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.LOW, "assignee", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z")
            val task2 = Task("1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.LOW, "assignee", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z")
            val task3 = Task("2", "Title", "Desc", TaskStatus.PENDING, TaskPriority.LOW, "assignee", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z")

            assertEquals(task1, task2)
            assertEquals(task1.hashCode(), task2.hashCode())
            assertTrue(task1 != task3)
        }

        @Test
        @DisplayName("Task toString works (data class feature)")
        fun testTaskToString() {
            val taskString = task.toString()
            assertTrue(taskString.contains("Task"))
            assertTrue(taskString.contains("task-1"))
            assertTrue(taskString.contains("Sample Task"))
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    inner class EnumTests {

        @Test
        @DisplayName("TaskStatus enum has all values")
        fun testTaskStatusEnumValues() {
            val statuses = listOf(
                TaskStatus.PENDING,
                TaskStatus.IN_PROGRESS,
                TaskStatus.BLOCKED,
                TaskStatus.REVIEW,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
            )

            assertEquals(6, statuses.size)
            assertTrue(statuses.all { it is TaskStatus })
        }

        @Test
        @DisplayName("TaskPriority enum has all values")
        fun testTaskPriorityEnumValues() {
            val priorities = listOf(
                TaskPriority.LOW,
                TaskPriority.MEDIUM,
                TaskPriority.HIGH,
                TaskPriority.CRITICAL
            )

            assertEquals(4, priorities.size)
            assertTrue(priorities.all { it is TaskPriority })
        }

        @Test
        @DisplayName("Enums are comparable")
        fun testEnumComparison() {
            assertEquals(TaskStatus.PENDING, TaskStatus.PENDING)
            assertTrue(TaskStatus.PENDING != TaskStatus.COMPLETED)
            assertEquals(TaskPriority.HIGH, TaskPriority.HIGH)
        }
    }

    @Nested
    @DisplayName("CreateTaskRequest DTO Tests")
    inner class CreateTaskRequestTests {

        @Test
        @DisplayName("CreateTaskRequest construction")
        fun testCreateTaskRequest() {
            val request = CreateTaskRequest(
                title = "New Task",
                description = "Task description",
                priority = TaskPriority.MEDIUM,
                assignee = "user1",
                tags = listOf("tag1", "tag2"),
                dueDate = "2024-02-01T00:00:00Z"
            )

            assertEquals("New Task", request.title)
            assertEquals("Task description", request.description)
            assertEquals(TaskPriority.MEDIUM, request.priority)
            assertEquals("user1", request.assignee)
            assertEquals(listOf("tag1", "tag2"), request.tags)
            assertEquals("2024-02-01T00:00:00Z", request.dueDate)
        }

        @Test
        @DisplayName("CreateTaskRequest with all required fields")
        fun testCreateTaskRequestAllFields() {
            val request = CreateTaskRequest(
                title = "Required Task",
                description = "Description",
                priority = TaskPriority.LOW,
                assignee = "assignee1",
                tags = emptyList(),
                dueDate = "2024-12-31T00:00:00Z"
            )
            assertEquals("Required Task", request.title)
            assertEquals("Description", request.description)
            assertEquals(TaskPriority.LOW, request.priority)
            assertEquals("assignee1", request.assignee)
        }
    }

    @Nested
    @DisplayName("UpdateTaskRequest DTO Tests")
    inner class UpdateTaskRequestTests {

        @Test
        @DisplayName("UpdateTaskRequest construction")
        fun testUpdateTaskRequest() {
            val request = UpdateTaskRequest(
                title = "Updated Title",
                description = "Updated description",
                status = TaskStatus.IN_PROGRESS,
                priority = TaskPriority.HIGH,
                assignee = "newAssignee",
                tags = listOf("updated"),
                dueDate = "2024-03-01T00:00:00Z"
            )

            assertEquals("Updated Title", request.title)
            assertEquals("Updated description", request.description)
            assertEquals(TaskStatus.IN_PROGRESS, request.status)
            assertEquals(TaskPriority.HIGH, request.priority)
            assertEquals("newAssignee", request.assignee)
        }

        @Test
        @DisplayName("UpdateTaskRequest with all fields")
        fun testUpdateTaskRequestAllFields() {
            val request = UpdateTaskRequest(
                title = "Complete Update",
                description = "All fields updated",
                status = TaskStatus.COMPLETED,
                priority = TaskPriority.CRITICAL,
                assignee = "assignee2",
                tags = emptyList(),
                dueDate = "2024-12-31T00:00:00Z"
            )

            assertEquals("Complete Update", request.title)
            assertEquals("All fields updated", request.description)
            assertEquals(TaskStatus.COMPLETED, request.status)
        }
    }

    @Nested
    @DisplayName("TaskListResponse DTO Tests")
    inner class TaskListResponseTests {

        @Test
        @DisplayName("TaskListResponse construction")
        fun testTaskListResponse() {
            val tasks = listOf(
                Task("1", "Task 1", "Desc1", TaskStatus.PENDING, TaskPriority.LOW, "user1", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z"),
                Task("2", "Task 2", "Desc2", TaskStatus.COMPLETED, TaskPriority.HIGH, "user2", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z")
            )

            val response = TaskListResponse(
                tasks = tasks,
                total = 2,
                page = 1,
                pageSize = 10
            )

            assertEquals(2, response.tasks.size)
            assertEquals(2, response.total)
            assertEquals(1, response.page)
            assertEquals(10, response.pageSize)
        }

        @Test
        @DisplayName("TaskListResponse with empty list")
        fun testTaskListResponseEmpty() {
            val response = TaskListResponse(
                tasks = emptyList(),
                total = 0,
                page = 1,
                pageSize = 10
            )

            assertEquals(0, response.tasks.size)
            assertEquals(0, response.total)
        }
    }

    @Nested
    @DisplayName("Kotlin-specific Features Tests")
    inner class KotlinSpecificFeaturesTests {

        @Test
        @DisplayName("Data classes support destructuring")
        fun testDestructuring() {
            val (id, title, description, status) = Task(
                "1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.LOW, "user", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z"
            )

            assertEquals("1", id)
            assertEquals("Title", title)
            assertEquals("Desc", description)
            assertEquals(TaskStatus.PENDING, status)
        }

        @Test
        @DisplayName("Data classes with required properties work correctly")
        fun testRequiredProperties() {
            val taskWithAllFields = Task(
                id = "1",
                title = "Title",
                description = "Description",
                status = TaskStatus.PENDING,
                priority = TaskPriority.MEDIUM,
                assignee = "user1",
                tags = emptyList(),
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z",
                dueDate = "2024-01-31T00:00:00Z"
            )

            assertNotNull(taskWithAllFields.description)
            assertNotNull(taskWithAllFields.priority)
            assertNotNull(taskWithAllFields.assignee)
        }

        @Test
        @DisplayName("Collections in data classes work correctly")
        fun testCollectionsInDataClasses() {
            val tags = listOf("tag1", "tag2", "tag3")
            val task = Task(
                "1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.LOW, "user", tags, "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z"
            )

            assertEquals(3, task.tags.size)
            assertTrue(task.tags.contains("tag1"))
            assertTrue(task.tags.contains("tag2"))
        }

        @Test
        @DisplayName("Enums can be used in when expressions")
        fun testEnumsInWhenExpressions() {
            val status = TaskStatus.IN_PROGRESS

            val message = when (status) {
                TaskStatus.PENDING -> "Waiting to start"
                TaskStatus.IN_PROGRESS -> "Currently working"
                TaskStatus.COMPLETED -> "Finished"
                TaskStatus.BLOCKED -> "Stuck"
                TaskStatus.REVIEW -> "Under review"
                TaskStatus.CANCELLED -> "Cancelled"
            }

            assertEquals("Currently working", message)
        }

        @Test
        @DisplayName("Extension functions can be added to DTOs")
        fun testExtensionFunctions() {
            val task = Task(
                "1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.LOW, "user", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z"
            )

            // Extension function
            fun Task.isPending() = this.status == TaskStatus.PENDING

            assertTrue(task.isPending())
        }

        @Test
        @DisplayName("DTOs support scope functions (let, apply, run, also, with)")
        fun testScopeFunctions() {
            val task = Task("1", "Title", "Desc", TaskStatus.PENDING, TaskPriority.LOW, "user", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z")
                .let { t ->
                    // Can transform
                    t.copy(title = t.title.uppercase())
                }

            assertEquals("TITLE", task.title)

            val task2 = Task("1", "Original", "Desc", TaskStatus.PENDING, TaskPriority.LOW, "user", emptyList(), "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z", "2024-01-31T00:00:00Z")
                .also {
                    // Side effects
                    assertNotNull(it.id)
                }

            assertEquals("Original", task2.title)
        }
    }

    @Nested
    @DisplayName("Error Response DTO Tests")
    inner class ErrorResponseTests {

        @Test
        @DisplayName("ErrorResponse construction")
        fun testErrorResponse() {
            val error = ErrorResponse(
                error = "BadRequest",
                message = "Bad request",
                details = "Invalid input" as Object
            )

            assertEquals("BadRequest", error.error)
            assertEquals("Bad request", error.message)
            assertEquals("Invalid input", error.details as String)
        }

        @Test
        @DisplayName("ErrorResponse with all fields")
        fun testErrorResponseAllFields() {
            val error = ErrorResponse(
                error = "InternalServerError",
                message = "Internal error",
                details = mapOf("stack" to "trace info") as Object
            )

            assertEquals("InternalServerError", error.error)
            assertEquals("Internal error", error.message)
            assertNotNull(error.details)
        }
    }

    @Nested
    @DisplayName("NotifyBrowserRequest DTO Tests")
    inner class NotifyBrowserRequestTests {

        @Test
        @DisplayName("NotifyBrowserRequest construction")
        fun testNotifyBrowserRequest() {
            val request = NotifyBrowserRequest(
                message = "Task updated",
                type = "info",
                userId = "user-1"
            )

            assertEquals("user-1", request.userId)
            assertEquals("Task updated", request.message)
            assertEquals("info", request.type)
        }
    }

    @Nested
    @DisplayName("UpdateTaskStatusRequest DTO Tests")
    inner class UpdateTaskStatusRequestTests {

        @Test
        @DisplayName("UpdateTaskStatusRequest construction")
        fun testUpdateTaskStatusRequest() {
            val request = UpdateTaskStatusRequest(
                status = TaskStatus.COMPLETED
            )

            assertEquals(TaskStatus.COMPLETED, request.status)
        }

        @Test
        @DisplayName("Can update status to any TaskStatus value")
        fun testUpdateToAllStatuses() {
            val statuses = listOf(
                TaskStatus.PENDING,
                TaskStatus.IN_PROGRESS,
                TaskStatus.BLOCKED,
                TaskStatus.REVIEW,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
            )

            statuses.forEach { status ->
                val request = UpdateTaskStatusRequest(status)
                assertEquals(status, request.status)
            }
        }
    }

    @Nested
    @DisplayName("TaskStatistics DTO Tests")
    inner class TaskStatisticsTests {

        @Test
        @DisplayName("TaskStatistics construction")
        fun testTaskStatistics() {
            val stats = TaskStatistics(
                totalTasks = 100,
                byStatus = mapOf(
                    "COMPLETED" to 45,
                    "IN_PROGRESS" to 30,
                    "PENDING" to 20,
                    "BLOCKED" to 5
                ),
                byPriority = mapOf(
                    "HIGH" to 40,
                    "MEDIUM" to 35,
                    "LOW" to 25
                )
            )

            assertEquals(100, stats.totalTasks)
            assertEquals(45, stats.byStatus["COMPLETED"])
            assertEquals(30, stats.byStatus["IN_PROGRESS"])
            assertEquals(40, stats.byPriority["HIGH"])
        }

        @Test
        @DisplayName("TaskStatistics calculation methods")
        fun testStatisticsCalculations() {
            val stats = TaskStatistics(
                totalTasks = 100,
                byStatus = mapOf(
                    "COMPLETED" to 45,
                    "IN_PROGRESS" to 30,
                    "PENDING" to 20,
                    "BLOCKED" to 5
                ),
                byPriority = emptyMap()
            )

            // Calculate completion percentage
            val completed = stats.byStatus["COMPLETED"] ?: 0
            val completionPercentage = (completed.toDouble() / stats.totalTasks) * 100
            assertEquals(45.0, completionPercentage)
        }
    }
}
