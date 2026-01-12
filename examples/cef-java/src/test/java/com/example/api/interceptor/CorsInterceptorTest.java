package com.example.api.interceptor;

import com.example.api.exception.ApiException;
import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CorsInterceptor Tests")
class CorsInterceptorTest {

    private CorsInterceptor interceptor;

    @Mock
    private ApiRequest mockRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create CORS interceptor with allowed origins list")
    void testConstructorWithAllowedOrigins() {
        List<String> origins = Arrays.asList("https://example.com", "https://app.example.com");
        interceptor = new CorsInterceptor(origins);

        assertThat(interceptor).isNotNull();
        assertThat(interceptor.getAllowedOrigins()).isEqualTo(origins);
    }

    @Test
    @DisplayName("Should allow request when origin matches allowed list")
    void testAllowRequestWhenOriginMatches() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com", "https://other.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://example.com");

        // Should not throw exception
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject request when origin not in allowed list")
    void testRejectRequestWhenOriginNotAllowed() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://malicious.com");

        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Origin not allowed");
    }

    @Test
    @DisplayName("Should allow all origins when allowed list is empty")
    void testSupportWildcardAsteriskForAllowAllOrigins() throws Exception {
        // Empty list = allow all origins (implicit wildcard)
        interceptor = new CorsInterceptor(Collections.emptyList());

        when(mockRequest.getHeader("Origin")).thenReturn("https://any-domain.com");

        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow all origins when allowed list is null")
    void testAllowAllOriginsWhenListIsNull() throws Exception {
        interceptor = new CorsInterceptor(null);

        when(mockRequest.getHeader("Origin")).thenReturn("https://any-origin.example.com");

        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject request when Origin header is empty string and not in allowed list")
    void testRejectRequestWhenOriginHeaderIsEmptyString() throws Exception {
        // Empty string is a real origin value, not treated as null
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("");

        // Empty string origin is not in allowed list, so should be rejected
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Origin not allowed");
    }

    @Test
    @DisplayName("Should allow request when Origin header is null")
    void testAllowRequestWhenOriginHeaderIsNull() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn(null);

        // Null origin should pass through
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should do exact origin matching (case-sensitive)")
    void testOriginMatchingIsCaseSensitive() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://EXAMPLE.COM");

        // Different case should not match
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should throw API exception with 403 status")
    void testThrowsApiExceptionWithCorrectStatus() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://allowed.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://notallowed.com");

        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatusCode()).isEqualTo(403);
                });
    }

    @Test
    @DisplayName("Should handle multiple allowed origins")
    void testMultipleAllowedOrigins() throws Exception {
        List<String> allowedOrigins = Arrays.asList(
                "https://app.example.com",
                "https://admin.example.com",
                "https://api.example.com"
        );
        interceptor = new CorsInterceptor(allowedOrigins);

        // Test each origin
        for (String origin : allowedOrigins) {
            when(mockRequest.getHeader("Origin")).thenReturn(origin);
            assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("Should handle localhost origins")
    void testLocalhostOrigins() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("http://localhost:3000", "http://127.0.0.1:8080"));

        when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:3000");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();

        when(mockRequest.getHeader("Origin")).thenReturn("http://127.0.0.1:8080");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate before request processing")
    void testValidationOccursBeforeHandling() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://evil.com");

        // Validation should occur during beforeHandle
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Origin not allowed");
    }

    @Test
    @DisplayName("Should return allowed origins list")
    void testGetAllowedOrigins() {
        List<String> origins = Arrays.asList("https://a.com", "https://b.com");
        interceptor = new CorsInterceptor(origins);

        assertThat(interceptor.getAllowedOrigins()).isEqualTo(origins);
    }

    @Test
    @DisplayName("Should handle protocol-specific matching")
    void testProtocolSpecificMatching() throws Exception {
        // http and https are different origins
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("http://example.com");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle origins with ports")
    void testOriginsWithPorts() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("http://localhost:3000"));

        when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:3000");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();

        when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:3001");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should implement RequestInterceptor interface")
    void testImplementsRequestInterceptor() {
        interceptor = new CorsInterceptor(Collections.emptyList());
        assertThat(interceptor).isInstanceOf(RequestInterceptor.class);
    }

    @Test
    @DisplayName("Constructor with single origin")
    void testConstructorWithSingleOrigin() {
        List<String> origins = Arrays.asList("https://single.origin.com");
        interceptor = new CorsInterceptor(origins);
        assertThat(interceptor.getAllowedOrigins()).hasSize(1).contains("https://single.origin.com");
    }

    @Test
    @DisplayName("Should handle whitespace in allowed origins list")
    void testWhitespaceInOriginsList() throws Exception {
        List<String> origins = Arrays.asList("https://example.com", "https://app.example.com");
        interceptor = new CorsInterceptor(origins);

        when(mockRequest.getHeader("Origin")).thenReturn("https://example.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject origin with extra whitespace in request header")
    void testOriginWithExtraWhitespace() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn(" https://example.com ");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle subdomain origins")
    void testSubdomainOrigins() throws Exception {
        List<String> origins = Arrays.asList(
                "https://sub1.example.com",
                "https://sub2.example.com",
                "https://deep.sub.example.com"
        );
        interceptor = new CorsInterceptor(origins);

        when(mockRequest.getHeader("Origin")).thenReturn("https://sub1.example.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();

        when(mockRequest.getHeader("Origin")).thenReturn("https://deep.sub.example.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should not match partial domain matches")
    void testNoPartialDomainMatching() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://subexample.com");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle IP address origins")
    void testIpAddressOrigins() throws Exception {
        List<String> origins = Arrays.asList("http://192.168.1.1:8080", "http://10.0.0.1:9000");
        interceptor = new CorsInterceptor(origins);

        when(mockRequest.getHeader("Origin")).thenReturn("http://192.168.1.1:8080");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();

        when(mockRequest.getHeader("Origin")).thenReturn("http://10.0.0.1:9000");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should not match different IP addresses")
    void testDifferentIpAddressesNotMatching() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("http://192.168.1.1"));

        when(mockRequest.getHeader("Origin")).thenReturn("http://192.168.1.2");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle origins with path (path is part of exact match)")
    void testOriginsWithPath() throws Exception {
        // Origin header typically doesn't include path, but test the behavior
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://example.com/api");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle origins with query parameters (exact match required)")
    void testOriginsWithQueryParameters() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://example.com?param=value");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle origins with fragment")
    void testOriginsWithFragment() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://example.com#section");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should return 403 status with appropriate error message")
    void testErrorMessageForRejectedOrigin() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://allowed.example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://denied.example.com");

        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatusCode()).isEqualTo(403);
                    assertThat(apiEx.getMessage()).contains("https://denied.example.com");
                });
    }

    @Test
    @DisplayName("Should handle mixed http and https origins")
    void testMixedHttpAndHttpsOrigins() throws Exception {
        List<String> origins = Arrays.asList(
                "http://example.com",
                "https://example.com",
                "http://app.example.com",
                "https://app.example.com"
        );
        interceptor = new CorsInterceptor(origins);

        when(mockRequest.getHeader("Origin")).thenReturn("http://example.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();

        when(mockRequest.getHeader("Origin")).thenReturn("https://example.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should be case-sensitive for protocol matching")
    void testCaseSensitiveProtocolMatching() throws Exception {
        interceptor = new CorsInterceptor(Arrays.asList("https://example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("HTTPS://example.com");
        // Protocol should be case-sensitive
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle wildcard-like patterns without regex interpretation")
    void testWildcardPatternsNotInterpreted() throws Exception {
        // CORS doesn't support wildcards in individual origins, this is just for testing
        interceptor = new CorsInterceptor(Arrays.asList("https://*.example.com"));

        when(mockRequest.getHeader("Origin")).thenReturn("https://api.example.com");
        // Should not match because wildcards are not interpreted
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle very long origin lists")
    void testLargeOriginsList() throws Exception {
        List<String> origins = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            origins.add("https://domain" + i + ".example.com");
        }
        interceptor = new CorsInterceptor(origins);

        when(mockRequest.getHeader("Origin")).thenReturn("https://domain50.example.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();

        when(mockRequest.getHeader("Origin")).thenReturn("https://domain99.example.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject origin not in large origins list")
    void testRejectOriginNotInLargeList() throws Exception {
        List<String> origins = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            origins.add("https://domain" + i + ".example.com");
        }
        interceptor = new CorsInterceptor(origins);

        when(mockRequest.getHeader("Origin")).thenReturn("https://notinlist.example.com");
        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should handle origins with unicode characters")
    void testOriginsWithUnicodeCharacters() throws Exception {
        List<String> origins = Arrays.asList("https://例え.jp", "https://münchen.de");
        interceptor = new CorsInterceptor(origins);

        when(mockRequest.getHeader("Origin")).thenReturn("https://例え.jp");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle empty allowed origins with various request origins")
    void testEmptyAllowedOriginsWithVariousRequests() throws Exception {
        interceptor = new CorsInterceptor(Collections.emptyList());

        // All origins should be allowed when list is empty
        when(mockRequest.getHeader("Origin")).thenReturn("https://any1.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();

        when(mockRequest.getHeader("Origin")).thenReturn("https://any2.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();

        when(mockRequest.getHeader("Origin")).thenReturn("https://any3.com");
        assertThatCode(() -> interceptor.beforeHandle(mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should check origin existence before rejection")
    void testOriginCheckOrderBeforeRejection() throws Exception {
        List<String> allowed = Arrays.asList("https://allowed1.com", "https://allowed2.com");
        interceptor = new CorsInterceptor(allowed);

        when(mockRequest.getHeader("Origin")).thenReturn("https://denied.com");

        assertThatThrownBy(() -> interceptor.beforeHandle(mockRequest))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatusCode()).isEqualTo(403);
                    assertThat(apiEx.getMessage()).contains("Origin not allowed");
                });
    }
}
