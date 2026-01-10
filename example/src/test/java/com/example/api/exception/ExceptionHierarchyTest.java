package com.example.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for exception hierarchy and specific exception types.
 * Target coverage: 100%
 */
class ExceptionHierarchyTest {

    @Test
    void testBadRequestException() {
        BadRequestException exception = new BadRequestException("Invalid input");

        assertEquals(400, exception.getStatusCode());
        assertEquals("Invalid input", exception.getMessage());
        assertTrue(exception instanceof ApiException);
    }

    @Test
    void testNotFoundException() {
        NotFoundException exception = new NotFoundException("Resource not found");

        assertEquals(404, exception.getStatusCode());
        assertEquals("Resource not found", exception.getMessage());
        assertTrue(exception instanceof ApiException);
    }

    @Test
    void testInternalServerErrorException() {
        InternalServerErrorException exception = new InternalServerErrorException("Server error");

        assertEquals(500, exception.getStatusCode());
        assertEquals("Server error", exception.getMessage());
        assertTrue(exception instanceof ApiException);
    }

    @Test
    void testNotImplementedException() {
        NotImplementedException exception = new NotImplementedException("Not implemented");

        assertEquals("Not implemented", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
        // NotImplementedException extends RuntimeException, not ApiException
    }

    @Test
    void testExceptionInheritance() {
        // ApiException subclasses
        assertTrue(new BadRequestException("test") instanceof ApiException);
        assertTrue(new NotFoundException("test") instanceof ApiException);
        assertTrue(new InternalServerErrorException("test") instanceof ApiException);

        // NotImplementedException extends RuntimeException
        assertTrue(new NotImplementedException("test") instanceof RuntimeException);
    }

    @Test
    void testStatusCodes() {
        assertEquals(400, new BadRequestException("test").getStatusCode());
        assertEquals(404, new NotFoundException("test").getStatusCode());
        assertEquals(500, new InternalServerErrorException("test").getStatusCode());
        // NotImplementedException doesn't have getStatusCode() method
    }

    @Test
    void testExceptionWithCause() {
        RuntimeException cause = new RuntimeException("Root cause");

        BadRequestException badRequest = new BadRequestException("Bad request", cause);
        assertEquals(cause, badRequest.getCause());

        NotFoundException notFound = new NotFoundException("Not found", cause);
        assertEquals(cause, notFound.getCause());

        InternalServerErrorException serverError = new InternalServerErrorException("Server error", cause);
        assertEquals(cause, serverError.getCause());

        // NotImplementedException might not have constructor with cause
    }

    @Test
    void testExceptionMessagesPreserved() {
        String message1 = "Invalid email format";
        String message2 = "User not found: 123";
        String message3 = "Database connection failed";
        String message4 = "Feature not yet implemented";

        assertEquals(message1, new BadRequestException(message1).getMessage());
        assertEquals(message2, new NotFoundException(message2).getMessage());
        assertEquals(message3, new InternalServerErrorException(message3).getMessage());
        assertEquals(message4, new NotImplementedException(message4).getMessage());
    }

    @Test
    void testCatchAsApiException() {
        // Verify exceptions can be caught as ApiException
        try {
            throw new BadRequestException("test");
        } catch (ApiException e) {
            assertEquals(400, e.getStatusCode());
        }

        try {
            throw new NotFoundException("test");
        } catch (ApiException e) {
            assertEquals(404, e.getStatusCode());
        }
    }

    @Test
    void testThrowAndCatch() {
        assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException("test");
        });

        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("test");
        });

        assertThrows(InternalServerErrorException.class, () -> {
            throw new InternalServerErrorException("test");
        });

        assertThrows(NotImplementedException.class, () -> {
            throw new NotImplementedException("test");
        });
    }
}
