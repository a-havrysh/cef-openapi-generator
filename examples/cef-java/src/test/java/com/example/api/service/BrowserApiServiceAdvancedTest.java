package com.example.api.service;

import com.example.api.dto.NotifyBrowserRequest;
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BrowserApiService Advanced Scenarios and CEF Integration")
class BrowserApiServiceAdvancedTest {

    private BrowserApiService service;

    @Mock
    private CefBrowser mockBrowser;

    @Mock
    private CefFrame mockFrame;

    @Mock
    private CefRequest mockCefRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BrowserApiService() {};
    }

    @Nested
    @DisplayName("NotifyBrowser Request Validation")
    class NotifyBrowserRequestValidationTests {

        @Test
        @DisplayName("notifyBrowser with null request throws BadRequestException")
        void notifyBrowser_null_request() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null) {
                        throw new com.example.api.exception.BadRequestException("Request cannot be null");
                    }
                    return null;
                }
            };

            assertThatThrownBy(() -> customService.notifyBrowser(null))
                    .isInstanceOf(com.example.api.exception.BadRequestException.class);
        }

        @Test
        @DisplayName("notifyBrowser with null message throws validation error")
        void notifyBrowser_null_message() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null || request.getMessage().isEmpty()) {
                        throw new com.example.api.exception.BadRequestException("Message is required");
                    }
                    return null;
                }
            };

            NotifyBrowserRequest requestWithoutMessage = new NotifyBrowserRequest();
            requestWithoutMessage.setType("info");

            assertThatThrownBy(() -> customService.notifyBrowser(requestWithoutMessage))
                    .isInstanceOf(com.example.api.exception.BadRequestException.class)
                    .hasMessage("Message is required");
        }

        @Test
        @DisplayName("notifyBrowser with empty message throws validation error")
        void notifyBrowser_empty_message() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null || request.getMessage().isEmpty()) {
                        throw new com.example.api.exception.BadRequestException("Message cannot be empty");
                    }
                    return null;
                }
            };

            NotifyBrowserRequest requestWithEmptyMessage = new NotifyBrowserRequest("", "warning", "user-1");

            assertThatThrownBy(() -> customService.notifyBrowser(requestWithEmptyMessage))
                    .isInstanceOf(com.example.api.exception.BadRequestException.class);
        }

        @Test
        @DisplayName("notifyBrowser with valid request succeeds")
        void notifyBrowser_valid_request() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null || request.getMessage().isEmpty()) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    return null;
                }
            };

            NotifyBrowserRequest validRequest = new NotifyBrowserRequest("Hello", "info", "user-123");
            Void result = customService.notifyBrowser(validRequest);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser with very long message")
        void notifyBrowser_very_long_message() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null || request.getMessage().isEmpty()) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    if (request.getMessage().length() > 10000) {
                        throw new com.example.api.exception.BadRequestException("Message too long");
                    }
                    return null;
                }
            };

            String longMessage = "A".repeat(10001);
            NotifyBrowserRequest request = new NotifyBrowserRequest(longMessage, "info", "user-1");

            assertThatThrownBy(() -> customService.notifyBrowser(request))
                    .isInstanceOf(com.example.api.exception.BadRequestException.class)
                    .hasMessage("Message too long");
        }
    }

    @Nested
    @DisplayName("CEF Browser Integration Scenarios")
    class CefBrowserIntegrationTests {

        @Test
        @DisplayName("handleNotifyBrowser has access to CEF browser object")
        void handleNotifyBrowser_browser_access() {
            final CefBrowser[] capturedBrowser = new CefBrowser[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedBrowser[0] = browser;
                    return ApiResponse.ok(null);
                }
            };

            customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedBrowser[0]).isSameAs(mockBrowser);
        }

        @Test
        @DisplayName("handleNotifyBrowser has access to CEF frame object")
        void handleNotifyBrowser_frame_access() {
            final CefFrame[] capturedFrame = new CefFrame[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedFrame[0] = frame;
                    return ApiResponse.ok(null);
                }
            };

            customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedFrame[0]).isSameAs(mockFrame);
        }

        @Test
        @DisplayName("handleNotifyBrowser has access to CEF request object")
        void handleNotifyBrowser_request_access() {
            final CefRequest[] capturedRequest = new CefRequest[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedRequest[0] = cefRequest;
                    return ApiResponse.ok(null);
                }
            };

            customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedRequest[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("handleNotifyBrowser can execute JavaScript using browser object")
        void handleNotifyBrowser_execute_javascript() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    if (browser != null && request.getMessage() != null) {
                        browser.executeJavaScript(
                                "showNotification('" + request.getMessage() + "')", "", 0);
                    }
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("Alert!", "info", "user-1");
            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            verify(mockBrowser, times(1)).executeJavaScript(
                    "showNotification('Alert!')", "", 0);
        }

        @Test
        @DisplayName("handleNotifyBrowser with null CEF objects handled gracefully")
        void handleNotifyBrowser_null_cef_objects() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    // Should handle null CEF objects gracefully
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("Test", "info", "user-1");
            ApiResponse<Void> response = customService.handleNotifyBrowser(request, null, null, null);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Request Message Types and Content")
    class RequestMessageTypeTests {

        @Test
        @DisplayName("notifyBrowser with info type message")
        void notifyBrowser_info_type() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    if (!isValidType(request.getType())) {
                        throw new com.example.api.exception.BadRequestException("Invalid type");
                    }
                    return null;
                }

                private boolean isValidType(String type) {
                    return type != null && (type.equals("info") || type.equals("warning") ||
                            type.equals("error") || type.equals("success"));
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("Information message", "info", "user-1");
            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser with warning type message")
        void notifyBrowser_warning_type() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    return null;
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("Warning message", "warning", "user-1");
            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser with error type message")
        void notifyBrowser_error_type() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    return null;
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("Error occurred", "error", "user-1");
            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser with special characters in message")
        void notifyBrowser_special_characters() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null || request.getMessage().isEmpty()) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    return null;
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest(
                    "Message with !@#$%^&*() characters", "info", "user-1");
            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser with unicode characters in message")
        void notifyBrowser_unicode_message() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null || request.getMessage().isEmpty()) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    return null;
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest(
                    "Unicode message: 你好 مرحبا Здравствуйте", "info", "user-1");
            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Default Method Behavior")
    class DefaultMethodBehaviorTests {

        @Test
        @DisplayName("default notifyBrowser throws NotImplementedException")
        void default_notifyBrowser_throws() {
            assertThatThrownBy(() -> service.notifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1")))
                    .isInstanceOf(NotImplementedException.class);
        }

        @Test
        @DisplayName("default handleNotifyBrowser throws NotImplementedException")
        void default_handleNotifyBrowser_throws() {
            assertThatThrownBy(() -> service.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(NotImplementedException.class);
        }

        @Test
        @DisplayName("new instance with no overrides throws NotImplementedException")
        void new_instance_not_implemented() {
            BrowserApiService newService = new BrowserApiService() {};

            assertThatThrownBy(() -> newService.notifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1")))
                    .isInstanceOf(NotImplementedException.class);
        }
    }

    @Nested
    @DisplayName("Response Wrapping and Status Codes")
    class ResponseWrappingTests {

        @Test
        @DisplayName("handleNotifyBrowser wraps Void result in ApiResponse")
        void handleNotifyBrowser_wraps_void() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    return null;
                }
            };

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("handleNotifyBrowser returns 200 status code by default")
        void handleNotifyBrowser_returns_200_status() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    return null;
                }
            };

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("handleNotifyBrowser can set custom status code")
        void handleNotifyBrowser_custom_status_code() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.noContent();
                }
            };

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(204);
        }

        @Test
        @DisplayName("handleNotifyBrowser can add custom headers")
        void handleNotifyBrowser_custom_headers() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    ApiResponse<Void> response = ApiResponse.ok((Void) null);
                    response = response.header("X-Notification-Sent", "true");
                    response = response.header("X-User-Id", request.getUserId());
                    return response;
                }
            };

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-123"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getHeaders())
                    .containsEntry("X-Notification-Sent", "true")
                    .containsEntry("X-User-Id", "user-123");
        }
    }

    @Nested
    @DisplayName("Method Override Patterns")
    class MethodOverridePatternTests {

        @Test
        @DisplayName("override business method only - wrapper delegates")
        void override_business_only() {
            final AtomicInteger callCount = new AtomicInteger(0);

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    callCount.incrementAndGet();
                    return null;
                }
            };

            customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("override wrapper only - bypasses business method")
        void override_wrapper_only() {
            final AtomicInteger businessCallCount = new AtomicInteger(0);

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    businessCallCount.incrementAndGet();
                    return null;
                }

                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.ok(null);
                }
            };

            customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCallCount.get()).isZero();
        }

        @Test
        @DisplayName("override wrapper and call business method from wrapper")
        void override_wrapper_call_business() {
            final AtomicBoolean businessCalled = new AtomicBoolean(false);

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    businessCalled.set(true);
                    return null;
                }

                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    Void result = notifyBrowser(request);
                    return ApiResponse.ok(result).header("X-Called", "true");
                }
            };

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCalled.get()).isTrue();
            assertThat(response.getHeaders()).containsEntry("X-Called", "true");
        }

        @Test
        @DisplayName("both methods can be called independently")
        void both_methods_independent() {
            final AtomicInteger wrapperCallCount = new AtomicInteger(0);
            final AtomicInteger businessCallCount = new AtomicInteger(0);

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    businessCallCount.incrementAndGet();
                    return null;
                }

                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    wrapperCallCount.incrementAndGet();
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("test", "info", "user-1");
            customService.notifyBrowser(request);
            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);
            customService.notifyBrowser(request);

            assertThat(businessCallCount.get()).isEqualTo(2);
            assertThat(wrapperCallCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("User ID Handling")
    class UserIdHandlingTests {

        @Test
        @DisplayName("notifyBrowser with null userId")
        void notifyBrowser_null_userId() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null || request.getMessage().isEmpty()) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    return null;
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("test", "info", null);
            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser with userId preservation")
        void notifyBrowser_userId_preservation() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    if (request == null || request.getMessage() == null) {
                        throw new com.example.api.exception.BadRequestException("Invalid request");
                    }
                    // userId should be preserved through the request
                    assertThat(request.getUserId()).isEqualTo("user-456");
                    return null;
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("test", "info", "user-456");
            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("handleNotifyBrowser passes userId to business method")
        void handleNotifyBrowser_userId_delegation() {
            final String[] capturedUserId = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    capturedUserId[0] = request.getUserId();
                    return null;
                }
            };

            NotifyBrowserRequest request = new NotifyBrowserRequest("test", "info", "user-789");
            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedUserId[0]).isEqualTo("user-789");
        }
    }

    @Nested
    @DisplayName("Exception Handling in Service")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("business method can throw custom exceptions")
        void notifyBrowser_custom_exception() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    throw new IllegalStateException("Browser not initialized");
                }
            };

            assertThatThrownBy(() -> customService.notifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Browser not initialized");
        }

        @Test
        @DisplayName("wrapper propagates exception from business method")
        void handleNotifyBrowser_propagates_exception() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    throw new RuntimeException("Service error");
                }
            };

            assertThatThrownBy(() -> customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Service error");
        }

        @Test
        @DisplayName("wrapper can catch and handle exceptions")
        void handleNotifyBrowser_exception_handling() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest request) {
                    throw new RuntimeException("Business logic error");
                }

                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest request, CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    try {
                        notifyBrowser(request);
                    } catch (RuntimeException e) {
                        ApiResponse<Void> response = ApiResponse.ok((Void) null);
                        return response.header("X-Error", e.getMessage());
                    }
                    return ApiResponse.ok((Void) null);
                }
            };

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    new NotifyBrowserRequest("test", "info", "user-1"),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getHeaders()).containsEntry("X-Error", "Business logic error");
        }
    }

    @Nested
    @DisplayName("NotifyBrowserRequest Builder Pattern")
    class RequestBuilderPatternTests {

        @Test
        @DisplayName("NotifyBrowserRequest builder creates complete object")
        void request_builder_complete() {
            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test message")
                    .type("info")
                    .userId("user-123")
                    .build();

            assertThat(request.getMessage()).isEqualTo("Test message");
            assertThat(request.getType()).isEqualTo("info");
            assertThat(request.getUserId()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("NotifyBrowserRequest builder with partial fields")
        void request_builder_partial() {
            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Partial message")
                    .build();

            assertThat(request.getMessage()).isEqualTo("Partial message");
            assertThat(request.getType()).isNull();
            assertThat(request.getUserId()).isNull();
        }

        @Test
        @DisplayName("NotifyBrowserRequest constructor vs builder equivalence")
        void request_constructor_builder_equivalence() {
            NotifyBrowserRequest fromConstructor = new NotifyBrowserRequest("msg", "info", "user-1");
            NotifyBrowserRequest fromBuilder = NotifyBrowserRequest.builder()
                    .message("msg")
                    .type("info")
                    .userId("user-1")
                    .build();

            assertThat(fromConstructor.getMessage()).isEqualTo(fromBuilder.getMessage());
            assertThat(fromConstructor.getType()).isEqualTo(fromBuilder.getType());
            assertThat(fromConstructor.getUserId()).isEqualTo(fromBuilder.getUserId());
        }
    }
}
