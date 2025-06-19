package com.example.SindhiProgrammingLanguage.SecurityLayer;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final RestTemplate rest = new RestTemplate();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        long start = System.currentTimeMillis();

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        String userAgent = request.getHeader("User-Agent");
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        String os = extractOS(userAgent);
        String browser = extractBrowser(userAgent);
        String device = userAgent != null && userAgent.toLowerCase().contains("mobile") ? "Mobile" : "Desktop";

        Map<?, ?> geo = fetchGeoData(ip);

        chain.doFilter(req, res);

        long duration = System.currentTimeMillis() - start;
        String country = geo.get("country_name") != null ? geo.get("country_name").toString() : "Unknown";
        String region = geo.get("region") != null ? geo.get("region").toString() : "Unknown";
        String city = geo.get("city") != null ? geo.get("city").toString() : "Unknown";

        logger.info("🌐 Request Info: IP={}, URI={}, Device={}, OS={}, Browser={}, Origin={}, Referer={}, Country={}, Region={}, City={}, Duration={}ms",
                ip,
                request.getRequestURI(),
                device,
                os,
                browser,
                origin,
                referer,
                country,
                region,
                city,
                duration);

    }

    private Map<?, ?> fetchGeoData(String ip) {
        try {
            // Use ipapi.co to get geolocation JSON
            ResponseEntity<Map> resp = rest.getForEntity(
                    "https://ipapi.co/" + ip + "/json/", Map.class);
            return resp.getBody();
        } catch (Exception e) {
            logger.warn("Geo fetch failed for IP {}: {}", ip, e.getMessage());
            return Map.of();
        }
    }

    private String extractOS(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "Mac";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone")) return "iOS";
        return "Other";
    }

    private String extractBrowser(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome")) return "Safari";
        return "Other";
    }
}

