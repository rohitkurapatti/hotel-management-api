package com.reservation.hotel.config;

import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeignTraceIdInterceptorTest {

    private final FeignTraceIdInterceptor interceptor = new FeignTraceIdInterceptor();

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void testApplyTraceIdWhenTraceIdExists() {
        String traceId = "test-trace-id-12345";
        MDC.put("traceId", traceId);

        RequestTemplate template = new RequestTemplate();
        template.method(Request.HttpMethod.GET);
        template.uri("/test-endpoint");

        interceptor.apply(template);

        assertTrue(template.headers().containsKey("X-Trace-Id"));
        assertEquals(traceId, template.headers().get("X-Trace-Id").iterator().next());
    }
}

