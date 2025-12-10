package com.reservation.hotel.config;

import com.reservation.hotel.constants.AppConstants;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {

            String traceId = httpRequest.getHeader(AppConstants.TRACE_ID_HEADER);
            if (traceId == null || traceId.trim().isEmpty()) {
                traceId = UUID.randomUUID().toString();
                log.debug("Generated new trace-id: {}", traceId);
            } else {
                log.debug("Using existing trace-id: {}", traceId);
            }

            // Put trace-id in MDC for logging
            MDC.put(AppConstants.TRACE_ID_MDC_KEY, traceId);

            // Add trace-id to response header
            httpResponse.setHeader(AppConstants.TRACE_ID_HEADER, traceId);

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

