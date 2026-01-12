package com.example.api.util;

import com.example.api.protocol.MultipartFile;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MultipartParser.
 */
class MultipartParserTest {

    @Test
    void testParse_SingleFormField() {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String body = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "\r\n" +
            "Test Task\r\n" +
            "------WebKitFormBoundary7MA4YWxkTrZu0gW--";

        String contentType = "multipart/form-data; boundary=" + boundary;

        MultipartParser.MultipartData data = MultipartParser.parse(
            body.getBytes(StandardCharsets.UTF_8),
            contentType
        );

        assertEquals("Test Task", data.getField("title"));
        assertTrue(data.getFiles().isEmpty());
    }

    @Test
    void testParse_FileUpload() {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String body = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "File content here\r\n" +
            "------WebKitFormBoundary7MA4YWxkTrZu0gW--";

        String contentType = "multipart/form-data; boundary=" + boundary;

        MultipartParser.MultipartData data = MultipartParser.parse(
            body.getBytes(StandardCharsets.UTF_8),
            contentType
        );

        MultipartFile file = data.getFile("file");
        assertNotNull(file);
        assertEquals("test.txt", file.getOriginalFilename());
        assertEquals("text/plain", file.getContentType());
        assertEquals("File content here", file.getContentAsString().trim());
    }

    @Test
    void testParse_MultipleFields() {
        String boundary = "----Boundary";
        String body = "------Boundary\r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "\r\n" +
            "My Title\r\n" +
            "------Boundary\r\n" +
            "Content-Disposition: form-data; name=\"description\"\r\n" +
            "\r\n" +
            "My Description\r\n" +
            "------Boundary--";

        String contentType = "multipart/form-data; boundary=" + boundary;

        MultipartParser.MultipartData data = MultipartParser.parse(
            body.getBytes(StandardCharsets.UTF_8),
            contentType
        );

        assertEquals("My Title", data.getField("title"));
        assertEquals("My Description", data.getField("description"));
    }

    @Test
    void testParse_MixedFieldsAndFiles() {
        String boundary = "----Boundary";
        String body = "------Boundary\r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "\r\n" +
            "Document\r\n" +
            "------Boundary\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"doc.pdf\"\r\n" +
            "Content-Type: application/pdf\r\n" +
            "\r\n" +
            "PDF content\r\n" +
            "------Boundary--";

        String contentType = "multipart/form-data; boundary=" + boundary;

        MultipartParser.MultipartData data = MultipartParser.parse(
            body.getBytes(StandardCharsets.UTF_8),
            contentType
        );

        assertEquals("Document", data.getField("title"));
        MultipartFile file = data.getFile("file");
        assertNotNull(file);
        assertEquals("doc.pdf", file.getOriginalFilename());
        assertEquals("application/pdf", file.getContentType());
    }

    @Test
    void testParse_NoBoundary() {
        assertThrows(IllegalArgumentException.class, () ->
            MultipartParser.parse(
                "data".getBytes(),
                "multipart/form-data"
            )
        );
    }

    @Test
    void testParse_NullBody() {
        assertThrows(IllegalArgumentException.class, () ->
            MultipartParser.parse(null, "multipart/form-data; boundary=test")
        );
    }

    @Test
    void testMultipartFile_GetSize() {
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MultipartFile("file", "test.txt", "text/plain", content);

        assertEquals(content.length, file.getSize());
    }

    @Test
    void testMultipartFile_IsEmpty() {
        MultipartFile emptyFile = new MultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        MultipartFile nonEmptyFile = new MultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        assertTrue(emptyFile.isEmpty());
        assertFalse(nonEmptyFile.isEmpty());
    }

    @Test
    void testMultipartFile_GetInputStream() {
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MultipartFile("file", "test.txt", "text/plain", content);

        assertNotNull(file.getInputStream());
    }
}
