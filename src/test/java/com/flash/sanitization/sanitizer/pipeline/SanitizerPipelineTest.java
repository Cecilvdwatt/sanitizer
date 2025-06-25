package com.flash.sanitization.sanitizer.pipeline;

import com.flash.sanitization.sanitizer.implementation.Sanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SanitizerPipelineTest {

    /**
     * A simple pipeline with a single mock sanitizer.
     * Simply checks if the sanitizer is called or not.
     */
    @Test
    void simplePipeline_isSanitizerCalled() {

        Sanitizer mockSanitizer = mock(Sanitizer.class);
        when(mockSanitizer.sanitize(Mockito.anyString())).thenReturn("sanitized");

        SanitizerPipeline pipeline = new SanitizerPipeline(List.of(mockSanitizer));

        assertThat(pipeline.sanitize("unsanitized")).isEqualTo("sanitized");
    }

    @Test
    void pipeline_twoSanitizers() {

        // String are immutable so throw it into an array
        String[] valueToCheck = {"Value"};

        Sanitizer mockSanitizerA = mock(Sanitizer.class);

        // assignments "return" values.
        when(mockSanitizerA.sanitize(Mockito.anyString()))
            .thenAnswer(invocation -> valueToCheck[0] = valueToCheck[0] + "A");

        Sanitizer mockSanitizerB = mock(Sanitizer.class);
        when(mockSanitizerB.sanitize(Mockito.anyString()))
            .thenAnswer(invocation -> valueToCheck[0] = valueToCheck[0] + "B");

        SanitizerPipeline pipeline = new SanitizerPipeline(List.of(mockSanitizerA, mockSanitizerB));

        assertThat(pipeline.sanitize("unsanitized")).isEqualTo("ValueAB");
    }

    @Test
    void pipeline_multiSanitizers() {

        // ints are primitives so throw it into an array
        int[] valueToCheck = {0};

        List<Sanitizer> pipelineSanitizers = new ArrayList<>();

        for(int i = 1; i <= 100; i++) {
            Sanitizer mockSanitizer = mock(Sanitizer.class);

            int finalI = i;
            when(mockSanitizer.sanitize(Mockito.anyString()))
                .thenAnswer(invocation -> String.valueOf(valueToCheck[0] = valueToCheck[0] + finalI));

            pipelineSanitizers.add(mockSanitizer);
        }

        SanitizerPipeline pipeline = new SanitizerPipeline(pipelineSanitizers);

        // Google told me that the sum of all numbers between 1 and 100 is 5,050
        assertThat(pipeline.sanitize("Doesn't Matter")).isEqualTo("5050");
    }

}