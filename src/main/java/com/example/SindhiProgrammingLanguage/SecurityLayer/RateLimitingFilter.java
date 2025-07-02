package com.example.SindhiProgrammingLanguage.SecurityLayer;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;


import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String ip = request.getRemoteAddr();

        Bucket bucket = buckets.computeIfAbsent(ip, k -> createNewBucket());
        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setStatus(429);
            response.getWriter().write("Too many requests - Rate limit exceeded");
        }
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}

