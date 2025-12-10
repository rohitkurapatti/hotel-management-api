package com.reservation.hotel.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignTraceIdInterceptor implements RequestInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(TRACE_ID_MDC_KEY);
        if (traceId != null && !traceId.isEmpty()) {
            template.header(TRACE_ID_HEADER, traceId);
            log.debug("Adding trace-id {} to Feign request: {} {}", traceId, template.method(), template.url());
        }
    }
}

