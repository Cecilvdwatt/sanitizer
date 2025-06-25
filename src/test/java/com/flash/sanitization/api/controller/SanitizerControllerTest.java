package com.flash.sanitization.api.controller;

import com.flash.sanitization.api.representation.SanitizerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.sanitizer.registry.SanitizerRegistry;
import com.flash.sanitization.sanitizer.service.SanitizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("h2")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // So H2 can clear the data.
public class SanitizerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SanitizationService sanitizationService;

    @Autowired
    private SanitizerRegistry sanitizerRegistry;

    @BeforeEach
    public void setUp() {
        sanitizerRegistry.clearCache();
    }

    @Test
    public void testSanitizeEndpoint_Html() throws Exception {

        // configure the database for html.
        sanitizationService.createInputType(
            "html",
            List.of(new ConfigRecord(
                "web-sanitizer",
                "html-sanitizer",
                null
            ))
        );


        SanitizerRequest request = new SanitizerRequest();
        request.setRequestId("12345");
        request.setToSanitize("<body>TEST</body>");
        request.setInputType("html");

        mockMvc.perform(post("/flash/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestId").value("12345"))
            .andExpect(jsonPath("$.sanitized").value("TEST"));
    }

    @Test
    public void testSanitizeEndpoint_Combined() throws Exception {

        // configure the database for html.
        sanitizationService.createInputType(
            "html",
            List.of(new ConfigRecord(
                    "Test123",
                    "word-sanitizer-factory",
                    new HashMap<>() {
                        {
                            put("mask", "XXX");
                            put("src", "LIST:Text,Sanitized");
                        }
                    }
                ),
                new ConfigRecord(
                    "html-sanitizer",
                    null,
                    null
                )
            )
        );

        SanitizerRequest request = new SanitizerRequest();
        request.setRequestId("12345");
        request.setToSanitize("<div>This Text Will Be Sanitized</div><script/>");
        request.setInputType("html");

        mockMvc.perform(post("/flash/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestId").value("12345"))
            .andExpect(jsonPath("$.sanitized").value("This XXX Will Be XXX"));
    }

    @Test
    public void testSanitizeEndpoint_NoType() throws Exception {

        SanitizerRequest request = new SanitizerRequest();
        request.setRequestId("12345");
        request.setToSanitize("ThIS ACTION FETCH IS PRESERVE SYSNIX UNION COOL SPACE TEMPORARY");
        request.setInputType("");

        mockMvc.perform(post("/flash/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestId").value("12345"))
            .andExpect(jsonPath("$.sanitized").value("ThIS *** *** IS *** *** *** COOL *** ***"));
    }

    @Test
    public void testSanitizeEndpoint_TextList() throws Exception {

        // configure the database for html.
        sanitizationService.createInputType(
            "html",
            List.of(new ConfigRecord(
                "WebContent",
                "word-sanitizer-factory",
                new HashMap<>() {
                    {
                        put("mask", "XXX");
                        put("src", "LIST:Text,Sanitized");
                    }
                }
            ))
        );

        SanitizerRequest request = new SanitizerRequest();
        request.setRequestId("12345");
        request.setToSanitize("This Text Will Be Sanitized");
        request.setInputType("html");

        mockMvc.perform(post("/flash/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestId").value("12345"))
            .andExpect(jsonPath("$.sanitized").value("This XXX Will Be XXX"));
    }

    @Test
    public void testSanitizeEndpoint_TextFile() throws Exception {

        Resource resource = new ClassPathResource("default-word-list.txt");
        File file = resource.getFile(); // This gives a real File

        // configure the database for html.
        sanitizationService.createInputType(
            "html",
            List.of(new ConfigRecord(
                "WebContent",
                "word-sanitizer-factory",
                new HashMap<>() {
                    {
                        put("mask", "");
                        put("src", "FILE:" + file.getAbsolutePath());
                    }
                }
            ))
        );

        SanitizerRequest request = new SanitizerRequest();
        request.setRequestId("12345");
        request.setToSanitize("ThIS ACTION FETCH IS PRESERVE SYSNIX UNION COOL SPACE TEMPORARY");
        request.setInputType("html");

        mockMvc.perform(post("/flash/sanitize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestId").value("12345"))
            .andExpect(jsonPath("$.sanitized").value("ThIS *** *** IS *** *** *** COOL *** ***"));
    }
}
