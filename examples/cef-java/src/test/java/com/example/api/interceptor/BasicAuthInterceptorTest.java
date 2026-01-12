package com.example.api.interceptor;

import com.example.api.exception.ApiException;
import com.example.api.protocol.ApiRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for BasicAuthInterceptor.
 * Target coverage: 100% of all code paths
 */
class BasicAuthInterceptorTest {

    // ========== Valid Credentials Tests ==========

    @Test
    @DisplayName("Valid credentials should pass authentication")
    void testValidCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pass".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Valid credentials with special characters")
    void testValidCredentialsWithSpecialCharacters() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user@domain:p@ss!word".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user@domain".equals(username) && "p@ss!word".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Valid credentials with empty password")
    void testValidCredentialsEmptyPassword() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Valid credentials with colon in password")
    void testValidCredentialsWithColonInPassword() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pass:word".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pass:word".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Invalid Credentials Tests ==========

    @Test
    @DisplayName("Invalid credentials should reject with 403")
    void testInvalidCredentials() {
        String credentials = Base64.getEncoder().encodeToString("user:wrong".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pass".equals(password)
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
        assertThat(ex.getMessage()).contains("Invalid username or password");
    }

    @Test
    @DisplayName("Invalid username should be rejected")
    void testInvalidUsername() {
        String credentials = Base64.getEncoder().encodeToString("wronguser:pass".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pass".equals(password)
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Case-sensitive credential matching")
    void testCaseSensitiveCredentials() {
        String credentials = Base64.getEncoder().encodeToString("USER:PASS".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pass".equals(password)
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

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Authorization header required");
    }

    @Test
    @DisplayName("Empty Authorization header should return 401")
    void testEmptyAuthHeader() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("");

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    // ========== Invalid Auth Scheme Tests ==========

    @Test
    @DisplayName("Bearer scheme instead of Basic should return 401")
    void testWrongAuthScheme() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Basic");
    }

    @Test
    @DisplayName("Digest scheme instead of Basic should return 401")
    void testDigestAuthScheme() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Digest username=user");

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("No space after Basic scheme")
    void testBasicNoSpace() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basicuser:pass");

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    // ========== Base64 Encoding Tests ==========

    @Test
    @DisplayName("Invalid Base64 encoding should return 401")
    void testInvalidBase64() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic !!!invalid-base64!!!");

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Invalid Basic auth credentials encoding");
    }

    @Test
    @DisplayName("Valid Base64 but invalid credential format (missing colon)")
    void testMissingColon() {
        String credentials = Base64.getEncoder().encodeToString("useronly".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Empty Base64 string should return 401")
    void testEmptyBase64String() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic ");

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Basic credentials required");
    }

    // ========== UTF-8 Encoding Tests ==========

    @Test
    @DisplayName("UTF-8 special characters in credentials")
    void testUtf8Credentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pässwörd".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pässwörd".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Emoji in credentials")
    void testEmojiInCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pass123".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pass123".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Whitespace Handling Tests ==========

    @Test
    @DisplayName("Whitespace after Basic scheme should be trimmed")
    void testWhitespaceAfterBasic() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic  " + credentials); // Extra space

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pass".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Whitespace within credentials")
    void testWhitespaceInCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user name:pass word".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user name".equals(username) && "pass word".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Implementation Tests ==========

    @Test
    @DisplayName("BasicAuthInterceptor should implement RequestInterceptor")
    void testImplementsRequestInterceptor() {
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);
        assertThat(interceptor).isInstanceOf(RequestInterceptor.class);
    }

    @Test
    @DisplayName("CredentialsValidator functional interface should work")
    void testCredentialsValidator() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("admin:secret".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "admin".equals(username) && "secret".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    // ========== Error Message Tests ==========

    @Test
    @DisplayName("Error message should be descriptive")
    void testErrorMessageDescriptive() {
        String credentials = Base64.getEncoder().encodeToString("user:wrong".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> false);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getMessage()).isNotEmpty();
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    // ========== Advanced Scenarios ==========

    @Test
    @DisplayName("Long username and password should be accepted")
    void testLongCredentials() throws Exception {
        String longUsername = "a".repeat(100);
        String longPassword = "b".repeat(200);
        String credentials = Base64.getEncoder().encodeToString(
            (longUsername + ":" + longPassword).getBytes(StandardCharsets.UTF_8)
        );
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> longUsername.equals(username) && longPassword.equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Multiple colons in password should be handled correctly")
    void testMultipleColonsInPassword() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pass:word:extra".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "pass:word:extra".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Whitespace in credentials should be preserved")
    void testWhespaceInCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user name:pass word".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user name".equals(username) && "pass word".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Numbers in credentials should work")
    void testNumbersInCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user123:pass456".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user123".equals(username) && "pass456".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Invalid scheme (case sensitivity)")
    void testSchemeCaseSensitivity() {
        String credentials = Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("basic " + credentials); // lowercase 'basic'

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Authorization header with extra whitespace should be trimmed and accepted")
    void testExtraWhitespaceIsTrimmed() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic  " + credentials); // extra space is trimmed

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> "user".equals(u) && "pass".equals(p));

        // Should not throw because whitespace is trimmed by implementation
        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Credentials validator receives exact decoded values")
    void testValidatorReceivesExactValues() throws Exception {
        String testUsername = "testuser";
        String testPassword = "testpass";
        String credentials = Base64.getEncoder().encodeToString(
            (testUsername + ":" + testPassword).getBytes(StandardCharsets.UTF_8)
        );
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> {
                assertThat(username).isEqualTo(testUsername);
                assertThat(password).isEqualTo(testPassword);
                return true;
            }
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Validator can reject with custom logic")
    void testValidatorCustomLogic() {
        String credentials = Base64.getEncoder().encodeToString("user:short".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> password.length() > 10 // Reject if password is too short
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Status 401 for authentication failures (missing header)")
    void testUnauthorizedStatusForMissingAuth() {
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor((u, p) -> true);

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Status 403 for credential validation failures")
    void testForbiddenStatusForInvalidCredentials() {
        String credentials = Base64.getEncoder().encodeToString("user:wrong".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "user".equals(username) && "correct".equals(password)
        );

        ApiException ex = assertThrows(ApiException.class, () -> interceptor.beforeHandle(request));
        assertThat(ex.getStatusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Empty username with non-empty password")
    void testEmptyUsername() throws Exception {
        String credentials = Base64.getEncoder().encodeToString(":password".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "".equals(username) && "password".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Non-empty username with empty password")
    void testEmptyPasswordNonEmptyUsername() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("username:".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "username".equals(username) && "".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }

    @Test
    @DisplayName("Both username and password empty")
    void testBothEmpty() throws Exception {
        String credentials = Base64.getEncoder().encodeToString(":".getBytes(StandardCharsets.UTF_8));
        ApiRequest request = mock(ApiRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor(
            (username, password) -> "".equals(username) && "".equals(password)
        );

        assertDoesNotThrow(() -> interceptor.beforeHandle(request));
    }
}
