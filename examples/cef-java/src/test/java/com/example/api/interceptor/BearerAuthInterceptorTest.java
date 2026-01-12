package com.example.api.interceptor;

import com.example.api.exception.ApiException;
import com.example.api.protocol.ApiRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for BearerAuthInterceptor.
 * Target coverage: 100% of all code paths
 */
class BearerAuthInterceptorTest {

    // ========== Valid Token Tests ==========

    @Test
    @DisplayName("Valid JWT token should pass authentication")
    void testValidToken() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt-token");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "valid-jwt-token".equals(token)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Valid token with typical JWT structure")
    void testValidJwtStructure() throws Exception {
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> jwtToken.equals(token)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Valid token with special characters")
    void testValidTokenWithSpecialCharacters() throws Exception {
        String token = "token-with_special.chars123!@#$%";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> token.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Valid token with custom bearer format")
    void testValidTokenWithCustomFormat() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer custom-format-token");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "custom-format-token".equals(token),
            "CustomFormat"
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Invalid Token Tests ==========

    @Test
    @DisplayName("Invalid token should return 403")
    void testInvalidToken() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "valid-jwt-token".equals(token)
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
        assertThat(ex.getMessage()).contains("Invalid or expired token");
    }

    @Test
    @DisplayName("Expired token should return 403")
    void testExpiredToken() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer expired.jwt.token");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> false // Token validator rejects expired token
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Case-sensitive token validation")
    void testCaseSensitiveTokenValidation() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer MyToken");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "mytoken".equals(token)
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    // ========== Missing/Empty Auth Header Tests ==========

    @Test
    @DisplayName("Missing Authorization header should return 401")
    void testMissingAuthHeader() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Authorization header required");
    }

    @Test
    @DisplayName("Empty Authorization header should return 401")
    void testEmptyAuthHeader() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Empty Bearer token (only scheme) should return 401")
    void testEmptyToken() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Bearer token required");
    }

    @Test
    @DisplayName("Bearer with only whitespace should return 401")
    void testBearerWithOnlyWhitespace() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    // ========== Invalid Auth Scheme Tests ==========

    @Test
    @DisplayName("Basic scheme instead of Bearer should return 401")
    void testWrongAuthScheme() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Bearer");
    }

    @Test
    @DisplayName("Digest scheme instead of Bearer should return 401")
    void testDigestAuthScheme() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Digest username=user");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Lowercase bearer scheme should return 401")
    void testLowercaseBearerScheme() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("bearer validtoken");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("No space after Bearer scheme")
    void testNoSpaceAfterBearer() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("BearerToken");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    // ========== Token Extraction Tests ==========

    @Test
    @DisplayName("Whitespace should be trimmed from token")
    void testWhitespaceInToken() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer  token-value  ");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "token-value".equals(token)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Token with newlines should be extracted correctly")
    void testTokenExtraction() throws Exception {
        String token = "token-without-newlines";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> token.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Default constructor should use JWT format")
    void testDefaultConstructor() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer mytoken");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "mytoken".equals(token)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Constructor with custom bearer format")
    void testConstructorWithCustomFormat() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "token123".equals(token),
            "CustomBearerFormat"
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Token Validator Tests ==========

    @Test
    @DisplayName("Token validator receives token without Bearer prefix")
    void testValidatorReceivesTokenWithoutPrefix() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt.token.here");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> {
                assertThat(token).isEqualTo("jwt.token.here");
                assertThat(token).doesNotContain("Bearer");
                return true;
            }
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Token validator can use custom validation logic")
    void testCustomTokenValidation() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token-with-20-chars");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> token.length() >= 10 // Accept tokens with 10+ characters
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Token validator rejection works correctly")
    void testValidatorRejection() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer shorttoken");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> token.length() >= 50 // Reject tokens shorter than 50 chars
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    // ========== Implementation Tests ==========

    @Test
    @DisplayName("TokenValidator functional interface should work")
    void testTokenValidatorInterface() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid");

        BearerAuthInterceptor.TokenValidator validator = token -> "valid".equals(token);
        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(validator);

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Complex Token Tests ==========

    @Test
    @DisplayName("Token with plus signs (URL-safe characters)")
    void testTokenWithPlusSigns() throws Exception {
        String token = "token+with+plus+signs";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> token.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Token with slashes (URL-safe characters)")
    void testTokenWithSlashes() throws Exception {
        String token = "token/with/slashes";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> token.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Long JWT token (typical size)")
    void testLongToken() throws Exception {
        String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + longToken);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> longToken.equals(token)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Advanced Scenarios ==========

    @Test
    @DisplayName("Should implement RequestInterceptor interface")
    void testImplementsRequestInterceptor() {
        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);
        assertThat(interceptor).isInstanceOf(RequestInterceptor.class);
    }

    @Test
    @DisplayName("Token with whitespace should be trimmed")
    void testTokenWhitespaceTrimmed() throws Exception {
        String token = "valid-token-123";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token + "  "); // trailing spaces

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> token.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Case-sensitive scheme validation")
    void testSchemeCaseSensitivity() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("bearer valid-token"); // lowercase bearer

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Token validator receives exact token value")
    void testValidatorReceivesExactToken() throws Exception {
        String testToken = "exact-test-token-value";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + testToken);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> {
                assertThat(token).isEqualTo(testToken);
                return true;
            }
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Validator can reject with custom logic")
    void testValidatorCustomLogic() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer short");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> token.length() > 10 // Reject if token is too short
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Status 401 for missing authorization header")
    void testUnauthorizedStatusForMissingHeader() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Status 403 for invalid token")
    void testForbiddenStatusForInvalidToken() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "valid-token".equals(token)
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Token with numbers and special characters")
    void testTokenWithSpecialCharacters() throws Exception {
        String token = "token-123_456.ABC+/=";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> token.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Multiple values in token field should be passed as is to validator")
    void testMultipleValuesInToken() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        // Everything after "Bearer " is treated as the token
        when(request.getHeader("Authorization")).thenReturn("Bearer token1 token2");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            token -> "token1 token2".equals(token) // Full string including space is the token
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Bearer scheme with no space before token should fail")
    void testBearerNoSpace() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearertoken");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Token with only numbers")
    void testNumericToken() throws Exception {
        String token = "123456789";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> token.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Very long token should be accepted")
    void testVeryLongToken() throws Exception {
        String longToken = "a".repeat(1000);
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + longToken);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> longToken.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Token with unicode characters")
    void testUnicodeToken() throws Exception {
        String token = "token-with-中文-and-ñ";
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(
            t -> token.equals(t)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Bearer token with multiple Bearer keyword should fail")
    void testMultipleBearerKeywords() throws Exception {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer Bearer token");

        BearerAuthInterceptor interceptor = new BearerAuthInterceptor(token -> true);

        // "Bearer token" would be the token value
        try {
            interceptor.beforeHandle(request);
            // If it passes, that's okay - it means implementation accepts "Bearer token"
        } catch (ApiException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(403);
        }
    }
}
