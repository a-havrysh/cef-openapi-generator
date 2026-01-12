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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BrowserApiService Interface")
class BrowserApiServiceTest {

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
    @DisplayName("Browser Notification Operations")
    class BrowserNotificationTests {

        @Test
        @DisplayName("notifyBrowser throws NotImplementedException by default")
        void notifyBrowser_default_throws_not_implemented() {
            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test message")
                    .type("info")
                    .userId("user-123")
                    .build();

            assertThatThrownBy(() -> service.notifyBrowser(request))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("notifyBrowser not implemented");
        }

        @Test
        @DisplayName("notifyBrowser can be overridden to return null (Void)")
        void notifyBrowser_can_be_overridden() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test message")
                    .build();

            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser receives correct NotifyBrowserRequest parameter")
        void notifyBrowser_receives_correct_parameter() {
            final NotifyBrowserRequest[] capturedRequest = new NotifyBrowserRequest[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedRequest[0] = notifyBrowserRequest;
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Custom message")
                    .type("warning")
                    .userId("user-456")
                    .build();

            customService.notifyBrowser(request);

            assertThat(capturedRequest[0]).isSameAs(request);
            assertThat(capturedRequest[0].getMessage()).isEqualTo("Custom message");
            assertThat(capturedRequest[0].getType()).isEqualTo("warning");
            assertThat(capturedRequest[0].getUserId()).isEqualTo("user-456");
        }

        @Test
        @DisplayName("handleNotifyBrowser delegates to notifyBrowser business method")
        void handleNotifyBrowser_delegates_to_notify_browser() {
            final NotifyBrowserRequest[] capturedRequest = new NotifyBrowserRequest[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedRequest[0] = notifyBrowserRequest;
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Delegated notification")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedRequest[0]).isSameAs(request);
        }

        @Test
        @DisplayName("handleNotifyBrowser wraps result in ApiResponse.ok(null)")
        void handleNotifyBrowser_wraps_in_api_response_with_null() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("handleNotifyBrowser can be overridden for custom response")
        void handleNotifyBrowser_can_be_overridden() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Custom override")
                    .build();

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("handleNotifyBrowser has access to CEF browser object")
        void handleNotifyBrowser_has_access_to_cef_browser() {
            final CefBrowser[] capturedBrowser = new CefBrowser[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedBrowser[0] = browser;
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Browser access test")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedBrowser[0]).isSameAs(mockBrowser);
        }

        @Test
        @DisplayName("handleNotifyBrowser has access to CEF frame object")
        void handleNotifyBrowser_has_access_to_cef_frame() {
            final CefFrame[] capturedFrame = new CefFrame[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedFrame[0] = frame;
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Frame access test")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedFrame[0]).isSameAs(mockFrame);
        }

        @Test
        @DisplayName("handleNotifyBrowser has access to CEF request object")
        void handleNotifyBrowser_has_access_to_cef_request() {
            final CefRequest[] capturedRequest = new CefRequest[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedRequest[0] = cefRequest;
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Request access test")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedRequest[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("handleNotifyBrowser can access all CEF objects simultaneously")
        void handleNotifyBrowser_can_access_all_cef_objects() {
            final CefBrowser[] capturedBrowser = new CefBrowser[1];
            final CefFrame[] capturedFrame = new CefFrame[1];
            final CefRequest[] capturedRequest = new CefRequest[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    capturedBrowser[0] = browser;
                    capturedFrame[0] = frame;
                    capturedRequest[0] = cefRequest;
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Multi-object access test")
                    .type("success")
                    .userId("user-789")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedBrowser[0]).isSameAs(mockBrowser);
            assertThat(capturedFrame[0]).isSameAs(mockFrame);
            assertThat(capturedRequest[0]).isSameAs(mockCefRequest);
        }

        @Test
        @DisplayName("notifyBrowser works with various NotifyBrowserRequest instances")
        void notifyBrowser_works_with_various_request_instances() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    assertThat(notifyBrowserRequest).isNotNull();
                    return null;
                }
            };

            NotifyBrowserRequest request1 = NotifyBrowserRequest.builder()
                    .message("Message 1")
                    .type("info")
                    .userId("user-1")
                    .build();

            NotifyBrowserRequest request2 = NotifyBrowserRequest.builder()
                    .message("Message 2")
                    .type("error")
                    .userId("user-2")
                    .build();

            NotifyBrowserRequest request3 = NotifyBrowserRequest.builder()
                    .message("Message 3")
                    .type("warning")
                    .userId("user-3")
                    .build();

            Void result1 = customService.notifyBrowser(request1);
            Void result2 = customService.notifyBrowser(request2);
            Void result3 = customService.notifyBrowser(request3);

            assertThat(result1).isNull();
            assertThat(result2).isNull();
            assertThat(result3).isNull();
        }
    }

    @Nested
    @DisplayName("Default Behavior and Exception Handling")
    class DefaultBehaviorTests {

        @Test
        @DisplayName("Business method throws NotImplementedException")
        void business_method_throws_not_implemented() {
            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Exception test")
                    .build();

            assertThatThrownBy(() -> service.notifyBrowser(request))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("notifyBrowser not implemented");
        }

        @Test
        @DisplayName("NotImplementedException message indicates which method is not implemented")
        void not_implemented_exception_has_clear_message() {
            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            Throwable thrown = catchThrowable(() -> service.notifyBrowser(request));

            assertThat(thrown)
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessageContaining("notifyBrowser");
        }

        @Test
        @DisplayName("Wrapper method provides default implementation that calls business method")
        void wrapper_method_provides_default_implementation() {
            final boolean[] businessMethodCalled = new boolean[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    businessMethodCalled[0] = true;
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Wrapper test")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessMethodCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Wrapper method provides default ApiResponse wrapping")
        void wrapper_method_provides_default_response_wrapping() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Response wrapping test")
                    .build();

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getContentType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("Overriding wrapper method bypasses default business method call")
        void override_wrapper_method_bypasses_business_method() {
            final boolean[] businessMethodCalled = new boolean[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    businessMethodCalled[0] = true;
                    return null;
                }

                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Custom override")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessMethodCalled[0]).isFalse();
        }

        @Test
        @DisplayName("Wrapper method can be customized while keeping business method")
        void wrapper_customization_with_business_reuse() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    return null;
                }

                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    Void result = notifyBrowser(notifyBrowserRequest);
                    return ApiResponse.ok(result).header("X-Notification-Sent", "true");
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Custom wrapper with business reuse")
                    .build();

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getHeaders()).containsEntry("X-Notification-Sent", "true");
        }
    }

