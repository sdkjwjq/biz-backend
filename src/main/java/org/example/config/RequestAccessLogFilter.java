package org.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Request metadata for diagnosis. Never records query strings, headers, tokens or request bodies. */
@Component
public class RequestAccessLogFilter extends OncePerRequestFilter {
    private static final Logger access = LoggerFactory.getLogger("BACKEND_REQUEST");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long started = System.nanoTime();
        boolean failed = false;
        try {
            chain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException failure) {
            failed = true;
            throw failure;
        } finally {
            // Log the matched route pattern, never raw path variables (which may contain private values).
            Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String path = route instanceof String pattern ? request.getContextPath() + pattern : request.getContextPath() + "/<unmapped>";
            long millis = (System.nanoTime() - started) / 1_000_000;
            access.info("method={} path={} status={} durationMs={}", request.getMethod(), path,
                    failed && response.getStatus() < 400 ? 500 : response.getStatus(), millis);
        }
    }
}
