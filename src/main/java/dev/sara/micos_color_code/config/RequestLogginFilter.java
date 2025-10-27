package dev.sara.micos_color_code.config;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class RequestLogginFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
         if (request.getRequestURI().contains("/feedback")) {
            RepeatableContentCachingRequestWrapper wrappedRequest = new RepeatableContentCachingRequestWrapper(request);
            
            String body = wrappedRequest.getBody();
            System.out.println("📦 Body RAW: [" + body + "]");
            System.out.println("📦 Body length: " + body.length());
            System.out.println("📦 Content-Type: " + request.getContentType());
            
            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