    @Nested
    @DisplayName("Request Parameter Variations")
    class RequestParameterVariationsTests {

        @Test
        @DisplayName("notifyBrowser accepts request with all fields set")
        void notifyBrowser_accepts_full_request() {
            NotifyBrowserRequest fullRequest = NotifyBrowserRequest.builder()
                    .message("Complete notification")
                    .type("info")
                    .userId("user-complete")
                    .build();

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    assertThat(notifyBrowserRequest.getMessage()).isEqualTo("Complete notification");
                    assertThat(notifyBrowserRequest.getType()).isEqualTo("info");
                    assertThat(notifyBrowserRequest.getUserId()).isEqualTo("user-complete");
                    return null;
                }
            };

            Void result = customService.notifyBrowser(fullRequest);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser accepts request with minimal fields")
        void notifyBrowser_accepts_minimal_request() {
            NotifyBrowserRequest minimalRequest = NotifyBrowserRequest.builder()
                    .message("Minimal notification")
                    .build();

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    assertThat(notifyBrowserRequest.getMessage()).isEqualTo("Minimal notification");
                    return null;
                }
            };

            Void result = customService.notifyBrowser(minimalRequest);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser preserves request message content")
        void notifyBrowser_preserves_message_content() {
            final String[] capturedMessage = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedMessage[0] = notifyBrowserRequest.getMessage();
                    return null;
                }
            };

            String testMessage = "This is a test message with special chars: !@#$%^&*()";
            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message(testMessage)
                    .build();

            customService.notifyBrowser(request);
            assertThat(capturedMessage[0]).isEqualTo(testMessage);
        }

        @Test
        @DisplayName("handleNotifyBrowser preserves request data through wrapper")
        void handleNotifyBrowser_preserves_request_through_wrapper() {
            final String[] capturedMessage = new String[1];
            final String[] capturedType = new String[1];
            final String[] capturedUserId = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedMessage[0] = notifyBrowserRequest.getMessage();
                    capturedType[0] = notifyBrowserRequest.getType();
                    capturedUserId[0] = notifyBrowserRequest.getUserId();
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Wrapper preservation test")
                    .type("warning")
                    .userId("test-user-123")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedMessage[0]).isEqualTo("Wrapper preservation test");
            assertThat(capturedType[0]).isEqualTo("warning");
            assertThat(capturedUserId[0]).isEqualTo("test-user-123");
        }

        @Test
        @DisplayName("notifyBrowser accepts null request")
        void notifyBrowser_accepts_null_request() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    if (notifyBrowserRequest == null) {
                        return null;
                    }
                    return null;
                }
            };

            Void result = customService.notifyBrowser(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("handleNotifyBrowser accepts null request")
        void handleNotifyBrowser_accepts_null_request() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    return null;
                }
            };

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    null, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("handleNotifyBrowser with empty request fields")
        void handleNotifyBrowser_with_empty_request_fields() {
            final String[] capturedMessage = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    if (notifyBrowserRequest != null) {
                        capturedMessage[0] = notifyBrowserRequest.getMessage();
                    }
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder().build();
            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(capturedMessage[0]).isNull();
        }
    }

    @Nested
    @DisplayName("ApiResponse Status Code and Override Tests")
    class ApiResponseStatusCodeTests {

        @Test
        @DisplayName("handleNotifyBrowser returns 200 status code by default")
        void handleNotifyBrowser_default_returns_200() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("handleNotifyBrowser can return custom status code via ApiResponse.ok()")
        void handleNotifyBrowser_custom_status_via_ok() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return (ApiResponse<Void>) (ApiResponse<?>) ApiResponse.ok(null).header("X-Notified", "true");
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getHeaders()).containsEntry("X-Notified", "true");
        }
    }

    @Nested
    @DisplayName("Override Chain Behavior")
    class OverrideChainTests {

        @Test
        @DisplayName("override business method only - wrapper calls it")
        void override_business_only_wrapper_calls() {
            final boolean[] businessCalled = new boolean[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    businessCalled[0] = true;
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCalled[0]).isTrue();
        }

        @Test
        @DisplayName("override both methods - wrapper not calling business by default")
        void override_both_wrapper_override_only() {
            final boolean[] businessCalled = new boolean[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    businessCalled[0] = true;
                    return null;
                }

                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    return ApiResponse.ok(null);
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCalled[0]).isFalse();
        }

        @Test
        @DisplayName("override both and explicitly call business from wrapper")
        void override_both_call_business_from_wrapper() {
            final boolean[] businessCalled = new boolean[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    businessCalled[0] = true;
                    return null;
                }

                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    Void result = notifyBrowser(notifyBrowserRequest);
                    return ApiResponse.ok(result).header("X-Business-Called", "true");
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(businessCalled[0]).isTrue();
            assertThat(response.getHeaders()).containsEntry("X-Business-Called", "true");
        }
    }

    @Nested
    @DisplayName("Exception Propagation Tests")
    class ExceptionPropagationTests {

        @Test
        @DisplayName("exception in business method propagates through wrapper")
        void exception_in_business_propagates() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    throw new IllegalArgumentException("Invalid notification");
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            assertThatThrownBy(() ->
                    customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid notification");
        }

        @Test
        @DisplayName("exception in wrapper method is not caught")
        void exception_in_wrapper_not_caught() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    throw new RuntimeException("Wrapper error");
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            assertThatThrownBy(() ->
                    customService.handleNotifyBrowser(request, mockBrowser, mockFrame, mockCefRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Wrapper error");
        }

        @Test
        @DisplayName("NotImplementedException when business method not overridden")
        void not_implemented_exception_when_not_overridden() {
            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .build();

            assertThatThrownBy(() -> service.notifyBrowser(request))
                    .isInstanceOf(NotImplementedException.class)
                    .hasMessage("notifyBrowser not implemented");
        }
    }

    @Nested
    @DisplayName("Multiple Request Processing")
    class MultipleRequestProcessingTests {

        @Test
        @DisplayName("notifyBrowser handles multiple requests sequentially")
        void notifyBrowser_handles_multiple_requests() {
            final List<String> messages = new ArrayList<>();

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    if (notifyBrowserRequest != null) {
                        messages.add(notifyBrowserRequest.getMessage());
                    }
                    return null;
                }
            };

            customService.notifyBrowser(NotifyBrowserRequest.builder().message("Message 1").build());
            customService.notifyBrowser(NotifyBrowserRequest.builder().message("Message 2").build());
            customService.notifyBrowser(NotifyBrowserRequest.builder().message("Message 3").build());

            assertThat(messages).hasSize(3).containsExactly("Message 1", "Message 2", "Message 3");
        }

        @Test
        @DisplayName("handleNotifyBrowser processes multiple requests through wrapper")
        void handleNotifyBrowser_multiple_requests_through_wrapper() {
            final List<String> messages = new ArrayList<>();

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    if (notifyBrowserRequest != null) {
                        messages.add(notifyBrowserRequest.getMessage());
                    }
                    return null;
                }
            };

            customService.handleNotifyBrowser(
                    NotifyBrowserRequest.builder().message("Msg 1").build(),
                    mockBrowser, mockFrame, mockCefRequest);
            customService.handleNotifyBrowser(
                    NotifyBrowserRequest.builder().message("Msg 2").build(),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(messages).hasSize(2).containsExactly("Msg 1", "Msg 2");
        }
    }

    @Nested
    @DisplayName("Additional Edge Cases and Scenarios")
    class AdditionalEdgeCasesTests {

        @Test
        @DisplayName("notifyBrowser with very long message")
        void notifyBrowser_with_very_long_message() {
            final String[] capturedMessage = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedMessage[0] = notifyBrowserRequest.getMessage();
                    return null;
                }
            };

            String longMessage = "a".repeat(10000);
            NotifyBrowserRequest request = NotifyBrowserRequest.builder().message(longMessage).build();
            customService.notifyBrowser(request);

            assertThat(capturedMessage[0]).isEqualTo(longMessage);
        }

        @Test
        @DisplayName("notifyBrowser with message containing special characters")
        void notifyBrowser_with_special_characters() {
            final String[] capturedMessage = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedMessage[0] = notifyBrowserRequest.getMessage();
                    return null;
                }
            };

            String specialMessage = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            NotifyBrowserRequest request = NotifyBrowserRequest.builder().message(specialMessage).build();
            customService.notifyBrowser(request);

            assertThat(capturedMessage[0]).isEqualTo(specialMessage);
        }

        @Test
        @DisplayName("notifyBrowser with unicode characters in message")
        void notifyBrowser_with_unicode_characters() {
            final String[] capturedMessage = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedMessage[0] = notifyBrowserRequest.getMessage();
                    return null;
                }
            };

            String unicodeMessage = "Hello world 你好 مرحبا привет こんにちは";
            NotifyBrowserRequest request = NotifyBrowserRequest.builder().message(unicodeMessage).build();
            customService.notifyBrowser(request);

            assertThat(capturedMessage[0]).isEqualTo(unicodeMessage);
        }

        @Test
        @DisplayName("notifyBrowser with very long userId")
        void notifyBrowser_with_very_long_user_id() {
            final String[] capturedUserId = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedUserId[0] = notifyBrowserRequest.getUserId();
                    return null;
                }
            };

            String longUserId = "user-" + "x".repeat(995);
            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Test")
                    .userId(longUserId)
                    .build();
            customService.notifyBrowser(request);

            assertThat(capturedUserId[0]).isEqualTo(longUserId);
        }

        @Test
        @DisplayName("notifyBrowser with various notification types")
        void notifyBrowser_with_various_types() {
            final List<String> types = new ArrayList<>();

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    types.add(notifyBrowserRequest.getType());
                    return null;
                }
            };

            customService.notifyBrowser(NotifyBrowserRequest.builder().message("Test").type("info").build());
            customService.notifyBrowser(NotifyBrowserRequest.builder().message("Test").type("warning").build());
            customService.notifyBrowser(NotifyBrowserRequest.builder().message("Test").type("error").build());
            customService.notifyBrowser(NotifyBrowserRequest.builder().message("Test").type("success").build());

            assertThat(types).containsExactly("info", "warning", "error", "success");
        }

        @Test
        @DisplayName("handleNotifyBrowser with custom headers and metadata")
        void handleNotifyBrowser_with_custom_headers_and_metadata() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    ApiResponse<Void> resp = ApiResponse.ok(null);
                    resp = resp.header("X-Notification-Type", notifyBrowserRequest.getType());
                    resp = resp.header("X-User-ID", notifyBrowserRequest.getUserId());
                    resp = resp.header("X-Timestamp", "2024-01-01T12:00:00Z");
                    return resp;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("Alert")
                    .type("warning")
                    .userId("user-123")
                    .build();

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    request, mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getHeaders())
                    .containsEntry("X-Notification-Type", "warning")
                    .containsEntry("X-User-ID", "user-123")
                    .containsEntry("X-Timestamp", "2024-01-01T12:00:00Z");
        }

        @Test
        @DisplayName("notifyBrowser with null message field")
        void notifyBrowser_with_null_message() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    assertThat(notifyBrowserRequest).isNotNull();
                    if (notifyBrowserRequest.getMessage() == null) {
                        return null;
                    }
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder().type("info").build();
            Void result = customService.notifyBrowser(request);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("notifyBrowser with empty string fields")
        void notifyBrowser_with_empty_string_fields() {
            final String[] capturedMessage = new String[1];
            final String[] capturedType = new String[1];
            final String[] capturedUserId = new String[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedMessage[0] = notifyBrowserRequest.getMessage();
                    capturedType[0] = notifyBrowserRequest.getType();
                    capturedUserId[0] = notifyBrowserRequest.getUserId();
                    return null;
                }
            };

            NotifyBrowserRequest request = NotifyBrowserRequest.builder()
                    .message("")
                    .type("")
                    .userId("")
                    .build();

            customService.notifyBrowser(request);

            assertThat(capturedMessage[0]).isEmpty();
            assertThat(capturedType[0]).isEmpty();
            assertThat(capturedUserId[0]).isEmpty();
        }

        @Test
        @DisplayName("handleNotifyBrowser can chain multiple header calls")
        void handleNotifyBrowser_chain_multiple_headers() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    ApiResponse<Void> resp = ApiResponse.ok(null);
                    resp = resp.header("Header1", "value1");
                    resp = resp.header("Header2", "value2");
                    resp = resp.header("Header3", "value3");
                    resp = resp.header("Header4", "value4");
                    resp = resp.header("Header5", "value5");
                    return resp;
                }
            };

            ApiResponse<Void> response = customService.handleNotifyBrowser(
                    NotifyBrowserRequest.builder().message("Test").build(),
                    mockBrowser, mockFrame, mockCefRequest);

            assertThat(response.getHeaders()).hasSize(5);
        }

        @Test
        @DisplayName("notifyBrowser processes various request field combinations")
        void notifyBrowser_various_field_combinations() {
            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    return null;
                }
            };

            NotifyBrowserRequest req1 = NotifyBrowserRequest.builder().message("Msg").build();
            NotifyBrowserRequest req2 = NotifyBrowserRequest.builder().message("Msg").type("info").build();
            NotifyBrowserRequest req3 = NotifyBrowserRequest.builder().message("Msg").type("info").userId("user-1").build();

            Void r1 = customService.notifyBrowser(req1);
            Void r2 = customService.notifyBrowser(req2);
            Void r3 = customService.notifyBrowser(req3);

            assertThat(r1).isNull();
            assertThat(r2).isNull();
            assertThat(r3).isNull();
        }

        @Test
        @DisplayName("handleNotifyBrowser preserves CEF objects through exception handling")
        void handleNotifyBrowser_preserves_cef_on_exception() {
            final CefBrowser[] browserOnException = new CefBrowser[1];

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public ApiResponse<Void> handleNotifyBrowser(
                        NotifyBrowserRequest notifyBrowserRequest,
                        CefBrowser browser, CefFrame frame, CefRequest cefRequest) {
                    browserOnException[0] = browser;
                    throw new RuntimeException("Test exception");
                }
            };

            try {
                customService.handleNotifyBrowser(
                        NotifyBrowserRequest.builder().message("Test").build(),
                        mockBrowser, mockFrame, mockCefRequest);
            } catch (RuntimeException e) {
                // Expected
            }

            assertThat(browserOnException[0]).isSameAs(mockBrowser);
        }

        @Test
        @DisplayName("notifyBrowser request parameters preserved across calls")
        void notifyBrowser_parameters_preserved_across_calls() {
            final List<NotifyBrowserRequest> capturedRequests = new ArrayList<>();

            BrowserApiService customService = new BrowserApiService() {
                @Override
                public Void notifyBrowser(NotifyBrowserRequest notifyBrowserRequest) {
                    capturedRequests.add(notifyBrowserRequest);
                    return null;
                }
            };

            NotifyBrowserRequest req1 = NotifyBrowserRequest.builder()
                    .message("Message 1")
                    .type("info")
                    .userId("user-1")
                    .build();

            NotifyBrowserRequest req2 = NotifyBrowserRequest.builder()
                    .message("Message 2")
                    .type("error")
                    .userId("user-2")
                    .build();

            customService.notifyBrowser(req1);
            customService.notifyBrowser(req2);

            assertThat(capturedRequests).hasSize(2);
            assertThat(capturedRequests.get(0).getMessage()).isEqualTo("Message 1");
            assertThat(capturedRequests.get(1).getMessage()).isEqualTo("Message 2");
        }
    }
}
