package com.example.api.mock;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;

import java.util.Vector;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

/**
 * Centralized factory for creating mock CEF objects.
 * Avoids code duplication across test classes.
 */
public class MockCefFactory {

    /**
     * Create mock CefRequest with URL and HTTP method.
     */
    public static CefRequest createMockRequest(String url, String method) {
        CefRequest mock = mock(CefRequest.class, withSettings().lenient());
        when(mock.getURL()).thenReturn(url);
        when(mock.getMethod()).thenReturn(method);
        when(mock.getPostData()).thenReturn(null);
        return mock;
    }

    /**
     * Create mock CefRequest with POST body.
     */
    public static CefRequest createMockRequestWithBody(String url, String method, String body) {
        CefRequest mock = createMockRequest(url, method);
        CefPostData postData = createMockPostData(body);
        when(mock.getPostData()).thenReturn(postData);
        return mock;
    }

    /**
     * Create mock CefRequest with headers.
     */
    public static CefRequest createMockRequestWithHeaders(String url, String method, java.util.Map<String, String> headers) {
        CefRequest mock = createMockRequest(url, method);
        // Mock getHeader for each header
        headers.forEach((key, value) -> {
            when(mock.getHeaderByName(key)).thenReturn(value);
        });
        return mock;
    }

    /**
     * Create mock CefBrowser.
     */
    public static CefBrowser createMockBrowser() {
        CefBrowser mock = mock(CefBrowser.class, withSettings().lenient());
        return mock;
    }

    /**
     * Create mock CefFrame.
     */
    public static CefFrame createMockFrame() {
        CefFrame mock = mock(CefFrame.class, withSettings().lenient());
        return mock;
    }

    /**
     * Create mock CefPostData with string content.
     */
    public static CefPostData createMockPostData(String data) {
        CefPostData mockPostData = mock(CefPostData.class, withSettings().lenient());
        CefPostDataElement mockElement = mock(CefPostDataElement.class, withSettings().lenient());

        // Setup element - getBytes(int size, byte[] buffer) returns bytes read
        byte[] bytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(mockElement.getBytes(anyInt(), any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(1);
            int length = Math.min(bytes.length, buffer.length);
            System.arraycopy(bytes, 0, buffer, 0, length);
            return length;
        });
        when(mockElement.getBytesCount()).thenReturn(bytes.length);

        // Setup post data - getElements(Vector) fills vector (void method)
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Vector<CefPostDataElement> elements = invocation.getArgument(0);
            elements.add(mockElement);
            return null;
        }).when(mockPostData).getElements(any());
        when(mockPostData.getElementCount()).thenReturn(1);

        return mockPostData;
    }

    /**
     * Create complete mock request setup with all common fields.
     */
    public static MockRequestBuilder builder() {
        return new MockRequestBuilder();
    }

    /**
     * Builder for more complex mock request scenarios.
     */
    public static class MockRequestBuilder {
        private String url = "http://localhost/api";
        private String method = "GET";
        private String body = null;
        private java.util.Map<String, String> headers = new java.util.HashMap<>();

        public MockRequestBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MockRequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public MockRequestBuilder body(String body) {
            this.body = body;
            return this;
        }

        public MockRequestBuilder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public CefRequest build() {
            CefRequest mock;
            if (body != null) {
                mock = createMockRequestWithBody(url, method, body);
            } else {
                mock = createMockRequest(url, method);
            }

            // Add headers
            headers.forEach((key, value) -> {
                when(mock.getHeaderByName(key)).thenReturn(value);
            });

            return mock;
        }
    }

    /**
     * Create mock IntelliJ Project for testing services.
     */
    public static com.intellij.openapi.project.Project createMockProject() {
        return mock(com.intellij.openapi.project.Project.class, withSettings().lenient());
    }
}
